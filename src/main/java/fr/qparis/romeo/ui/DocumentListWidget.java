package fr.qparis.romeo.ui;

import fr.qparis.romeo.excel.ExcelColumn;
import fr.qparis.romeo.excel.ExcelWorksheet;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import fr.qparis.romeo.excel.ExcelWorkbook;
import fr.qparis.romeo.excel.QueryStringProvider;

import java.util.List;
import java.util.function.Consumer;

public class DocumentListWidget extends TreeView<QueryStringProvider> {
    private Consumer<TreeItem<QueryStringProvider>> onNodeClickEvent = (item) -> {};

    public DocumentListWidget() {
        super(new TreeItem<>());
        this.expand(this.getRoot());
        this.setShowRoot(false);

        this.addEventHandler(MouseEvent.MOUSE_CLICKED,
                event -> {
                    if (event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY)) {
                        if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                            expand(getRoot());

                            TreeItem<QueryStringProvider> item = this.getSelectionModel().getSelectedItem();
                            this.onNodeClickEvent.accept(item);
                        }

                        event.consume();
                    }
                }
        );
    }

    public void setOnNodeClickEvent(Consumer<TreeItem<QueryStringProvider>> onNodeClickEvent) {
        this.onNodeClickEvent = onNodeClickEvent;
    }

    private void expand(TreeItem<?> item) {
        item.setExpanded(true);
        item.getChildren().forEach(this::expand);
    }


    public void addWorkbooks(List<ExcelWorkbook> workbookList) {
        for (ExcelWorkbook excelWorkbook : workbookList) {
            final TreeItem<QueryStringProvider> treeItem = new TreeItem<>(excelWorkbook);
            for (ExcelWorksheet excelWorksheet : excelWorkbook.getWorksheets()) {
                final TreeItem<QueryStringProvider> sheetItem = new TreeItem<>(excelWorksheet);
                for (ExcelColumn columnName : excelWorksheet.getColumns()) {
                    final TreeItem<QueryStringProvider> columnItem = new TreeItem<>(columnName);
                    sheetItem.getChildren().add(columnItem);
                }
                treeItem.getChildren().add(sheetItem);
            }
            this.getRoot().getChildren().add(treeItem);
        }

        this.expand(this.getRoot());
    }
}
