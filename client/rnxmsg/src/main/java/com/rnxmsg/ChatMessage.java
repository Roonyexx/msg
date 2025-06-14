package com.rnxmsg;

import java.security.Timestamp;

public class ChatMessage 
{
    private long id;
    private String content;
    private Timestamp time;
    
    
    public ChatMessage(long id, String content, Timestamp time)
    {
        this.id = id;
        this.content = content;
        this.time = time;
    }

    public long getId()
    {
        return id;
    }

    public String getContent()
    {
        return content;
    }

    public Timestamp getTimestamp()
    {
        return time;
    }
}
