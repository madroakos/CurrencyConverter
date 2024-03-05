module com.madroakos.currencyconverter {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.json;


    opens com.madroakos.currencyconverter to javafx.fxml;
    exports com.madroakos.currencyconverter;
}