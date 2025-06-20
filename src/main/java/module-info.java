module org.sillylabs {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens org.sillylabs to javafx.fxml;
    exports org.sillylabs;
    exports org.sillylabs.gui;
    opens org.sillylabs.gui to javafx.fxml;
    exports org.sillylabs.pieces;
    opens org.sillylabs.pieces to javafx.fxml;
}