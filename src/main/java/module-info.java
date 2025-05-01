module de.mherbst.funkplotter {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires org.jetbrains.annotations;

    opens de.mherbst.funkplotter to javafx.fxml;
    exports de.mherbst.funkplotter;
}