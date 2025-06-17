package com.rnxmsg.serverEventHandlers;

import org.json.JSONObject;

public class DefaultHendler implements ServerEventHandler {

    @Override
    public void handle(JSONObject response) {
        System.out.println(response);
    }
    
}
