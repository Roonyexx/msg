package com.rnxmsg;

import java.util.Vector;

public class GroupChat implements Chat
{
    private String uuid;
    private String title;
    private Vector<ChatMessage> messages;
    private Vector<User> users;

    public GroupChat(String uuid, String title)
    {
        this.uuid = uuid;
        this.title = title;
        this.messages = new Vector<>();
        this.users = new Vector<>();
    }

    @Override
    public String getId() 
    {
        return uuid;
    }

    @Override
    public String getTitle() 
    {
        return title;
    }

    @Override
    public ChatMessage getLastMessage() 
    {
        if (messages != null && !messages.isEmpty()) 
        {
            ChatMessage lastMsg = messages.lastElement();
            return lastMsg;
        }
        return null;
    }

    @Override
    public void addMessage(ChatMessage msg)
    {
        messages.add(msg);
    }

    public void addUser(User user) 
    {
        users.add(user);
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
