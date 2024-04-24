module com.tonki {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;

    requires bluecove;
    requires javafx.graphics;
    

    exports com.tonki; 
    opens com.tonki to javafx.fxml, javafx.graphics; 
}

