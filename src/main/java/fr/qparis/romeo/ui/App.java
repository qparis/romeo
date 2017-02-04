package fr.qparis.romeo.ui;

import com.jacob.com.ComFailException;
import fr.qparis.romeo.excel.ExcelReader;
import fr.qparis.romeo.excel.ExcelWorkbook;
import fr.qparis.romeo.excel.ExcelWorksheet;
import fr.qparis.romeo.excel.ExcelWriter;
import fr.qparis.romeo.sql.HSQLManager;
import fr.qparis.romeo.sql.SQLResult;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class App extends Application {
    private final DocumentListWidget documentListWidget = new DocumentListWidget();
    private final Connection connection;
    private final HSQLManager hsqlManager;
    private final ExcelReader excelReader;
    private final ExcelWriter excelWriter;
    private final ExecutorService executorService;
    private SQLResult currentResults;
    private SQLEditor sqlEditor;
    private ResultTable resultTable;
    private Scene scene;
    private List<ExcelWorkbook> currentWorkbooks = new ArrayList<>();

    public App() {
        try {
            this.connection = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", "");
            this.hsqlManager = new HSQLManager(connection);
            this.excelReader = new ExcelReader();
            this.excelWriter = new ExcelWriter();
            this.executorService = Executors.newSingleThreadExecutor();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws SQLException {
        stage.getIcons().add(new Image(getClass().getResourceAsStream("romeo.png")));
        stage.setOnCloseRequest(event -> this.close());
        final MainWindowHeader mainWindowHeader = new MainWindowHeader();


        final VBox vBox = new VBox();

        final SplitPane horizontalPane = new SplitPane();
        final SplitPane sqlAndResults = new SplitPane();
        sqlAndResults.setDividerPositions(0.7f, 0.3f);


        horizontalPane.getItems().addAll(sqlAndResults, documentListWidget);
        horizontalPane.setDividerPositions(0.7f, 0.3f);
        sqlAndResults.setOrientation(Orientation.VERTICAL);

        vBox.getChildren().addAll(mainWindowHeader, horizontalPane);

        resultTable = new ResultTable();

        stage.setTitle("Romeo");

        mainWindowHeader.setPlayEvent(this::onPlayButtonClick);
        mainWindowHeader.setRefreshEvent(event -> this.refresh());
        mainWindowHeader.setExportEvent(this::export);
        mainWindowHeader.setAboutEvent(event -> about());

        scene = new Scene(vBox, 750, 500);
        scene.getStylesheets().add(this.getClass().getResource("app.css").toExternalForm());
        stage.setScene(scene);
        sqlEditor = new SQLEditor();
        sqlAndResults.getItems().addAll(resultTable, sqlEditor);

        stage.show();
        refresh();


        documentListWidget.setOnNodeClickEvent(s -> sqlEditor.insertText(s.getValue().getQueryHint().toLowerCase()));
    }

    private void close() {
        executorService.shutdownNow();
        Platform.exit();
    }

    private void export(MouseEvent mouseEvent) {
        excelWriter.write(currentResults);
    }

    private void onPlayButtonClick(MouseEvent mouseEvent) {
        final String query = sqlEditor.getQuery();
        startWait();
        executorService.submit(() -> runQuery(query, currentWorkbooks.stream().map(excelWorkbook -> excelWorkbook.getWorksheets().size()).reduce(Integer::sum)));
    }


    private void runQuery(String query, Optional<Integer> tries) {
        runQuery(query, tries.isPresent() ? tries.get() - 1 : 10);
    }

    private void runQuery(String query, int tries) {
        try {
            currentResults = hsqlManager.executeQuery(query);
            Platform.runLater(() -> resultTable.setContent(currentResults));
            Platform.runLater(this::stopWaiting);
        } catch (SQLSyntaxErrorException e) {
            if (e.getCause().getMessage().contains("user lacks privilege or object not found: ")) {
                final String missingTable = e.getCause().getMessage()
                        .replace("user lacks privilege or object not found: ", "").trim();
                if (populateTableFromExcel(missingTable)) {
                    System.out.println("Table was created: " + missingTable);
                    runQuery(query, tries - 1);
                } else {
                    displayError(e);
                }
            } else {
                displayError(e);
            }
        } catch (Throwable e) {
            displayError(e);
        }
    }

    private void stopWaiting() {
        resultTable.showResults();
        sqlEditor.removeReadonly();
    }

    private void startWait() {
        sqlEditor.makeReadonly();
        resultTable.showWaiter();
    }

    private void about() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("romeo_small.png"))));
            alert.setTitle("About Romeo");
            alert.setHeaderText("About Romeo");
            String s = "(c) Copyright Quentin PÂRIS 2016\n\nhttp://romeo.quentin.paris\n\nAll rights reserved. For personal use only.";
            alert.setContentText(s);
            alert.show();
        });
    }
    private void displayError(Throwable e) {
        e.printStackTrace();
        Platform.runLater(() -> {
            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(ExceptionUtils.getStackTrace(e));
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error was encountered");
            alert.setContentText(e.getMessage());
            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();

            stopWaiting();
        });
    }

    private boolean populateTableFromExcel(String missingTable) {
        boolean found = false;
        for (ExcelWorkbook currentWorkbook : currentWorkbooks) {
            for (ExcelWorksheet excelWorksheet : currentWorkbook.getWorksheets()) {
                if (excelWorksheet.getTableName().split("\\.")[1].equalsIgnoreCase(missingTable)) {
                    try {
                        hsqlManager.initializeDatabase(excelWorksheet, this::displayError);
                        found = true;
                    } catch (SQLException e) {
                        displayError(e);
                        e.printStackTrace();
                    }
                }
            }
        }

        return found;
    }

    private void refresh() {
        try {
            hsqlManager.clearDatabase();
            resultTable.setContent(new SQLResult(Collections.emptyList(), Collections.emptyList()));
            resultTable.showWaiter();

            executorService.submit(() -> {
                this.fetchWorkbooks();
                Platform.runLater(resultTable::showResults);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchWorkbooks() {
        try {
            currentWorkbooks = excelReader.getWorkBooks();

            Platform.runLater(() -> {
                documentListWidget.getRoot().getChildren().clear();
                documentListWidget.addWorkbooks(currentWorkbooks);
            });

            final Map<String, List<String>> tableMap = new HashMap<>();
            for (ExcelWorkbook currentWorkbook : currentWorkbooks) {
                for (ExcelWorksheet excelWorksheet : currentWorkbook.getWorksheets()) {
                    tableMap.put(excelWorksheet.getTableName().toLowerCase(),
                            excelWorksheet.getColumns().stream()
                                    .map(excelColumn -> excelColumn.getQueryHint().toLowerCase()).collect(Collectors.toList()));
                }
            }
            Platform.runLater(() -> sqlEditor.setTables(tableMap));
        } catch (ComFailException e) {
            displayError(new IllegalStateException("Unable to connect to Excel. \n" +
                    "Please ensure that the app is installed, that VBA support is enabled and that Excel is not locked (this can occur if you are currently editing a cell)."));
        } catch (UnsatisfiedLinkError e) {
            displayError(new IllegalStateException(e.getMessage()));
        }
    }


}
