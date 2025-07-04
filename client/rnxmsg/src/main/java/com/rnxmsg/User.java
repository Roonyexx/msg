package com.rnxmsg;

public class User 
{
    private String uuid;
    private String username;

    public User(String uuid, String username)
    {
        this.uuid = uuid;
        this.username = username;
    }
    
    public User() { }

    public String getUsername()
    {
        return username;
    }

    public String getId() 
    {
        return uuid;
    }

    public void setId(String uuid)
    {
        this.uuid = uuid;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}
