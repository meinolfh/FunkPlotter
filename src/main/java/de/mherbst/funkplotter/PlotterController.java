package de.mherbst.funkplotter;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mherbst.funkplotter.PlotterConstants.*;

public class PlotterController implements Initializable {

    @FXML
    public BorderPane rootPane;

    @FXML
    public SplitPane splitPane;

    @FXML
    public AnchorPane anchorPaneLeft;

    @FXML
    public AnchorPane anchorPaneRight;

    @FXML
    public ScrollPane scrollPane;

    @FXML
    public Canvas canvas;

    @FXML
    public Label statusBar;

    private double lastMouseX, lastMouseY;
    private boolean mouseDragging = false;

    @FXML
    public void canvasOnMouseMoved ( MouseEvent event ) {
        String mousePos = String.format ( "MousePos: (%d | %d)", ( int ) event.getX ( ), ( int ) event.getY ( ) );
        statusBar.setText ( mousePos );
    }

    @FXML
    public void canvasOnMousePressed ( MouseEvent event ) {
        if ( event.isPrimaryButtonDown ( ) ) {
            System.out.println ( "Left mouse button clicked => Maus On Canvas pressed" );
            lastMouseX = event.getSceneX ( );
            lastMouseY = event.getSceneY ( );
            mouseDragging = true;
        }
    }

    @FXML
    public void canvasOnMouseDragged ( MouseEvent event ) {
        if ( mouseDragging ) {
            double deltaX = lastMouseX - event.getSceneX ( );
            double deltaY = lastMouseY - event.getSceneY ( );

            scrollPane.setHvalue ( scrollPane.getHvalue ( ) + deltaX / canvas.getWidth ( ) );
            scrollPane.setVvalue ( scrollPane.getVvalue ( ) + deltaY / canvas.getHeight ( ) );

            lastMouseX = event.getSceneX ( );
            lastMouseY = event.getSceneY ( );
        }
    }

    @FXML
    public void canvasOnMouseReleased ( MouseEvent event ) {
        mouseDragging = false;
    }

    @FXML
    public void canvasOnMouseClicked ( MouseEvent event ) {
        MouseButton button = event.getButton ( );
        switch (button) {
            case PRIMARY:
                System.out.println ( "Left mouse button clicked => Maus On Canvas clicked" );
                canvasContextMenu.hide ( );
                break;
            case MIDDLE:
                System.out.println ( "Middle mouse button clicked!" );
                canvasContextMenu.hide ( );
                break;
            case SECONDARY:
                System.out.println ( "Right mouse button clicked!" );
                canvasContextMenu.show ( canvas, event.getScreenX ( ), event.getScreenY ( ) );
                break;
//            default:
//                System.out.println ( "Other mouse button clicked!" );
//                canvasContextMenu.hide ( );
//                break;
        }
    }

    @FXML
    public void menuItemExitOnAction () {
        PlotterApplication plotterApplication = PlotterApplication.getInstance;
        plotterApplication.logout ( PlotterApplication.primaryStage );
    }

    public static final PlotterController getInstance = new PlotterController ( );

    public ContextMenu canvasContextMenu;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize ( URL location, ResourceBundle resources ) {

        // Set the desired canvas width and height
        canvas.setWidth ( CANVAS_WIDTH );
        canvas.setHeight ( CANVAS_HEIGHT );

        scrollPane.setHvalue ( 0.5 );
        scrollPane.setVvalue ( 0.5 );

        scrollPane.viewportBoundsProperty ( ).addListener ( ( observable, oldBounds, newBounds ) -> {
            updateVisibleArea ( );
        } );

        scrollPane.hvalueProperty ( ).addListener ( ( observable, oldValue, newValue ) -> {
            updateVisibleArea ( );
        } );

        scrollPane.vvalueProperty ( ).addListener ( ( observable, oldValue, newValue ) -> {
            updateVisibleArea ( );
        } );

        initializeContextMenu ( );
        updateVisibleArea ( );
    }

    public void initializeContextMenu () {
        canvasContextMenu = new ContextMenu ( );

        // Create menu items
        MenuItem clearItem = new MenuItem ( "Grafik" );
        CheckMenuItem drawGridItem = new CheckMenuItem ( "Koordinatengitter" );
        MenuItem infoItem = new MenuItem ( "Info" );

        // Add actions to menu items
        clearItem.setOnAction ( event -> {
            //setCanvasGridOff ( );
        } );

        drawGridItem.setOnAction ( event -> {
            if ( drawGridItem.isSelected ( ) ) {
                System.out.println ( "Koordinatengitter enabled" );
                //setCanvasGridOn ( );
            } else {
                System.out.println ( "Koordinatengitter disabled" );
                //setCanvasGridOff ( );
            }
        } );

        infoItem.setOnAction ( event -> System.out.println ( "Info action triggered" ) );

        // Add menu items to the context menu
        canvasContextMenu.getItems ( ).addAll ( clearItem, drawGridItem, infoItem );
    }


    public void updateVisibleArea () {
        Bounds viewportBounds = scrollPane.getViewportBounds ( );

        double viewportX = scrollPane.getHvalue ( ) * ( canvas.getWidth ( ) - viewportBounds.getWidth ( ) );
        double viewportY = scrollPane.getVvalue ( ) * ( canvas.getHeight ( ) - viewportBounds.getHeight ( ) );

        drawAxes ( canvas.getGraphicsContext2D ( ), viewportX, viewportY, viewportBounds.getWidth ( ), viewportBounds.getHeight ( ) );
        drawFunction ( canvas.getGraphicsContext2D ( ), viewportX, viewportY, viewportBounds.getWidth ( ), viewportBounds.getHeight ( ) );
    }

    public void drawAxes ( GraphicsContext gc, double viewportX, double viewportY, double viewportWidth, double viewportHeight ) {
        gc.clearRect ( viewportX, viewportY, viewportWidth, viewportHeight );
        gc.setStroke ( Color.GRAY );
        gc.setLineWidth ( 1.0 );

        double canvasCenterX = canvas.getWidth ( ) / 2;
        double canvasCenterY = canvas.getHeight ( ) / 2;

        // Sichtbare Grenzen berechnen
        double endX = viewportX + viewportWidth;
        double endY = viewportY + viewportHeight;

        // **X-Achse zeichnen (falls im sichtbaren Bereich)**
        if ( canvasCenterY >= viewportY && canvasCenterY <= endY ) {
            gc.setStroke ( Color.BLACK );
            gc.setLineWidth ( 2 );
            gc.strokeLine ( viewportX, canvasCenterY, endX, canvasCenterY );
            drawArrow ( gc, endX - 10, canvasCenterY, endX, canvasCenterY );
        }

        // **Y-Achse zeichnen (falls im sichtbaren Bereich)**
        if ( canvasCenterX >= viewportX && canvasCenterX <= endX ) {
            gc.setStroke ( Color.BLACK );
            gc.setLineWidth ( 2 );
            gc.strokeLine ( canvasCenterX, viewportY, canvasCenterX, endY );
            drawArrow ( gc, canvasCenterX, viewportY + 10, canvasCenterX, viewportY );
        }

        // **Rasterlinien (jede 50 Pixel)**
        gc.setStroke ( Color.LIGHTGRAY );
        gc.setLineWidth ( 0.5 );

        // Vertikale Linien
        for (double x = canvasCenterX; x < endX; x += 50) {
            gc.strokeLine ( x, viewportY, x, endY );
            gc.fillText ( String.valueOf ( ( int ) ( ( x - canvasCenterX ) / 50 ) ), x + 3, canvasCenterY - 5 );
        }

        for (double x = canvasCenterX; x > viewportX; x -= 50) {
            gc.strokeLine ( x, viewportY, x, endY );
            gc.fillText ( String.valueOf ( ( int ) ( ( x - canvasCenterX ) / 50 ) ), x + 3, canvasCenterY - 5 );
        }

        // Horizontale Linien
        for (double y = canvasCenterY; y < endY; y += 50) {
            gc.strokeLine ( viewportX, y, endX, y );
            gc.fillText ( String.valueOf ( ( int ) ( ( canvasCenterY - y ) / 50 ) ), canvasCenterX + 5, y - 3 );
        }

        for (double y = canvasCenterY; y > viewportY; y -= 50) {
            gc.strokeLine ( viewportX, y, endX, y );
            gc.fillText ( String.valueOf ( ( int ) ( ( canvasCenterY - y ) / 50 ) ), canvasCenterX + 5, y - 3 );
        }

        // **Achsenbeschriftung**
        gc.fillText ( "X", endX - 20, canvasCenterY - 5 ); // X-Achse Label
        gc.fillText ( "Y", canvasCenterX + 5, viewportY + 20 ); // Y-Achse Label
    }

    public void drawFunction ( GraphicsContext gc, double viewportX, double viewportY, double viewportWidth, double viewportHeight ) {
        //gc.clearRect ( viewportX, viewportY, viewportWidth, viewportHeight );
        gc.setStroke ( Color.RED );
        gc.setLineWidth ( 1 );

        double canvasCenterX = canvas.getWidth ( ) / 2;
        double canvasCenterY = canvas.getHeight ( ) / 2;

        double scaleX = 50; // Skalierung der X-Achse
        double scaleY = 50; // Skalierung der Y-Achse

        double endX = viewportX + viewportWidth;

        double lastX = viewportX;
        double lastY = canvasCenterY - Math.sin ( ( viewportX - canvasCenterX ) / scaleX ) * scaleY;

        for (double x = viewportX; x <= endX; x += 1) {
            double y = canvasCenterY - Math.sin ( ( x - canvasCenterX ) / scaleX ) * scaleY;
            gc.strokeLine ( lastX, lastY, x, y );
            lastX = x;
            lastY = y;
        }
    }

    private void drawArrow ( GraphicsContext gc, double x1, double y1, double x2, double y2 ) {
        double arrowSize = 8; // Größe des Pfeils
        double angle = Math.atan2 ( y2 - y1, x2 - x1 ); // Richtung berechnen

        double xA = x2 - arrowSize * Math.cos ( angle - Math.PI / 6 );
        double yA = y2 - arrowSize * Math.sin ( angle - Math.PI / 6 );

        double xB = x2 - arrowSize * Math.cos ( angle + Math.PI / 6 );
        double yB = y2 - arrowSize * Math.sin ( angle + Math.PI / 6 );

        gc.setFill ( Color.BLACK );
        gc.fillPolygon ( new double[]{x2, xA, xB}, new double[]{y2, yA, yB}, 3 );
    }
}