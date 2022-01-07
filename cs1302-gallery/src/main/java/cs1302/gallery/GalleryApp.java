package cs1302.gallery;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.net.URLEncoder;
import java.io.InputStreamReader;
import javafx.scene.text.Text;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import java.util.Random;
import javafx.stage.Screen;
import javafx.util.Duration;
import javafx.scene.control.ProgressBar;
import cs1302.gallery.FileMenu;

/**
 * Represents an iTunes GalleryApp.
 */
public class GalleryApp extends Application {

    // default height and width
    private static final int DEF_HEIGHT = 100;
    private static final int DEF_WIDTH = 100;

    // instance variables
    HBox searchLayer = new HBox();
    TilePane tile = new TilePane();
    Button updateImage;
    TextField searchBar;
    Button playPause;
    Image img;
    List<Image> images = new ArrayList<Image>();
    List<Image> extraImages;
    Random rand = new Random();
    Timeline timeline = new Timeline();
    KeyFrame keyFrame;
    ProgressBar progressBar = new ProgressBar(0);
    FileMenu menu;
    int count = 0;
    String searchTerm = "pop";

    /**
     * {@inheritdoc}
     * An override of the start method that is in {@code Application}, and
     * this puts all of the components into the main app.
     */
    @Override
    public void start(Stage stage) {
        // application scene
        VBox app = new VBox(10);

        // menu
        menu = new FileMenu();

        // search bar
        searchBar = new TextField(searchTerm);
        updateImage = new Button("Update Images");
        playPause = new Button("Pause");
        playPause.setDisable(true);
        searchLayer.getChildren().addAll(playPause, searchBar, updateImage);
        searchLayer.setHgrow(searchBar, Priority.ALWAYS);
        Runnable r = () -> loadImage();
        EventHandler<ActionEvent> loadHandler = e -> runNow(r);
        updateImage.setOnAction(loadHandler);

        // creating timeline
        EventHandler<ActionEvent> playHandler = event -> switchImages();
        keyFrame = new KeyFrame(Duration.seconds(2), playHandler);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyFrame);
        Runnable t = () -> playImage();
        EventHandler<ActionEvent> switchHandler = e -> runNow(t);
        playPause.setOnAction(switchHandler);

        // images
        tile.setPrefColumns(5);
        tile.setPrefRows(4);
        this.queryToImages(searchTerm);

        // progress bar
        HBox bottom = new HBox(10);
        Text citation = new Text("Images provided courtesy of iTunes");
        bottom.getChildren().addAll(progressBar, citation);

        // adding to app
        app.getChildren().addAll(menu, searchLayer, tile, bottom);

        // setting up scene
        Scene scene = new Scene(app, 500, 510);
        stage.setMaxWidth(1280);
        stage.setMaxHeight(720);
        stage.setTitle("GalleryApp!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start

    /**
     * A method that reads in a URl and displays the image from that URL.
     */
    public void loadImage() {
        // error catching
        if (searchBar.getText().equals(searchTerm)) {
            searchBar.clear();
            searchBar.setText("This is currently displayed!");
            searchTerm = searchBar.getText();
        } else if (searchBar.getText().equals("")) {
            searchBar.clear();
            searchBar.setText("Type something to be searched!");
            this.disableSearch();
        } else if (searchBar.getText().equals("The search query did not procure enough images.")
            || searchBar.getText().equals("Try another search!")
                || searchBar.getText().equals("Type something to be searched!")) {
            searchBar.clear();
            searchBar.setText("Try another search!");
            this.disableSearch();
        } else {
            searchTerm = searchBar.getText();
            if (images.isEmpty() == false && extraImages.isEmpty() == false) {
                images.clear();
                extraImages.clear();
                this.resetSearchBar();
                timeline.pause();
            }
            this.queryToImages(searchTerm);
        }
    } // loadImage

    /**
     * A method that switches a random image in the main area
     * with an unused one.
     */
    public void switchImages() {
        int randImage = rand.nextInt(20);
        int randExtraImage = rand.nextInt(extraImages.size());
        Image tempImage = images.get(randImage);
        images.set(randImage, extraImages.get(randExtraImage));
        extraImages.remove(randExtraImage);
        extraImages.add(tempImage);
        this.changeDisplay();
    } // switchImages

    /**
     * A method that will randomly replace an image in the
     * main content area with an extra one when the user uses
     * the Pause/Play Button.
     */
    public void playImage() {
        if (playPause.getText().equals("Play")) {
            Platform.runLater(() -> playPause.setText("Pause"));
            Platform.runLater(() -> this.switchImages());
            timeline.play();
        } else if (playPause.getText().equals("Pause")) {
            Platform.runLater(() -> playPause.setText("Play"));
            timeline.pause();
        }
    } // playImages

    /**
     * Creates and immediately starts a new daemon thread that executes
     * {@code target.run()}. This method, which may be called from any thread,
     * will return immediately its the caller.
     * @param target the object whose {@code run} method is invoked when this
     *               thread is started
     */
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.setDaemon(true);
        t.start();
    } // runNow

    /**
     * A private method that sets the progres of the progress bar.
     * @param progress the progress of the bar
     */
    private void setProgress(final double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    } // setProgress

    /**
     * A method that edits the current display.
     */
    private void changeDisplay() {
        Platform.runLater(() -> tile.getChildren().clear());
        for (int i = 0; i < 20; i++) {
            ImageView imgView = new ImageView(images.get(i));
            Platform.runLater(() -> tile.getChildren().add(imgView));
        }
    } // changeDisplay

    /**
     * A method that resets the search bar.
     */
    private void resetSearchBar() {
        Platform.runLater(() -> playPause.setText("Pause"));
        playPause.setDisable(false);
        Platform.runLater(() -> progressBar.setProgress(0));
    } // resetSearchBar

    /**
     * A method that resets the search bar.
     */
    private void disableSearch() {
        Platform.runLater(() -> playPause.setText("Play"));
        playPause.setDisable(true);
        Platform.runLater(() -> progressBar.setProgress(0));
    } // disableSearch

    /**
     * A method that takes a query as input, runs it through
     * the iTunes API and produces the resulting images.
     * @param query a query that is turned into images
     */
    private void queryToImages(String query) {
        try {
            playPause.setDisable(true);
            updateImage.setDisable(true);
            String itunesQ = "https://itunes.apple.com/search?term="
                + URLEncoder.encode(query, "UTF-8") +  "&media=music";
            URL url = new URL(itunesQ);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            JsonElement je = JsonParser.parseReader(reader);
            JsonObject root = je.getAsJsonObject();
            JsonArray results = root.getAsJsonArray("results");
            int numResults = results.size();

            // looking at number of results from query and adding to tile
            if (numResults < 21) {
                searchBar.setText("The search query did not procure enough images.");
                this.disableSearch();
            } else {
                for (int i = 0; i < numResults; i++) {
                    count = i;
                    JsonObject result = results.get(i).getAsJsonObject();
                    JsonElement artworkUrl100 = result.get("artworkUrl100");
                    String artUrl = artworkUrl100.getAsString();
                    img = new Image("https://deelay.me/2/" + artUrl, DEF_HEIGHT, DEF_WIDTH, false, false);
                    Platform.runLater(() -> this.setProgress(1.0 * (count + 1) / numResults));
                    if (images.contains(img)) {
                        continue;
                    } else {
                        images.add(img);
                    }
                }
                if (images.size() < 21) {
                    searchBar.setText("The search query did not procure enough images.");
                    this.disableSearch();
                } else {
                    extraImages = new ArrayList<Image>(images.subList(20, images.size()));
                    images.removeAll(extraImages);
                    searchTerm = query;
                    playPause.setDisable(false);
                    this.changeDisplay();
                    timeline.play();
                }
            }
            updateImage.setDisable(false);
        } catch (IOException ioe) {
            System.out.println("just dont happen");
        }
    } // queryToImages

} // GalleryApp
