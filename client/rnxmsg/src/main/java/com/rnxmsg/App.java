package com.rnxmsg;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.*;
import java.io.IOException;


/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    public static User mainUser;
    public static Map<String, Chat> chatList = new java.util.concurrent.ConcurrentHashMap<>();
    public static Chat currentChat;
    private static MessageReceiver receiver;
    private static NetworkClient client;
    private static MessageSender sender;


    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"), 640, 480);
        stage.setScene(scene);
        stage.show();
        scene.getStylesheets().add(App.class.getResource("/com/rnxmsg/styles/app.css").toExternalForm());

        // после запуска UI — запускаем сеть
        new Thread(() -> {
            try {
                client = new NetworkClient("192.168.1.174", 5001);
                sender = new MessageSender(client);
                receiver = new MessageReceiver(client);
                receiver.start();

                App.mainUser = new User();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void setRoot(String fxml) throws IOException {
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

    public static MessageSender getSender()
    {
        return sender;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (receiver != null && receiver.isAlive()) {
            receiver.interrupt();
            client.close();
        }
    }



    public static void main(String[] args) {
        launch();
    }

}