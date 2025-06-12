package com.rnxmsg;

import org.json.JSONObject;

public class MessageReceiver extends Thread
{
    private NetworkClient client;
    private volatile boolean running = true;
    private EventHandlerDispatcher handlers;

    public MessageReceiver(NetworkClient client)
    {
        this.client = client;
        handlers = new EventHandlerDispatcher();
    }

    public void run()
    {
        while(running)
        {
            try
            {
                String response = client.receiveMessage();
                handleServerResponse(response);
            }
            catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void handleServerResponse(String response)
    {
        JSONObject obj = new JSONObject(response);
        handlers.dispatch(obj);
    }
}
