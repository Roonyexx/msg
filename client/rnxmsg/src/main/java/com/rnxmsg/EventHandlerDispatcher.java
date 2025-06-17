package com.rnxmsg;
import com.rnxmsg.serverEventHandlers.*;
import java.util.*;

import org.json.JSONObject;

public class EventHandlerDispatcher
{
    private final Map<String, ServerEventHandler> handlers = new HashMap<>();

    public EventHandlerDispatcher()
    {
        registerAll();
    }

    public void dispatch(JSONObject response)
    {
        String action = response.optString("action");
        ServerEventHandler handler = handlers.get(action);

        if (handler != null)
        {
            handler.handle(response);
        } 
        else
        {
            System.out.println("Action: " + action + " is not registered");
            System.out.println(response.toString());
        }
    } 


    private void register(String name, ServerEventHandler handler)
    {
        if (handlers.containsKey(name)) 
        {
            System.err.println("Handler for action " + name + " is already registered");
            return;
        }
        handlers.put(name, handler);
    }

    private void registerAll()
    {
        register("login", new LoginHandler());
        register("register", new RegistrationHandler());
        register("get_user_chats", new GetUserChats());
        register("get_messages", new GetMessagesHandler());
        register("send_message", new DefaultHendler());
        register("new_message", new NewMessageHandler());
        register("search_users", new SearchUsersHandler());
        register("create_chat", new CreateChatHandler());


    }

}
