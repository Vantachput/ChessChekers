module org.sillylabs {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens org.sillylabs to javafx.fxml;
    exports org.sillylabs;
}