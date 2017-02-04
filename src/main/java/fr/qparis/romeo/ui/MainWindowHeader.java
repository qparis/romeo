/*
 * Copyright (C) 2015 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package fr.qparis.romeo.ui;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class MainWindowHeader extends BorderPane {
    private final FontAwesomeIconView play = new FontAwesomeIconView(FontAwesomeIcon.PLAY);
    private final FontAwesomeIconView refresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
    private final FontAwesomeIconView export = new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD);
    private final FontAwesomeIconView about = new FontAwesomeIconView(FontAwesomeIcon.INFO);

    public MainWindowHeader() {
        super();

        Logo logo = new Logo();

        this.setId("headerPane");
        this.setLeft(logo);


        HBox menu = new HBox();
        addStyle(menu, play, refresh, export, about);

        menu.getChildren().addAll(play, refresh, export, about);

        this.setRight(menu);

    }

    private void addStyle(HBox menu, FontAwesomeIconView... icons) {
        for (FontAwesomeIconView icon : icons) {
            HBox.setHgrow(menu, Priority.ALWAYS);
            icon.setStyleClass("thumbs-down-icon");
            icon.setOnMouseEntered(event -> setCursor(Cursor.HAND));
            icon.setWrappingWidth(30);
        }
    }

    public void setPlayEvent(EventHandler<? super MouseEvent> event) {
        play.setOnMouseClicked(event);
    }

    public void setRefreshEvent(EventHandler<? super MouseEvent> event) {
        refresh.setOnMouseClicked(event);
    }

    public void setExportEvent(EventHandler<? super MouseEvent> event) {
        export.setOnMouseClicked(event);
    }

    public void setAboutEvent(EventHandler<? super MouseEvent> event) {
        about.setOnMouseClicked(event);
    }

}
