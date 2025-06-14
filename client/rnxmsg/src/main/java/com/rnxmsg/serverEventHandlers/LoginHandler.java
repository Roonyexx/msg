package com.rnxmsg.serverEventHandlers;

import org.json.JSONObject;

import com.rnxmsg.App;

public class LoginHandler implements ServerEventHandler 
{
    @Override
    public void handle(JSONObject response) 
    {
        if("success".equals(response.optString("status")))
        {
            App.mainUser.setId(response.optString("message"));
            // ui обновить
            System.out.println("login successful");
        }

        else
        {
            //вывести сообщение о ошибке.
        }

    }
}
