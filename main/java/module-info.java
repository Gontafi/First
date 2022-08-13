module project2.p2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.media;
    requires java.desktop;

    opens project2.p2 to javafx.fxml;
    exports project2.p2;
}