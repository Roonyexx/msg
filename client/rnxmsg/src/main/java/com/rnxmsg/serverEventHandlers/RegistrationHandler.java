package com.rnxmsg.serverEventHandlers;

import org.json.JSONObject;

import com.rnxmsg.App;

public class RegistrationHandler implements ServerEventHandler 
{

    @Override
    public void handle(JSONObject response) 
    {
        if("success".equals(response.optString("status")))
        {
            App.mainUser.setId(response.optString("message"));
        }
        else
        {

        }
    }
    
}
