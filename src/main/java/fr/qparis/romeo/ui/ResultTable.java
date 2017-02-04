package fr.qparis.romeo.ui;

import fr.qparis.romeo.sql.SQLResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.Map;

class ResultTable extends BorderPane {
    private final ResultTableInside resultTableInside;
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();
    private final ResultTableWaiter resultTableWaiter;

    ResultTable() {
        resultTableInside = new ResultTableInside();
        resultTableWaiter = new ResultTableWaiter();
        setStyle("-fx-box-border: transparent;");

        this.setCenter(resultTableInside);
    }

    public void showWaiter() {
        this.setCenter(resultTableWaiter);
    }

    public void showResults() {
        this.setCenter(resultTableInside);
    }


    class ResultTableWaiter extends BorderPane {
        ResultTableWaiter() {
            setCenter(new Label("Please wait..."));
        }
    }

    class ResultTableInside extends TableView<Map<String, Object>> {

        ResultTableInside() {
            super();
            this.setItems(items);
            this.getSelectionModel().setCellSelectionEnabled(true);
            this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            TableUtils.installCopyPasteHandler(this);
        }
    }

    public void setContent(SQLResult sqlResult) {
        resultTableInside.getColumns().clear();

        sqlResult.getColumns().forEach(s -> {
            TableColumn<Map<String, Object>, String> tableColumn = new TableColumn<>(s);
            tableColumn.setCellValueFactory(param -> param.getValue().get(s) == null ? null : new SimpleStringProperty(param.getValue().get(s).toString()));
            resultTableInside.getColumns().add(tableColumn);
        });
        items.clear();

        items.addAll(sqlResult.getResults());

    }
}