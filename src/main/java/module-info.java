module com.tonki {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;

    requires bluecove; // Replace 'requires bluecove;' with 'requires bluecove.module.name;'
    requires javafx.graphics;
    

    exports com.tonki; 
    opens com.tonki to javafx.fxml, javafx.graphics; 
}

