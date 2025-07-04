package com.rnxmsg.serverEventHandlers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

import com.rnxmsg.App;
import com.rnxmsg.Chat;
import com.rnxmsg.ChatMessage;
import com.rnxmsg.User;
import com.rnxmsg.controlers.MainController;

public class NewMessageHandler implements ServerEventHandler {

    @Override
    public void handle(JSONObject response) {
        
        
        String chatId = response.optString("chat_id");
        String fromUserId = response.optString("from_user_id");
        String username = response.optString("username");
        String content = response.optString("content");
        String messageId = response.optString("message_id");
        
        Chat chat = App.chatList.get(chatId);
        if (chat == null) {
            new Thread(() -> App.getSender().getUserChats(App.mainUser.getId())).start();
        }



        ChatMessage msg = new ChatMessage(
            Long.parseLong(messageId),
            content,
            new User(fromUserId, username), 
            OffsetDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        );

        chat.addMessage(msg);
        
        if (App.currentChat != null && App.currentChat.getId().equals(chatId)) {
            MainController controller = MainController.getInstance();
            if (controller != null) {
                javafx.application.Platform.runLater(controller::showMessagesForCurrentChat);
            }
        }
    }
    
}
