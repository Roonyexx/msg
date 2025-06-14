package com.rnxmsg.serverEventHandlers;

import org.json.JSONObject;

import com.rnxmsg.App;

public class LoginHandler implements ServerEventHandler 
{
    @Override
    public void handle(JSONObject response) 
    {
        if(response.optString("status") == "success")
        {
            App.mainUser.setId(response.optString("message"));
            // ui обновить
        }

        else
        {
            //вывести сообщение о ошибке.
        }

    }
}
