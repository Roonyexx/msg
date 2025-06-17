package com.rnxmsg.serverEventHandlers;

import org.json.JSONArray;
import org.json.JSONObject;
import com.rnxmsg.App;
import com.rnxmsg.Chat;
import com.rnxmsg.GroupChat;
import com.rnxmsg.PrivateChat;
import com.rnxmsg.User;
import com.rnxmsg.controlers.MainController;
import javafx.application.Platform;

public class CreateChatHandler implements ServerEventHandler {
    @Override
    public void handle(JSONObject response) {
        if ("success".equals(response.optString("status"))) {
            JSONObject chatObj = response.optJSONObject("message");
            if (chatObj != null) {
                String chatId = chatObj.optString("chat_id");
                String title = chatObj.optString("title", "");
                String type = chatObj.optString("is_private", "true").equals("true") ? "private" : "group";
                Chat chat;
                if ("group".equals(type)) {
                    chat = new GroupChat(chatId, title);
                } else {
                    // для приватного чата ищем второго пользователя
                    JSONArray users = chatObj.optJSONArray("users");
                    User otherUser = null;
                    if (users != null) {
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject userObj = users.getJSONObject(i);
                            if (!userObj.optString("user_id").equals(App.mainUser.getId())) {
                                otherUser = new User(userObj.optString("user_id"), userObj.optString("username"));
                                break;
                            }
                        }
                    }
                    chat = new PrivateChat(chatId, otherUser);
                }
                App.chatList.put(chatId, chat);

                // обновить UI
                MainController controller = MainController.getInstance();
                if (controller != null) {
                    Platform.runLater(controller::updateChatListUI);
                }
            }
        } else {
            System.out.println("Ошибка создания чата: " + response.optString("message"));
        }
    }
}