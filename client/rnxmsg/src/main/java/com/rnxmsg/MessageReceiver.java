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
        while(!Thread.currentThread().isInterrupted())
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
        String action = obj.optString("action", null);
        if (action != null) {
            ResponseWaiter.complete(action, obj);
        }
        handlers.dispatch(obj);
    }

    public void Stop()
    {
        this.running = false;
    }
}
