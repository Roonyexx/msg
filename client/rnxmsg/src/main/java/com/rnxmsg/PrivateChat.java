package com.rnxmsg;

import java.util.Vector;

public class PrivateChat implements Chat {

    private String uuid;
    private Vector<ChatMessage> messages;
    private User chatWith;

    @Override
    public String getId() 
    {
        return uuid;
    }

    @Override
    public String getTitle() 
    {
        return chatWith != null ? chatWith.getUsername() : "";
    }

    @Override
    public ChatMessage getLastMessage() 
    {
        if (messages != null && !messages.isEmpty()) 
        {
            return messages.lastElement();
        }
        return null;
    }

    @Override
    public void addMessage(ChatMessage msg) 
    {
        messages.add(msg);
    }
}
