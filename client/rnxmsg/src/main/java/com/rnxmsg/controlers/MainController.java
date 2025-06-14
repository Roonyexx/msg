package com.rnxmsg.controlers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.rnxmsg.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MainController {
    private static MainController instance;
    
    public MainController() {
        instance = this;
    }
    public static MainController getInstance() {
        return instance;
    }

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Chat> chatListView;

    @FXML
    private Label chatTitleLabel;

    @FXML
    private ListView<ChatMessage> messageListView;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    public void initialize() {

        chatListView.setItems(FXCollections.observableArrayList());
        // показываем название чата в списке
        chatListView.setCellFactory(listView -> new ListCell<Chat>() {
            @Override
            protected void updateItem(Chat chat, boolean empty) {
                super.updateItem(chat, empty);
                setText((empty || chat == null) ? null : chat.getTitle());
            }
        });

        chatListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldChat, newChat) -> onChatSelected(newChat)
        );

        // cell factory для пузырей сообщений
        messageListView.setCellFactory(listView -> new ListCell<ChatMessage>() {
            @Override
            protected void updateItem(ChatMessage msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    boolean isMine = msg.getUser() != null && msg.getUser().getId().equals(App.mainUser.getId());

                    Label nameLabel = new Label(msg.getUser() != null ? msg.getUser().getUsername() : "");
                    nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");
                    Label contentLabel = new Label(msg.getContent());
                    contentLabel.setWrapText(true);
                    contentLabel.setStyle("-fx-background-color: " + (isMine ? "#cce5ff" : "#f1f0f0") + ";"
                            + "-fx-padding: 8 12 8 12; -fx-background-radius: 12; -fx-font-size: 14px;"
                            + "-fx-text-fill: #222;");
                    Label timeLabel = new Label(msg.getTimestamp());
                    timeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #aaa;");

                    VBox bubble = new VBox(nameLabel, contentLabel, timeLabel);
                    bubble.setSpacing(2);

                    HBox wrapper = new HBox(bubble);
                    wrapper.setMaxWidth(listView.getWidth() - 40);
                    wrapper.setFillHeight(false);
                    wrapper.setStyle("-fx-padding: 4;");

                    if (isMine) {
                        wrapper.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    } else {
                        wrapper.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    }

                    setGraphic(wrapper);
                    setText(null);
                }
            }
        });
    }

    @FXML
    private void handleSendButton() {
        String message = messageField.getText();
        if (message != null && !message.isEmpty()) {
            // Создаём ChatMessage для текущего пользователя
            ChatMessage chatMessage = new ChatMessage(
                System.currentTimeMillis(), // или другой id
                message,
                App.mainUser,
                java.time.LocalTime.now().toString() // или другое время
            );
            messageListView.getItems().add(chatMessage);
            messageField.clear();
        }
    }

    public void updateChatListUI() {
        chatListView.getItems().setAll(App.chatList.values());
    }

    private void onChatSelected(Chat newChat) {
        if (newChat != null) {
            chatTitleLabel.setText(newChat.getTitle());
            App.currentChat = newChat;
            if (newChat.getMessages() == null || newChat.getMessages().isEmpty()) {
                new Thread(() -> App.getSender().getChatHistory(newChat.getId())).start();
            } else {
                messageListView.setItems(FXCollections.observableArrayList(newChat.getMessages()));
            }
        } else {
            chatTitleLabel.setText("Выберите чат");
            messageListView.setItems(FXCollections.observableArrayList());
            App.currentChat = null;
        }
    }

    public void showMessagesForCurrentChat() {
        if (App.currentChat != null) {
            ObservableList<ChatMessage> items = FXCollections.observableArrayList(App.currentChat.getMessages());
            messageListView.setItems(items);
            if (!items.isEmpty()) {
                Platform.runLater(() -> messageListView.scrollTo(items.size() - 1));
            }
        } else {
            messageListView.setItems(FXCollections.observableArrayList());
        }
    }
}