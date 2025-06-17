package com.rnxmsg;

import java.security.Timestamp;

public class ChatMessage 
{
    private long id;
    private String content;
    private String time;
    private User user;
    
    
    public ChatMessage(long id, String content, User user, String time)
    {
        this.id = id;
        this.content = content;
        this.user = user;
        this.time = time;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public String getContent()
    {
        return content;
    }

    public String getTimestamp()
    {
        return time;
    }

    public User getUser()
    {
        return user;
    } 
}
