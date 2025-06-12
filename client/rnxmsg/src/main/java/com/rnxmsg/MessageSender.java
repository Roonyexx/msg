package com.rnxmsg;

import java.util.Vector;
import org.json.JSONObject;

public class MessageSender 
{
    private NetworkClient client;

    public MessageSender(NetworkClient client)
    {
        this.client = client;
    }

    public void sendRegister(String username, String password)
    {
        JSONObject obj = new JSONObject();
        obj.put("action", "register");
        obj.put("username", username);
        obj.put("password", password);
        try { client.sendMessage(obj.toString()); }
        catch(Exception e) { e.printStackTrace(); }
    }

    public void sendLogin(String username, String password)
    {
        JSONObject obj = new JSONObject();
        obj.put("action", "login");
        obj.put("username", username);
        obj.put("password", password);
        try { client.sendMessage(obj.toString()); }
        catch(Exception e) { e.printStackTrace(); }
    }

    public void sendMessage(String userId, String chatId, String content)
    {
        JSONObject obj = new JSONObject();
        obj.put("action", "send_message");
        obj.put("userId", userId);
        obj.put("chatId", chatId);
        obj.put("content", content);
        try { client.sendMessage(obj.toString()); }
        catch(Exception e) { e.printStackTrace(); }
    }

    public void getChatHistory(String chatId)
    {
        JSONObject obj = new JSONObject();
        obj.put("action", "get_messages");
        obj.put("chat_id", chatId);
        try { client.sendMessage(obj.toString()); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void deleteMessageGlobal(long messageId, String chatId)
    {
        JSONObject obj = new JSONObject();
        obj.put("action", "delete_message_global");
        obj.put("chat_id", chatId);
        obj.put("message_id", messageId);
        try { client.sendMessage(obj.toString()); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void deteteMessageLocal(long messageId, String userId, String chatId)
    {
        JSONObject obj = new JSONObject();
        obj.put("action", "delete_message_local");
        obj.put("chat_id", chatId);
        obj.put("message_id", messageId);
        obj.put("user_id", userId);
        try { client.sendMessage(obj.toString()); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void createChat(String chatName, String chatType, Vector<String> chatMembers)
    {
        JSONObject obj = new JSONObject();
        obj.put("action", "create_chat");
        obj.put("chat_name", chatName);
        obj.put("chat_type", chatType);
        obj.put("chat_members", chatMembers);
        try { client.sendMessage(obj.toString()); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void leaveChat(String userId, String chatId)
    {
        JSONObject obj = new JSONObject();
        obj.put("action", "leave_chat");
        obj.put("user_id", userId);
        obj.put("chat_id", chatId);
        try { client.sendMessage(obj.toString()); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void sendChatInvite(String userId, String chatId)
    {
        JSONObject obj = new JSONObject();
        obj.put("action", "chat_invite");
        obj.put("user_id", userId);
        obj.put("chat_id", chatId);
        try { client.sendMessage(obj.toString()); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void getUserChats(String userId)
    {
        JSONObject obj = new JSONObject();
        obj.put("action", "get_user_chats");
        obj.put("user_id", userId);
        try { client.sendMessage(obj.toString()); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
