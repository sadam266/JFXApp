package sample;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jcodec.api.JCodecException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Adam Rabczuk
 */
public class Main extends Application {
    // Initial parameters
    private static final String MESH_FILE = "CylinderHead-binary.stl";

    private static final double X_SIZE = 600;
    private static final double Y_SIZE = 600;
    private static final double Z_SIZE = 600;

    private static final double X_OFFSET = 0;
    private static final double Y_OFFSET = 0;
    private static final double Z_OFFSET = 0;

    private static final double X_ANGLE = 10;
    private static final double Y_ANGLE = 30;
    private static final double Z_ANGLE = 0;

    private static final double SCALE = 3;

    private Color materialColor = Color.rgb(190, 190, 210);
    private Color lightColor = Color.rgb(240, 240, 240);
    private Color ambientColor = Color.rgb(10, 10, 10);
    private Color backColor = Color.rgb(10, 30, 10);

    // GUI elements
    private Button playButton;

    /**
     * Action event handler for GUI.
     */
    private EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
        boolean playing = false;

        @Override
        public void handle(ActionEvent event) {
            if (event.getSource() == playButton) {
                if (playing) {
                    timeline.pause();
                    playButton.setText("Play");
                    playing = false;
                } else {
                    timeline.play();
                    playButton.setText("Pause");
                    playing = true;
                }
            }
        }
    };

    // Fields
    private Timeline timeline;

    /**
     * Loads mesh view from a given file.
     * @return mesh view
     */
    private MeshView loadMeshView() {
        File file = new File(MESH_FILE);
        StlMeshImporter importer = new StlMeshImporter();
        importer.read(file);
        Mesh mesh = importer.getImport();

        return new MeshView(mesh);
    }

    /**
     * Creates scene for mesh view and sets its initial setup.
     * Adds list of point lights and ambient lights.
     * @return Parent for mesh view and lights
     */
    private Group buildScene() {
        MeshView meshView = loadMeshView();
        setTranslate(meshView);
        setRotate(meshView);
        setScale(meshView);
        setMaterial(meshView);

        List<PointLight> pointLights = new ArrayList<>();
        pointLights.add(addLight(0, 0, 0));
        pointLights.add(addLight(0.75, 0.75, 0.25));

        Group root = new Group();
        root.getChildren().add(0, meshView);
        for (PointLight light : pointLights)    root.getChildren().add(light);
        root.getChildren().add(new AmbientLight(ambientColor));

        return root;
    }

    /**
     * Sets initial position for a given mesh view.
     * @param meshView mesh view
     */
    private void setTranslate(MeshView meshView) {
        meshView.setTranslateX(X_SIZE / 2 + X_OFFSET);
        meshView.setTranslateY(Y_SIZE / 2 + Y_OFFSET);
        meshView.setTranslateZ(Z_SIZE / 2 + Z_OFFSET);
    }

    /**
     * Sets initial rotation for a given mesh view.
     * @param meshView mesh view
     */
    private void setRotate(MeshView meshView) {
        meshView.getTransforms().setAll(
                new Rotate(X_ANGLE, Rotate.X_AXIS),
                new Rotate(Y_ANGLE, Rotate.Y_AXIS),
                new Rotate(Z_ANGLE, Rotate.Z_AXIS)
        );
    }

    /**
     * Sets initial scale for a given mesh view.
     * @param meshView mesh view
     */
    private void setScale(MeshView meshView) {
        meshView.setScaleX(SCALE);
        meshView.setScaleY(SCALE);
        meshView.setScaleZ(SCALE);
    }

    /**
     * Sets material and its properties for a given mesh view.
     * @param meshView mesh view
     */
    private void setMaterial(MeshView meshView) {
        PhongMaterial material = new PhongMaterial(materialColor);
        material.setSpecularColor(lightColor);
        material.setSpecularPower(5);
        meshView.setMaterial(material);
    }

    /**
     * Sets point light position and color.
     * @param xFactor factor of values between 0 and 1, sets position along x axis
     * @param yFactor factor of values between 0 and 1, sets position along y axis
     * @param zFactor factor of values between 0 and 1, sets position along z axis
     * @param color color
     * @return point light
     */
    private PointLight addLight(double xFactor, double yFactor, double zFactor, Color color) {
        PointLight light = new PointLight(color);
        light.setTranslateX(X_SIZE * xFactor);
        light.setTranslateY(Y_SIZE * yFactor);
        light.setTranslateZ(Z_SIZE * zFactor);
        return light;

    }

    /**
     * Sets point light position with default color.
     * @param xFactor factor of values between 0 and 1, sets position along x axis
     * @param yFactor factor of values between 0 and 1, sets position along y axis
     * @param zFactor factor of values between 0 and 1, sets position along z axis
     * @return point light
     */
    private PointLight addLight(double xFactor, double yFactor, double zFactor) {
        return addLight(xFactor, yFactor, zFactor, lightColor);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Group root = buildScene();
        setProperties(root);
        setGUI(root);

        Scene scene = new Scene(root, X_SIZE, Y_SIZE, true);
        scene.setFill(backColor);
        scene.setCamera(new PerspectiveCamera());

        primaryStage.setTitle("STL Mesh View");
        primaryStage.setScene(scene);
        primaryStage.show();

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().addAll(
                new KeyFrame(
                        Duration.millis(9000),
                        new KeyValue(root.getChildren().get(0).rotateProperty(), -360)
                )
        );
    }

    /**
     * Initiates all GUI elements and adds them to a parent.
     * Sets parameters for each element and adds action
     * event handler to each element.
     * @param root parent for GUI nodes
     */
    private void setGUI(Group root) {
        playButton = new Button("Play");
        playButton.setOnAction(eventHandler);
        playButton.setPrefSize(80, 40);
        playButton.setTranslateX(X_SIZE / 2 - 40);
        playButton.setTranslateY(Y_SIZE - 40);
        root.getChildren().add(playButton);
    }

    /**
     * Sets properties of children of given parent for timeline animation.
     * @param root parent
     */
    private void setProperties(Group root) {
        root.getChildren().get(0).setRotationAxis(Rotate.Y_AXIS);
    }

    @SuppressWarnings("unused")
    private PerspectiveCamera addCamera(Scene scene) {
        PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
        scene.setCamera(perspectiveCamera);
        return perspectiveCamera;
    }

    public static void main(String[] args) throws IOException, JCodecException {
        launch(args);
    }
}
