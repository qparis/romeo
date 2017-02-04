package fr.qparis.romeo.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.util.Map;
import java.util.function.Consumer;

public class SQLEditor extends Region {
    private final WebView browser;
    private final WebEngine webEngine;
    private Consumer<String> onLocationChanged = (url) -> {};
    private Object query;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SQLEditor() {
        browser = new WebView();
        getChildren().add(browser);

        webEngine = browser.getEngine();
        webEngine.load(getClass().getResource("editor.html").toExternalForm());
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (Worker.State.SUCCEEDED.equals(newValue)) {
                this.onLocationChanged.accept(webEngine.getLocation());
            }
        });
    }

    public void setLocation(String newUrl) {
        webEngine.load(newUrl);
    }

    public void setOnLocationChanged(Consumer<String> onLocationChanged) {
        this.onLocationChanged = onLocationChanged;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    public String getQuery() {
        return (String) webEngine.executeScript("editor.getValue()");
    }

    public void makeReadonly() {
        webEngine.executeScript("makeReadonly()");
    }

    public void removeReadonly() {
        webEngine.executeScript("removeReadonly()");
    }

    public void setTables(Map<?, ?> tables) {
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != Worker.State.SUCCEEDED) {
                return;
            }
            JSObject window = (JSObject)webEngine.executeScript("window");
            try {
                window.call("setTables", objectMapper.writeValueAsString(tables));
            } catch (JsonProcessingException ignored) {
            }
        });

        JSObject window = (JSObject)webEngine.executeScript("window");
        try {
            window.call("setTables", objectMapper.writeValueAsString(tables));
        } catch (JsonProcessingException ignored) {
        }
    }

    public void insertText(String queryHint) {
        JSObject window = (JSObject)webEngine.executeScript("window");
        window.call("appendText", queryHint);
    }
}