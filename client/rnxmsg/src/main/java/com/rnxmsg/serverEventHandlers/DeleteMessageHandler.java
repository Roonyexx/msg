package com.rnxmsg.serverEventHandlers;

import org.json.JSONObject;
import com.rnxmsg.App;
import com.rnxmsg.Chat;
import com.rnxmsg.controlers.MainController;
import javafx.application.Platform;

public class DeleteMessageHandler implements ServerEventHandler {
    @Override
    public void handle(JSONObject response) {
        String chatId = response.optString("chat_id");
        String messageId = response.optString("message_id");
        Chat chat = App.chatList.get(chatId);
        if (chat != null) {
            chat.getMessages().removeIf(msg -> String.valueOf(msg.getId()).equals(messageId));
            if (App.currentChat != null && App.currentChat.getId().equals(chatId)) {
                MainController controller = MainController.getInstance();
                if (controller != null) {
                    Platform.runLater(controller::showMessagesForCurrentChat);
                }
            }
        }
    }
}