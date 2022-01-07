package cs1302.gallery;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.application.Platform;

/**
 * A class that creates a menu bar for the {@code GalleryApp}.
 */
public class FileMenu extends MenuBar {

    Menu menu1 = new Menu("File");

    /**
     * A constructor that creates the menu at the top of the
     * {@code GalleryApp} and is an instance of {@code MenuBar}.
     */
    public FileMenu() {
        super();
        MenuItem menu2 = new MenuItem("Exit");
        menu1.getItems().add(menu2);
        this.getMenus().add(menu1);
        EventHandler<ActionEvent> handler = e -> Platform.exit();
        menu2.setOnAction(handler);
    }

}
