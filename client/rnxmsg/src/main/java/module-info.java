module com.rnxmsg {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;

    opens com.rnxmsg to javafx.fxml;
    opens com.rnxmsg.controlers to javafx.fxml;
    exports com.rnxmsg;
}
