package fr.qparis.romeo.ui;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class Logo extends HBox {
    public Logo() {
        super();
        FontAwesomeIconView logo = new FontAwesomeIconView(FontAwesomeIcon.TABLE);
        logo.setId("logo");
        Text text = new Text("Romeo".toLowerCase());
        text.setId("logoText");

        this.getChildren().addAll(logo);
        this.getChildren().add(text);

    }
}
