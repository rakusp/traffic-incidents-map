package traffic.map;

import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.sothawo.mapjfx.event.MarkerEvent;
import com.sothawo.mapjfx.offline.OfflineCache;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traffic.api.GetData;
import traffic.api.Incident;
import traffic.triplet.Triplet;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    // coordinates
    private static final Coordinate coordWarsaw = new Coordinate(52.237049, 21.017532);
    private static final Coordinate coordPalace = new Coordinate(52.231667, 21.006389);

    // default zoom level
    private static final int ZOOM_DEFAULT = 14;

    // markers
    private HashMap<String, Triplet<Incident, Marker, CoordinateLine>> markerMap = new HashMap<>();
    private HashMap<String, Incident> markerToIncident = new HashMap<>();

    private Extent oldExtent;

    // map
    @FXML
    private MapView mapView;

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

    @FXML
    private Button clearButton;

    @FXML
    private HBox zoomControl;

    @FXML
    private Button zoomIn;

    @FXML
    private Button zoomOut;

    @FXML
    private ToggleButton updateButton;

    @FXML
    private StackPane stackPane;

    private DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    @FXML
    private TitledPane incidentInfo;
    @FXML
    private Text incidentType;
    @FXML
    private Text incidentFrom;
    @FXML
    private Text incidentTo;
    @FXML
    private Text incidentRoad;
    @FXML
    private Text incidentStart;
    @FXML
    private Text incidentEnd;


    // parameters for XYZ server
    private XYZParam xyzParams = new XYZParam()
            .withUrl("https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x})")
            .withAttributions(
                    "'Tiles &copy; <a href=\"https://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer\">ArcGIS</a>'");

    public Controller() {
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

        mapView.addEventHandler(MapViewEvent.MAP_BOUNDING_EXTENT, event -> {
            event.consume();
            labelExtent.setText(event.getExtent().toString());
            if(newExtentBigger(event.getExtent())) {
                logger.info("extent bounds expanded");
                oldExtent = event.getExtent();
                if(!updateButton.isSelected()) updateMarkers(oldExtent);
            }
        });

        mapView.addEventHandler(MarkerEvent.MARKER_CLICKED, event -> {
            event.consume();
            Incident incident = markerToIncident.get(event.getMarker().getId());
            incidentInfo.setExpanded(true);
            incidentType.setText("Incident: " + incident.properties.events[0].description);
            incidentFrom.setText("From: " + incident.properties.from);
            incidentTo.setText("To: " + incident.properties.to);
            incidentRoad.setText("Road numbers: " + incident.properties.roadNumbers.toString()
                    .replace("[","").replace("]",""));
            incidentStart.setText("Start time: " + incident.properties.startTime.format(format));
            incidentEnd.setText("End time: " + incident.properties.endTime.format(format));
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
        mapView.setCenter(coordPalace);

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
    }

    public void addMarkers(Extent extent) {
        double minLat = extent.getMin().getLatitude() - 0.05;
        double minLon = extent.getMin().getLongitude() - 0.05;
        double maxLat = extent.getMax().getLatitude() + 0.05;
        double maxLon = extent.getMax().getLongitude() + 0.05;
        if((maxLat - minLat) * (maxLon - minLon) > 1) {
            logger.info("area for loading is too big");
            return; // too many square kilometers
        }
        logger.info("loading incidents for minLat:" + minLat + " minLon:" + minLon + " maxLat:" + maxLat + " maxLon:" + maxLon);
        List<Incident> incidents = GetData.get(minLon, minLat, maxLon, maxLat);
        assert incidents != null; // TODO do something about this
        for (Incident incident:incidents) {
            if(markerMap.containsKey(incident.properties.id)) continue;
            Marker marker;
            List<Coordinate> coordList = Arrays.stream(incident.geometry.coordinates)
                    .map(d -> new Coordinate(d[1], d[0])).collect(Collectors.toList());
            CoordinateLine line = new CoordinateLine(coordList).setVisible(true);
            switch (incident.properties.events[0].iconCategory) {
                case 0:
                    marker = new Marker(getClass().getResource("/img/other.png"), -16, -16);
                    line.setColor(Color.BLUE);
                    break;
                case 1:
                    marker = new Marker(getClass().getResource("/img/accident.png"), -16, -16);
                    line.setColor(Color.RED);
                    break;
                case 2:
                    marker = new Marker(getClass().getResource("/img/fog-lamp.png"), -16, -16);
                    line.setColor(Color.ORANGE);
                    break;
                case 3:
                    marker = new Marker(getClass().getResource("/img/caution.png"), -16, -16);
                    line.setColor(Color.ORANGE);
                    break;
                case 4:
                    marker = new Marker(getClass().getResource("/img/rainy.png"), -16, -16);
                    line.setColor(Color.YELLOWGREEN);
                    break;
                case 5:
                    marker = new Marker(getClass().getResource("/img/ice.png"), -16, -16);
                    line.setColor(Color.ORANGE);
                    break;
                case 6:
                    marker = new Marker(getClass().getResource("/img/traffic-jam.png"), -16, -16);
                    line.setColor(Color.ORANGERED);
                    break;
                case 7:
                    marker = new Marker(getClass().getResource("/img/lane-closed.png"), -16, -16);
                    line.setColor(Color.RED);
                    break;
                case 8:
                    marker = new Marker(getClass().getResource("/img/road-closed.png"), -16, -16);
                    line.setColor(Color.RED);
                    break;
                case 9:
                    marker = new Marker(getClass().getResource("/img/road-work.png"), -16, -16);
                    line.setColor(Color.RED);
                    break;
                case 10:
                    marker = new Marker(getClass().getResource("/img/wind.png"), -16, -16);
                    line.setColor(Color.YELLOWGREEN);
                    break;
                case 11:
                    marker = new Marker(getClass().getResource("/img/flood.png"), -16, -16);
                    line.setColor(Color.RED);
                    break;
                case 14:
                    marker = new Marker(getClass().getResource("/img/brokendown.png"), -16, -16);
                    line.setColor(Color.YELLOWGREEN);
                    break;
                default:
                    return;
            }
            int med = Math.floorDiv(incident.geometry.coordinates.length, 2);
            Coordinate markCoord = new Coordinate(incident.geometry.coordinates[med][1], incident.geometry.coordinates[med][0]);
            marker.setPosition(markCoord).setVisible(true);

            markerMap.put(incident.properties.id, new Triplet<>(incident, marker, line));
            mapView.addCoordinateLine(line);
            mapView.addMarker(marker);
            markerToIncident.put(marker.getId(), incident);
        }
    }

    private boolean newExtentBigger(Extent newExtent) {
        if(oldExtent == null) return true;
        int point = 10;
        double minLat = Math.round(oldExtent.getMin().getLatitude()*point);
        double minLon = Math.round(oldExtent.getMin().getLongitude()*point);
        double maxLat = Math.round(oldExtent.getMax().getLatitude()*point);
        double maxLon = Math.round(oldExtent.getMax().getLongitude()*point);

        double minLat2 = Math.round(newExtent.getMin().getLatitude()*point);
        double minLon2 = Math.round(newExtent.getMin().getLongitude()*point);
        double maxLat2 = Math.round(newExtent.getMax().getLatitude()*point);
        double maxLon2 = Math.round(newExtent.getMax().getLongitude()*point);

        return !(new Rectangle2D(minLon, maxLat, maxLon - minLon, maxLat - minLat)
                .contains(new Rectangle2D(minLon2, maxLat2, maxLon2 - minLon2, maxLat2 - minLat2)));
    }

    private void updateMarkers(Extent extent) {
        logger.info("updating markers...");
        Platform.runLater(() -> addMarkers(extent));
    }

    @FXML
    private void applyFilters() {
        logger.info("applying filters...");
    }

    @FXML
    private void clearMap() {
        logger.info("clearing map...");
        markerMap.forEach((id, t) -> {mapView.removeMarker(t.getSecond()); mapView.removeCoordinateLine(t.getThird());});
        markerMap.clear();
        markerToIncident.clear();
        logger.info("map cleared");
    }

    @FXML
    private void updateToggle() {
        updateButton.setText("Marker updates: " + (updateButton.isSelected()? "OFF" : "ON"));
    }
}
