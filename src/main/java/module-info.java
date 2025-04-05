module de.mherbst.funkplotter {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;

    opens de.mherbst.funkplotter to javafx.fxml;
    exports de.mherbst.funkplotter;
}