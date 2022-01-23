package traffic.map;

import com.sothawo.mapjfx.Projection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveMap extends Application {

    private static final Logger logger = LoggerFactory.getLogger(InteractiveMap.class);

    public static void main(String[] args) {
        logger.trace("begin main");
        launch(args);
        logger.trace("end main");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("starting InteractiveMap");
        String fxmlFile = "/fxml/InteractiveMap.fxml";
        logger.debug("loading fxml file {}", fxmlFile);
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(InteractiveMap.class.getResource(fxmlFile));
        Parent rootNode = fxmlLoader.load();
        logger.debug("fxml file loaded successfully");

        final Controller controller = fxmlLoader.getController();
        final Projection projection = getParameters().getUnnamed().contains("wgs84")
                ? Projection.WGS_84 : Projection.WEB_MERCATOR;
        controller.initMapAndControls(projection);


        Scene scene = new Scene(rootNode);
        logger.debug("scene created");

        primaryStage.setTitle("Traffic Incidents Visualization");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);

        logger.debug("showing scene");
        primaryStage.show();

        logger.debug("application start method finished.");
    }
}

