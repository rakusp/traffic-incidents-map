package traffic.map;

import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MarkerEvent;
import com.sothawo.mapjfx.offline.OfflineCache;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;


public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    // coordinates
    private static final Coordinate coordWarsaw = new Coordinate(52.237049, 21.017532);
    private static final Coordinate coordPalace = new Coordinate(52.231667, 21.006389);

    // default zoom level
    private static final int ZOOM_DEFAULT = 14;

    @FXML
    private Text incidentType;

    // markers
    private ArrayList<Marker> markerList = new ArrayList<>();
    private final Marker markerPalace;

    // map
    @FXML
    private MapView mapView;

    @FXML
    private TitledPane incidentInfo;

    // controls on the left
    @FXML
    private VBox leftControls;

    @FXML
    private TitledPane mapControls;

    @FXML
    private TitledPane optionsFilters;

    /** Label to display the current center */
    @FXML
    private Label labelCenter;

    /** Label to display the current extent */
    @FXML
    private Label labelExtent;

    /** Label to display the current zoom */
    @FXML
    private Label labelZoom;

    /** label to display the last event. */
    @FXML
    private Label labelEvent;

    @FXML
    private Button clearButton;

    @FXML
    private HBox zoomControl;

    @FXML
    private Button zoomIn;

    @FXML
    private Button zoomOut;

    @FXML
    private StackPane stackPane;

    // parameters for XYZ server
    private XYZParam xyzParams = new XYZParam()
            .withUrl("https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x})")
            .withAttributions(
                    "'Tiles &copy; <a href=\"https://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer\">ArcGIS</a>'");

    public Controller() {
        // using provided marker
        markerPalace = Marker.createProvided(Marker.Provided.BLUE).setPosition(coordPalace).setVisible(
                true);

        // custom label
        // label
        MapLabel labelPalace = new MapLabel("Palace of Culture and Science",
                10, -10).setVisible(false).setCssClass("green-label");

        markerPalace.attachLabel(labelPalace);
    }

    /**
     * called after the fxml is loaded and all objects are created. This is not called initialize any more,
     * because we need to pass in the projection before initializing.
     *
     * @param projection
     *     the projection to use in the map.
     */
    public void initMapAndControls(Projection projection) {
        logger.debug("begin initialize");

        // init MapView-Cache
        final OfflineCache offlineCache = mapView.getOfflineCache();
        final String cacheDir = System.getProperty("java.io.tmpdir") + "/mapjfx-cache";
//        logger.info("using dir for cache: " + cacheDir);
//        try {
//            Files.createDirectories(Paths.get(cacheDir));
//            offlineCache.setCacheDirectory(cacheDir);
//            offlineCache.setActive(true);
//        } catch (IOException e) {
//            logger.warn("could not activate offline cache", e);
//        }

        // set the custom css file for the MapView
        mapView.setCustomMapviewCssURL(getClass().getResource("/custom_mapview.css"));

        // set the controls to disabled, this will be changed when the MapView is intialized
        setControlsDisable(true);

        // display map's properties
        labelCenter.textProperty().bind(Bindings.format("center: %s", mapView.centerProperty()));
        labelZoom.textProperty().bind(Bindings.format("zoom: %.0f", mapView.zoomProperty()));
        logger.debug("options and labels done");

        // watch the MapView's initialized property to finish initialization
        mapView.initializedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                afterMapIsInitialized();
            }
        });

        // set map type
        mapView.setXYZParam(xyzParams);
        mapView.setMapType(MapType.XYZ);

        setupEventHandlers();

        // initialize the map view
        logger.debug("start map initialization");
        mapView.initialize(Configuration.builder()
                .projection(projection)
                .showZoomControls(false)
                .build());

        // add images
        logger.debug("starting image initialization");
        try {
            initImages();
        } catch(Exception e) {
            logger.warn("FAILED TO LOAD IMAGES");
        }

        // zoom buttons alignment
        StackPane.setAlignment(zoomControl, Pos.TOP_RIGHT);

        logger.debug("initialization finished");
    }

    // initialize event handlers
    private void setupEventHandlers() {

        mapView.addEventHandler(MarkerEvent.MARKER_CLICKED, event -> {
            event.consume();
            incidentInfo.setExpanded(true);
            incidentType.setText("Wypadek");
            labelEvent.setText("Event: marker clicked: " + event.getMarker().getId());
        });
        mapView.addEventHandler(MarkerEvent.MARKER_RIGHTCLICKED, event -> {
            event.consume();
            labelEvent.setText("Event: marker right clicked: " + event.getMarker().getId());
        });

        logger.debug("map handlers initialized");
    }

    // enables/disables control buttons
    private void setControlsDisable(boolean flag) {
        leftControls.setDisable(flag);
    }

    // finish setup
    private void afterMapIsInitialized() {
        logger.debug("map intialized");
        logger.debug("setting center and enabling controls...");
        // starting coordinates and zoom
        mapView.setZoom(ZOOM_DEFAULT);
        mapView.setCenter(coordWarsaw);

        // add markers
        mapView.addMarker(markerPalace);

        // zoom listener
        mapView.zoomProperty().addListener((observableValue, oldZoom, currZoom) -> {
            if((double)currZoom < (double)oldZoom) updateMarkers();
        }
        );


        // enable the controls
        setControlsDisable(false);
    }

    private void initImages() {
        File file = new File(getClass().getResource("/img/filter.png").getFile());
        Image image = new Image(file.toURI().toString(), 12, 12, false, true);
        ImageView iv = new ImageView(image);
        optionsFilters.setGraphic(iv);
        optionsFilters.setContentDisplay(ContentDisplay.RIGHT);

        file = new File(getClass().getResource("/img/clear.png").getFile());
        image = new Image(file.toURI().toString(), 15, 15, false, true);
        iv = new ImageView(image);
        iv.setFitWidth(10);
        clearButton.setGraphic(iv);
        clearButton.setContentDisplay(ContentDisplay.RIGHT);

        file = new File(getClass().getResource("/img/control.png").getFile());
        image = new Image(file.toURI().toString(), 15, 15, false, true);
        iv = new ImageView(image);

        mapControls.setGraphic(iv);
        mapControls.setContentDisplay(ContentDisplay.RIGHT);

        file = new File(getClass().getResource("/img/plus.png").getFile());
        image = new Image(file.toURI().toString(), 23, 25, false, true);
        iv = new ImageView(image);
        zoomIn.setGraphic(iv);

        file = new File(getClass().getResource("/img/minus.png").getFile());
        image = new Image(file.toURI().toString(), 23, 25, false, true);
        iv = new ImageView(image);
        zoomOut.setGraphic(iv);
    }

    @FXML
    private void raiseOpacity() {
        zoomIn.setOpacity(0.8);
        zoomOut.setOpacity(0.8);
    }

    @FXML
    private void lowerOpacity() {
        zoomIn.setOpacity(0.5);
        zoomOut.setOpacity(0.5);
    }

    @FXML
    private void zoomIn() {
        mapView.setZoom(mapView.getZoom() + 1);
    }

    @FXML
    private void zoomOut() {
        mapView.setZoom(mapView.getZoom() - 1);
        updateMarkers();
    }

    public void addMarker() {
        Coordinate testCoord = new Coordinate(52.25, 21.006389);
        Marker testMarker = Marker.createProvided(Marker.Provided.RED).setPosition(testCoord).setVisible(
                true);
//        markerList.add(testMarker);
        mapView.addMarker(testMarker);
        logger.info("marker added");
    }


    private void updateMarkers() {
        logger.info("updating markers...");
    }

    @FXML
    private void applyFilters() {
        logger.info("applying filters...");
    }

    @FXML
    private void clearMap() {
        logger.info("clearing map...");
    }
}
