package com.rnxmsg;


public interface Chat 
{
    public String getId();
    public String getTitle();
    public ChatMessage getLastMessage();
    public void addMessage(ChatMessage msg);
}
