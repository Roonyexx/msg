package com.rnxmsg.serverEventHandlers;

import java.io.IOException;

import org.json.JSONObject;

import com.rnxmsg.App;

import javafx.application.Platform;

public class RegistrationHandler implements ServerEventHandler 
{

    @Override
    public void handle(JSONObject response) 
    {
        if("success".equals(response.optString("status")))
        {
            App.mainUser.setId(response.optString("message"));
            Platform.runLater(() -> {
                try {
                    App.setRoot("mainwindow");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        else
        {

        }
    }
    
}
