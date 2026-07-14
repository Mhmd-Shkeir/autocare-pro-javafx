module OOP2_pro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
	requires java.desktop;
	requires itextpdf;
    
    opens application to javafx.base, javafx.fxml;
    exports application;
}

