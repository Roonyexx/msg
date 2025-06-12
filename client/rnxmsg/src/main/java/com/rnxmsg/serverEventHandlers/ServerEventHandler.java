package com.rnxmsg.serverEventHandlers;

import org.json.JSONObject;

public interface ServerEventHandler {
    void handle(JSONObject response);
}