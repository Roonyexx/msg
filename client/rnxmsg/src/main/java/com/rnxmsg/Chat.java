package com.rnxmsg;

import java.util.Vector;

public interface Chat 
{
    public String getId();
    public String getTitle();
    public ChatMessage getLastMessage();
    public void addMessage(ChatMessage msg);
    public void setMessages(Vector<ChatMessage> messages);
    public Vector<ChatMessage> getMessages();
}
