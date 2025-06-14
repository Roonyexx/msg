package com.rnxmsg;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import org.json.JSONObject;

import com.rnxmsg.serverEventHandlers.GetUserChats;
import com.rnxmsg.serverEventHandlers.LoginHandler;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    public static User mainUser;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void setCurrentUser(User user)
    {
        mainUser = user;
    }

    public static void main(String[] args) {
        try
        {
            NetworkClient client = new NetworkClient("192.168.1.174", 5001);
            MessageSender sender = new MessageSender(client);
            MessageReceiver receiver = new MessageReceiver(client);
            receiver.start();
            mainUser = new User();
            mainUser.setUsername("alice");
            sender.sendLogin("alice", "123456");
            sender.getUserChats(mainUser.getId());

        }
        catch(Exception e) { e.printStackTrace(); }
        launch();
        System.out.println("f");
    }

}