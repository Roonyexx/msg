package com.rnxmsg.serverEventHandlers;

import org.json.JSONArray;
import org.json.JSONObject;
import com.rnxmsg.ChatMessage;
import com.rnxmsg.User;
import com.rnxmsg.controlers.MainController;

import javafx.application.Platform;

import com.rnxmsg.App;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class GetMessagesHandler implements ServerEventHandler {

    @Override
    public void handle(JSONObject response) {
        if ("success".equals(response.optString("status"))) {
            JSONArray messages = response.optJSONArray("messages");
            Vector<ChatMessage> messageList = new Vector<>();
            if (messages != null) {
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject msgObj = messages.getJSONObject(i);
                    long id = msgObj.optLong("id", 0);
                    String content = msgObj.optString("content", "");
                    String timestamp = msgObj.optString("timestamp", "");
                    timestamp = timestamp.replace(" ", "T");
                    OffsetDateTime dateTime = OffsetDateTime.parse(timestamp);
                    ZonedDateTime localTime = dateTime.atZoneSameInstant(ZoneId.systemDefault());
                    String formattedTime = localTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                    JSONObject userObj = msgObj.optJSONObject("user");
                    User user = null;
                    if (userObj != null) {
                        user = new User(userObj.optString("user_id", ""), 
                                        userObj.optString("username", ""));
                    }
                    ChatMessage chatMessage = new ChatMessage(id, content, user, formattedTime);
                    messageList.add(chatMessage);
                }
            }
            
            App.currentChat.setMessages(messageList);
            MainController controller = MainController.getInstance();
            Platform.runLater(controller::showMessagesForCurrentChat);
        } else {
            System.out.println("Ошибка загрузки сообщений: " + response.optString("message"));
        }
    }
}