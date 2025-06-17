package com.rnxmsg.serverEventHandlers;

import java.io.IOException;

import org.json.JSONObject;

import com.rnxmsg.App;
import com.rnxmsg.controlers.MainController;

import javafx.application.Platform;

public class LoginHandler implements ServerEventHandler 
{
    @Override
    public void handle(JSONObject response) 
    {
        if("success".equals(response.optString("status")))
        {
            String userId = response.optString("message");
            App.mainUser.setId(userId);
            Platform.runLater(() -> {
                try {
                    App.setRoot("mainwindow");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            new Thread(() -> App.getSender().getUserChats(userId)).start();

            MainController controller = MainController.getInstance();
            if (controller != null) {
                javafx.application.Platform.runLater(controller::updateChatListUI);
            }
        }

        else
        {
            //вывести сообщение о ошибке.
        }

    }
}
