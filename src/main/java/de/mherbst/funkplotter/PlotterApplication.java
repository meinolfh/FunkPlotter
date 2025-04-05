package de.mherbst.funkplotter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * JavaFX Application Entry Point
 */
public class PlotterApplication extends Application {

    public static final PlotterApplication getInstance = new PlotterApplication ( );

    public static Stage primaryStage;

    /**
     * @param stage the primary stage for this application, onto which
     *              the application scene can be set.
     *              Applications may create other stages, if needed, but they will not be
     *              primary stages.
     */
    @Override
    public void start ( Stage stage ) throws IOException {
        primaryStage = stage;

        final URL fxmlRes = this.getClass ( ).getResource ( "PlotterView.fxml" );
        final URL cssRes = this.getClass ( ).getResource ( "PlotterView.css" );
        primaryStage = stage;
        final FXMLLoader fxmlLoader = new FXMLLoader ( fxmlRes );
        final Parent root = fxmlLoader.load ( );
        //final PlotterController controller = fxmlLoader.getController ( );
        final Scene scene = new Scene ( root, PlotterConstants.APPL_WIDTH, PlotterConstants.APPL_HEIGHT );

        if ( cssRes != null ) {

            root.getScene ( ).getStylesheets ( ).add ( Objects.requireNonNull ( cssRes ).toExternalForm ( ) );
        }

        /*
        stage.setFullScreen ( true );
        stage.setFullScreenExitHint ( "please press q to exit" );
        stage.setFullScreenExitKeyCombination ( KeyCombination.valueOf ( "q" ) );
        */

        stage.setTitle ( "Funktionsplotter" );
        stage.setScene ( scene );
        stage.show ( );

        stage.setOnCloseRequest ( event -> {
            event.consume ( );
            logout ( stage );
        } );
    }

    /**
     * logout from application
     *
     * @param stage as Stage
     */
    public void logout ( Stage stage ) {
        Alert alert = new Alert ( Alert.AlertType.CONFIRMATION );
        alert.setTitle ( "Logout" );
        alert.setHeaderText ( "You are about to logout!" );
        alert.setContentText ( "Do you want to save before exiting?" );

        if ( alert.showAndWait ( ).get ( ) == ButtonType.OK ) {
            stage.close ( );
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main ( String[] args ) {

        launch ( args );
    }
}