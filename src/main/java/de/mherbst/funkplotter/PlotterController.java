package de.mherbst.funkplotter;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.transform.*;
import javafx.stage.Stage;

import java.net.*;
import java.util.*;

public class PlotterController implements Initializable {

    @FXML
    public BorderPane rootPane;
//    @FXML
//    public SplitPane splitPane;
    @FXML
    public AnchorPane anchorPaneLeft;
    @FXML
    public AnchorPane anchorPaneRight;
    @FXML
    public Canvas canvas;
    @FXML
    public Label statusBar;

    // --- Zoom and Pan State ---
    private double scale = 50.0; // Pixels per mathematical unit (initial zoom)
    private double originX = 0.0; // Canvas X pixel coordinate of the mathematical origin (0,0)
    private double originY = 0.0; // Canvas Y pixel coordinate of the mathematical origin (0,0)
    private Affine transform = new Affine ( ); // Transformation matrix

    // --- Panning State ---
    private double lastMouseX, lastMouseY;
    private boolean mouseDragging = false;

    public ContextMenu canvasContextMenu;

    // --- Coordinate Transformation ---

    private void updateTransform () {
        transform = new Affine ( );
        transform.appendTranslation ( originX, originY );
        transform.appendScale ( scale, -scale ); // Use -scale for Y to invert axis
    }

    private Point2D canvasToMath ( double canvasX, double canvasY ) {
        try {
            return transform.inverseTransform ( canvasX, canvasY );
        } catch (NonInvertibleTransformException e) {
            System.err.println ( "Error inverting transform: " + e.getMessage ( ) );
            return new Point2D ( 0, 0 ); // Or handle error appropriately
        }
    }

    private Point2D mathToCanvas ( double mathX, double mathY ) {

        return transform.transform ( mathX, mathY );
    }

    // --- Event Handlers Implementation ---

    @FXML
    public void mouseOnCanvasMoved ( MouseEvent event ) {
        Point2D mathCoords = canvasToMath ( event.getX ( ), event.getY ( ) );
        String mousePos = String.format ( "Canvas: (%d | %d) | Math: (%.2f | %.2f)",
                ( int ) event.getX ( ), ( int ) event.getY ( ),
                mathCoords.getX ( ), mathCoords.getY ( ) );
        statusBar.setText ( mousePos );
    }

    @FXML
    public void mouseOnCanvasPressed ( MouseEvent event ) {
        if ( event.isPrimaryButtonDown ( ) ) {
            // Record mouse position relative to the scene/stage for consistent dragging
            lastMouseX = event.getSceneX ( );
            lastMouseY = event.getSceneY ( );
            mouseDragging = true;
            canvas.getScene ( ).setCursor ( javafx.scene.Cursor.MOVE ); // Change cursor
        }
    }

    @FXML
    public void mouseOnCanvasDragged ( MouseEvent event ) {
        if ( mouseDragging && event.isPrimaryButtonDown ( ) ) {
            double currentMouseX = event.getSceneX ( );
            double currentMouseY = event.getSceneY ( );

            double deltaX = currentMouseX - lastMouseX;
            double deltaY = currentMouseY - lastMouseY;

            // Adjust the origin based on mouse movement
            originX += deltaX;
            originY += deltaY;

            lastMouseX = currentMouseX;
            lastMouseY = currentMouseY;

            updateTransform ( ); // Update transform after panning
            updateVisibleArea ( ); // Redraw after panning
        }
    }

    @FXML
    public void mouseOnCanvasReleased ( MouseEvent event ) {
        if ( mouseDragging && event.getButton ( ) == MouseButton.PRIMARY ) {
            mouseDragging = false;
            canvas.getScene ( ).setCursor ( javafx.scene.Cursor.DEFAULT ); // Restore cursor
        }
    }

    @FXML
    public void mouseOnCanvasClicked ( MouseEvent event ) {
        MouseButton button = event.getButton ( );
        switch (button) {
            case PRIMARY:
                // System.out.println("Left mouse button clicked");
                canvasContextMenu.hide ( );
                break;
            case MIDDLE:
                // System.out.println("Middle mouse button clicked!");
                canvasContextMenu.hide ( );
                break;
            case SECONDARY:
                // System.out.println("Right mouse button clicked!");
                canvasContextMenu.show ( canvas, event.getScreenX ( ), event.getScreenY ( ) );
                break;
            default:
                canvasContextMenu.hide ( );
                break;
        }
    }

    // --- Zoom Handling ---
    private void handleZoom ( ScrollEvent event ) {
        double zoomFactor = ( event.getDeltaY ( ) > 0 ) ? 1.1 : 1 / 1.1; // 10% zoom steps
        double mouseX = event.getX ( ); // Mouse X relative to canvas
        double mouseY = event.getY ( ); // Mouse Y relative to canvas

        // Get mathematical coordinates under the mouse BEFORE zoom
        Point2D mathCoordsBefore = canvasToMath ( mouseX, mouseY );

        // Apply zoom factor to scale
        scale *= zoomFactor;

        // Update the transformation matrix with the new scale
        updateTransform ( );

        // Get canvas coordinates for the same mathematical point AFTER zoom
        Point2D canvasCoordsAfter = mathToCanvas ( mathCoordsBefore.getX ( ), mathCoordsBefore.getY ( ) );

        // Calculate the difference (how much the point shifted on canvas)
        double deltaX = mouseX - canvasCoordsAfter.getX ( );
        double deltaY = mouseY - canvasCoordsAfter.getY ( );

        // Adjust the origin to counteract the shift, keeping the point under the mouse
        originX += deltaX;
        originY += deltaY;

        // Final update of the transformation matrix with the adjusted origin
        updateTransform ( );

        // Redraw the canvas
        updateVisibleArea ( );

        event.consume ( ); // Consume the event to prevent default scroll behavior
    }


    // --- Drawing Logic ---

    public void updateVisibleArea () {
        GraphicsContext gc = canvas.getGraphicsContext2D ( );
        double width = canvas.getWidth ( );
        double height = canvas.getHeight ( );
        //System.out.println ( "updateVisibleArea called. Canvas size: " + width + "x" + height + ", Origin: " + originX + "," + originY + ", Scale: " + scale ); // <-- ADDED

        if ( width <= 0 || height <= 0 ) {
            System.err.println ( "!!! WARNING: updateVisibleArea called with zero dimensions. Skipping draw." ); // <-- ADDED
            return;
        }

        // Clear the entire canvas (since it now matches the viewport)
        gc.clearRect ( 0, 0, width, height );

        // Draw components
        drawAxes ( gc, width, height );
        drawFunction ( gc, width, height );
    }

    public void drawAxes ( GraphicsContext gc, double canvasWidth, double canvasHeight ) {
        gc.save ( ); // Save current state (like default transform)
        gc.setStroke ( Color.GRAY );
        gc.setLineWidth ( 1.0 );

        // --- Calculate Visible Axis Positions on Canvas ---
        // Clamp the origin's canvas coordinates to the visible canvas bounds
        double visibleAxisY = Math.max ( 1, Math.min ( canvasHeight - 1, originY ) );  // 1, -1 to see the axis at the edge
        double visibleAxisX = Math.max ( 1, Math.min ( canvasWidth - 1, originX ) );   // 1, -1 to see the axis at the edge

        // Calculate mathematical bounds of the visible area
        Point2D topLeftMath = canvasToMath ( 0, 0 );
        Point2D bottomRightMath = canvasToMath ( canvasWidth, canvasHeight );
        double minMathX = topLeftMath.getX ( );
        double maxMathX = bottomRightMath.getX ( );
        double minMathY = bottomRightMath.getY ( ); // Y is inverted
        double maxMathY = topLeftMath.getY ( );     // Y is inverted

        // --- Grid Lines ---
        gc.setStroke ( Color.LIGHTGRAY );
        gc.setLineWidth ( 0.5 );

        // Determine appropriate grid spacing based on scale
        // (This is a simple example, more sophisticated logic might be needed)
        double gridStep = 1.0;
        if ( scale < 10 ) gridStep = 5;
        if ( scale < 5 ) gridStep = 10;
        if ( scale > 100 ) gridStep = 0.5;
        if ( scale > 200 ) gridStep = 0.25;
        // Add more steps as needed

        // Vertical grid lines
        double startX = Math.ceil ( minMathX / gridStep ) * gridStep;
        for (double mathX = startX; mathX <= maxMathX; mathX += gridStep) {
            if ( Math.abs ( mathX ) < 1e-9 ) continue; // Skip axis line if origin is visible
            Point2D canvasP = mathToCanvas ( mathX, 0 ); // Get canvas X for this math X
            gc.strokeLine ( canvasP.getX ( ), 0, canvasP.getX ( ), canvasHeight );
            // Draw label (only if reasonably spaced and axis is visible)
            if ( scale * gridStep > 25 ) {
                gc.setFill ( Color.DARKGRAY );
                // Position label relative to the VISIBLE Y-axis position, clamped slightly from top edge
                gc.fillText ( String.format ( "%.2g", mathX ), canvasP.getX ( ) + 3, Math.max ( 15, visibleAxisY - 5 ) );
            }
        }

        // Horizontal grid lines
        double startY = Math.ceil ( minMathY / gridStep ) * gridStep;
        for (double mathY = startY; mathY <= maxMathY; mathY += gridStep) {
            if ( Math.abs ( mathY ) < 1e-9 ) continue; // Skip axis line if origin is visible
            Point2D canvasP = mathToCanvas ( 0, mathY ); // Get canvas Y for this math Y
            gc.strokeLine ( 0, canvasP.getY ( ), canvasWidth, canvasP.getY ( ) );
            // Draw label (only if reasonably spaced and axis is visible)
            if ( scale * gridStep > 20 ) {
                gc.setFill ( Color.DARKGRAY );
                // Position label relative to the VISIBLE X-axis position, clamped slightly from right edge
                gc.fillText ( String.format ( "%.2g", mathY ), Math.min ( canvasWidth - 30, visibleAxisX + 5 ), canvasP.getY ( ) - 3 );
            }
        }

        // --- Main Axes ---
        gc.setStroke ( Color.BLACK );
        gc.setLineWidth ( 2.0 );
        gc.setFill ( Color.BLACK ); // For labels and arrows

        // X-Axis (Draw at visibleAxisY)
        gc.strokeLine ( 0, visibleAxisY, canvasWidth, visibleAxisY );
        // Draw arrow near the right edge, positioned vertically by visibleAxisY
        drawArrow ( gc, canvasWidth, visibleAxisY, canvasWidth, visibleAxisY );
        // Draw 'X' label near the arrow, clamped slightly from top edge
        gc.fillText ( "X", canvasWidth - 25, Math.max ( 15, visibleAxisY - 5 ) );


        // Y-Axis (Draw at visibleAxisX)
        gc.strokeLine ( visibleAxisX, 0, visibleAxisX, canvasHeight );
        // Draw arrow near the top edge, positioned horizontally by visibleAxisX
        drawArrow ( gc, visibleAxisX, 10, visibleAxisX, 0 );
        // Draw 'Y' label near the arrow, clamped slightly from right edge
        gc.fillText ( "Y", Math.min ( canvasWidth - 15, visibleAxisX + 5 ), 20 );

        gc.restore ( ); // Restore original state
    }

    public void drawFunction ( GraphicsContext gc, double canvasWidth, double canvasHeight ) {
        gc.save ( );
        gc.setStroke ( Color.RED );
        gc.setLineWidth ( 1.5 ); // Slightly thicker line

        // Calculate mathematical range to plot based on canvas width
        Point2D startMath = canvasToMath ( 0, 0 );
        Point2D endMath = canvasToMath ( canvasWidth, 0 ); // Only need X range

        double minMathX = startMath.getX ( );
        double maxMathX = endMath.getX ( );

        boolean firstPoint = true;
        double lastCanvasX = 0, lastCanvasY = 0;

        // Iterate through canvas pixels horizontally for smoothness
        for (double canvasX = 0; canvasX <= canvasWidth; canvasX += 1) {
            // Convert canvas pixel X to mathematical X
            Point2D currentMath = canvasToMath ( canvasX, 0 ); // Y doesn't matter here
            double mathX = currentMath.getX ( );

            // Calculate mathematical Y using the function
            double mathY = Math.sin ( mathX ); // Your function here!

            // Convert mathematical Y back to canvas pixel Y
            Point2D currentCanvas = mathToCanvas ( mathX, mathY );
            double canvasY = currentCanvas.getY ( );

            // Draw line segment
            if ( firstPoint ) {
                firstPoint = false;
            } else {
                // Basic clipping: Only draw if both points are roughly within bounds
                // More robust clipping might be needed for extreme zooms/pans
                if ( !Double.isNaN ( lastCanvasY ) && !Double.isNaN ( canvasY ) &&
                        lastCanvasY > -canvasHeight * 2 && lastCanvasY < canvasHeight * 2 && // Generous bounds
                        canvasY > -canvasHeight * 2 && canvasY < canvasHeight * 2 ) {
                    gc.strokeLine ( lastCanvasX, lastCanvasY, canvasX, canvasY );
                }
            }
            lastCanvasX = canvasX;
            lastCanvasY = canvasY;
        }
        gc.restore ( );
    }

    // --- Helper Methods ---

    private void drawArrow ( GraphicsContext gc, double x1, double y1, double x2, double y2 ) {
        // Keep this method as it is, it works on canvas coordinates
        gc.save ( );
        double arrowSize = 8; // Slightly smaller arrow
        double angle = Math.atan2 ( y2 - y1, x2 - x1 );

        double xA = x2 - arrowSize * Math.cos ( angle - Math.PI / 6 );
        double yA = y2 - arrowSize * Math.sin ( angle - Math.PI / 6 );
        double xB = x2 - arrowSize * Math.cos ( angle + Math.PI / 6 );
        double yB = y2 - arrowSize * Math.sin ( angle + Math.PI / 6 );

        gc.setFill ( Color.BLACK );
        gc.fillPolygon ( new double[]{x2, xA, xB}, new double[]{y2, yA, yB}, 3 );
        gc.restore ( );
    }

    public void initializeContextMenu () {
        // Keep this method as is
        canvasContextMenu = new ContextMenu ( );
        MenuItem clearItem = new MenuItem ( "Grafik" ); // Rename? Maybe "Reset View"?
        CheckMenuItem drawGridItem = new CheckMenuItem ( "Koordinatengitter" );
        drawGridItem.setSelected ( true ); // Default to grid on
        MenuItem infoItem = new MenuItem ( "Info" );

        clearItem.setOnAction ( event -> {
            // Reset zoom and pan
            originX = canvas.getWidth ( ) / 2;
            originY = canvas.getHeight ( ) / 2;
            scale = 50.0; // Reset scale
            updateTransform ( );
            updateVisibleArea ( );
        } );

        // Add listener to redraw when grid visibility changes
        drawGridItem.selectedProperty ( ).addListener ( ( obs, oldVal, newVal ) -> updateVisibleArea ( ) );

        infoItem.setOnAction ( event -> System.out.println ( "Info action triggered" ) );
        canvasContextMenu.getItems ( ).addAll ( clearItem, drawGridItem, infoItem );

        // Modify drawAxes to check drawGridItem.isSelected() if you want to toggle grid
        // (Currently, grid is always drawn)
    }

    @FXML
    public void menuItemExitOnAction () {
        // Get the stage from any node within the scene controlled by this controller
        Stage stage = ( Stage ) rootPane.getScene ( ).getWindow ( ); // rootPane is your BorderPane @FXML

        if ( stage != null ) {
            // Option 2 (Better): Handle the confirmation directly here
            Alert alert = new Alert ( Alert.AlertType.CONFIRMATION );
            alert.initOwner ( stage ); // Associate alert with the main window
            alert.setTitle ( "Exit Confirmation" );
            alert.setHeaderText ( "Exit Funktionsplotter?" );
            alert.setContentText ( "Do you want to save before exiting?" ); // Add save logic if needed

            if ( alert.showAndWait ( ).orElse ( ButtonType.CANCEL ) == ButtonType.OK ) {
                System.out.println ( "Exiting via Controller..." );
                // Perform any controller-specific cleanup if needed
                stage.close ( ); // Directly close the stage
            } else {
                System.out.println ( "Exit cancelled via Controller." );
            }
        } else {
            System.err.println ( "Could not get Stage to exit." );
        }
    }

    // --- Initialization ---
    @Override
    public void initialize ( URL location, ResourceBundle resources ) {

        // Make canvas resize with its container
        canvas.widthProperty ( ).bind ( anchorPaneRight.widthProperty ( ) );
        canvas.heightProperty ( ).bind ( anchorPaneRight.heightProperty ( ) );

        // Initial centering of the origin
        // Wait until the layout is done, use a listener on bounds
        anchorPaneRight.layoutBoundsProperty ( ).addListener ( ( observable, oldBounds, newBounds ) -> {

            double canvasWidth = canvas.getWidth ( );
            double canvasHeight = canvas.getHeight ( );

            // Initialize origin only once WHEN the canvas size is first known and non-zero
            if ( originX == 0.0 && originY == 0.0 && canvasWidth > 0 && canvasHeight > 0 ) { // Initialize origin only once
                originX = canvasWidth / 2;
                originY = canvasHeight / 2;
            }

            if ( canvasWidth > 0 && canvasHeight > 0 ) {
                updateTransform ( );        // Update transform when viewport changes
                updateVisibleArea ( );      // Redraw when viewport size changes
            }
        } );

        // --- Event Handlers ---
        canvas.setOnMouseMoved ( this::mouseOnCanvasMoved );
        canvas.setOnMousePressed ( this::mouseOnCanvasPressed );
        canvas.setOnMouseDragged ( this::mouseOnCanvasDragged );
        canvas.setOnMouseReleased ( this::mouseOnCanvasReleased );
        canvas.setOnMouseClicked ( this::mouseOnCanvasClicked );
        canvas.setOnScroll ( this::handleZoom ); // Use setOnScroll for zoom

        initializeContextMenu ( );
    }
}
