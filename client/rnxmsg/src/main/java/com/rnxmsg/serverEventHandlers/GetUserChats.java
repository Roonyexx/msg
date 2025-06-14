package com.rnxmsg.serverEventHandlers;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rnxmsg.Chat;
import com.rnxmsg.GroupChat;
import com.rnxmsg.PrivateChat;
import com.rnxmsg.User;
import com.rnxmsg.controlers.MainController;
import com.rnxmsg.App;

public class GetUserChats implements ServerEventHandler{

    @Override
    public void handle(JSONObject response) {
        System.out.println(response.toString());
        if ("success".equals(response.optString("status"))) {
            JSONArray chats = response.optJSONArray("chats");
            if (chats != null) {
                for (int i = 0; i < chats.length(); i++) {
                    JSONObject chatObj = chats.getJSONObject(i);
                    String type = chatObj.optString("chat_type", "private");
                    String uuid = chatObj.optString("chat_id");
                    String title = chatObj.optString("title", "");
                    if ("group".equals(type)) {
                        GroupChat groupChat = new GroupChat(uuid, title);
                        App.chatList.put(groupChat.getTitle(), groupChat);
                    } else {
                        JSONObject userObj = chatObj.optJSONObject("user");
                        User user = null;
                        if (userObj != null) {
                            user = new User(userObj.optString("user_id"), userObj.optString("username"));
                        }
                        PrivateChat privateChat = new PrivateChat(uuid, user);
                        App.chatList.put(privateChat.getTitle(), privateChat);
                    }
                }
            }
            MainController controller = MainController.getInstance();
            if (controller != null) {
                javafx.application.Platform.runLater(controller::updateChatListUI);
            }
        } else {
            System.out.println("Failed to load chats: " + response.optString("message"));
        }
    }
}
