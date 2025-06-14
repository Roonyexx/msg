package com.rnxmsg;

import java.util.Vector;

public class PrivateChat implements Chat {

    private String uuid;
    private Vector<ChatMessage> messages;
    private User chatWith;

    public PrivateChat(String uuid, User chatWith)
    {
        this.uuid = uuid;
        this.chatWith = chatWith;
        messages = new Vector<>();
    }

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

    @Override
    public void setMessages(Vector<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public Vector<ChatMessage> getMessages() {
        return messages;
    }
}
