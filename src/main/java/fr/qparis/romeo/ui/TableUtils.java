package fr.qparis.romeo.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.*;

import java.text.NumberFormat;

public class TableUtils {

    private static NumberFormat numberFormatter = NumberFormat.getNumberInstance();


    /**
     * Install the keyboard handler:
     * + CTRL + C = copy to clipboard
     *
     * @param table
     */
    public static void installCopyPasteHandler(TableView<?> table) {

        // install copy/paste keyboard handler
        table.setOnKeyPressed(new TableKeyEventHandler());

    }

    /**
     * Get table selection and copy it to the clipboard.
     *
     * @param table to handle
     */
    public static void copySelectionToClipboard(TableView<?> table) {

        StringBuilder plainBuffer = new StringBuilder();
        StringBuilder htmlBuffer = new StringBuilder();

        ObservableList<TablePosition> positionList = table.getSelectionModel().getSelectedCells();

        int prevRow = -1;

        htmlBuffer.append("<html>\n<body>\n<table>\n");
        htmlBuffer.append(" <tr>\n");
        for (TablePosition position : positionList) {
            int viewRow = position.getRow();
            int viewCol = position.getColumn();

            if (prevRow == viewRow) {
                plainBuffer.append('\t');
            } else if (prevRow != -1) {
                plainBuffer.append('\n');
                htmlBuffer.append(" </tr>\n <tr>\n");
            }

            String text = "";

            Object observableValue = table.getVisibleLeafColumn(viewCol).getCellObservableValue(viewRow);

            if (observableValue == null) {
                text = "";
            } else if (observableValue instanceof DoubleProperty) { // TODO: handle boolean etc
                text = numberFormatter.format(((DoubleProperty) observableValue).get());
            } else if (observableValue instanceof IntegerProperty) {
                text = numberFormatter.format(((IntegerProperty) observableValue).get());
            } else if (observableValue instanceof StringProperty) {
                text = ((StringProperty) observableValue).get();
            } else {
                System.out.println("Unsupported observable value: " + observableValue);
            }

            plainBuffer.append(text);
            htmlBuffer.append("  <td>").append(text).append("</td>\n");
            prevRow = viewRow;
        }

        htmlBuffer.append(" </tr>\n");
        htmlBuffer.append("</table>\n</body>\n</html>");

        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(plainBuffer.toString());
        clipboardContent.putHtml(htmlBuffer.toString());


        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    /**
     * Copy/Paste keyboard event handler.
     * The handler uses the keyEvent's source for the clipboard data. The source must be of type TableView.
     */
    public static class TableKeyEventHandler implements EventHandler<KeyEvent> {

        KeyCodeCombination copyKeyCodeCompination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
        KeyCodeCombination selectAll = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_ANY);
        KeyCodeCombination selectAll2 = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_ANY);

        public void handle(final KeyEvent keyEvent) {

            if (copyKeyCodeCompination.match(keyEvent)) {

                if (keyEvent.getSource() instanceof TableView) {
                    copySelectionToClipboard((TableView<?>) keyEvent.getSource());
                    keyEvent.consume();
                }

            }  else if (selectAll.match(keyEvent) || selectAll2.match(keyEvent)) {
                ((TableView<?>) keyEvent.getSource()).getSelectionModel().selectAll();
                keyEvent.consume();
            }
        }

    }

}