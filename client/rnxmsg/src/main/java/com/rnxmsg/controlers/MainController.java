package com.rnxmsg.controlers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

import com.rnxmsg.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;

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
    private ListView<User> userSearchListView;

    @FXML
    private Button createGroupChatButton;

    @FXML
    public void initialize() {

        userSearchListView.setCellFactory(listView -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText((empty || user == null) ? null : user.getUsername());
            }
        });

        // обработка выбора пользователя
        userSearchListView.setOnMouseClicked(event -> {
            User selected = userSearchListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                createPrivateChatWithUser(selected);
                userSearchListView.setVisible(false);
                userSearchListView.setManaged(false);
                searchField.clear();
            }
        });

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.trim().isEmpty()) {
                new Thread(() -> App.getSender().searchUsers(newText.trim())).start();
            } else {
                userSearchListView.setVisible(false);
                userSearchListView.setManaged(false);
            }
        });



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
        messageListView.setCellFactory(listView -> {
            ListCell<ChatMessage> cell = new ListCell<>() {
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
                        contentLabel.setStyle("-fx-background-color: " + (isMine ? "#1888fe" : "#e8e8ea") + ";"
                                + "-fx-padding: 8 12 8 12; -fx-background-radius: 12; -fx-font-size: 14px;"
                                + "-fx-text-fill: " + (isMine ? "#ffffff" : "#000000") + ";");
                        Label timeLabel = new Label(msg.getTimestamp());
                        timeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #aaa;");

                        VBox bubble = new VBox(nameLabel, contentLabel, timeLabel);
                        bubble.setSpacing(2);
                        bubble.setMaxWidth(30); 
                        bubble.setMinWidth(Region.USE_PREF_SIZE);
                        bubble.setPrefWidth(Region.USE_COMPUTED_SIZE);

                        if (isMine) {
                            bubble.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
                        } else {
                            bubble.setAlignment(javafx.geometry.Pos.TOP_LEFT);
                        }

                        HBox wrapper = new HBox(bubble);
                        wrapper.setFillHeight(false);
                        wrapper.setStyle("-fx-padding: 4;");

                        if (isMine) {
                            wrapper.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
                            wrapper.setPadding(new javafx.geometry.Insets(0, 10, 0, 40)); 
                        } else {
                            wrapper.setAlignment(javafx.geometry.Pos.TOP_LEFT);
                            wrapper.setPadding(new javafx.geometry.Insets(0, 40, 0, 10)); 
                        }

                        setGraphic(wrapper);
                        setText(null);
                    }
                }

            };
            // Контекстное меню
            ContextMenu contextMenu = new ContextMenu();

            MenuItem deleteForMe = new MenuItem("Удалить только для себя");
            deleteForMe.setOnAction(event -> {
                ChatMessage message = cell.getItem();
                if (message != null) {
                    App.currentChat.getMessages().removeIf(msg -> msg.getId() == message.getId());
                    showMessagesForCurrentChat();
                    new Thread(() -> App.getSender().deteteMessageLocal(
                        message.getId(),
                        App.mainUser.getId(),
                        App.currentChat.getId()
                    )).start();
                }
            });

            MenuItem deleteGlobal = new MenuItem("Удалить для всех");
            deleteGlobal.setOnAction(event -> {
                ChatMessage msg = cell.getItem();
                if (msg != null && msg.getUser() != null && msg.getUser().getId().equals(App.mainUser.getId())) {
                    new Thread(() -> App.getSender().deleteMessageGlobal(
                        msg.getId(),
                        App.currentChat.getId()
                    )).start();
                }
            });

            contextMenu.getItems().addAll(deleteForMe, deleteGlobal);

            cell.setOnContextMenuRequested(event -> {
                if (!cell.isEmpty() && cell.getItem() != null) {

                    deleteGlobal.setDisable(
                        cell.getItem().getUser() == null ||
                        !cell.getItem().getUser().getId().equals(App.mainUser.getId())
                    );
                    contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                }
            });

            cell.setOnMouseClicked(event -> {
                if (contextMenu.isShowing()) contextMenu.hide();
            });

            return cell;
        });

        createGroupChatButton.setOnAction(e -> {
            try {
                App.setRoot("create_group_chat");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        if (!App.chatList.isEmpty()) {
            updateChatListUI();
        }
    }

    @FXML
    private void handleSendButton() {
        String message = messageField.getText();
        if (message != null && !message.isEmpty()) {
            ChatMessage chatMessage = new ChatMessage(
                System.currentTimeMillis(), 
                message,
                App.mainUser,
                OffsetDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            );
            App.currentChat.addMessage(chatMessage);
            new Thread(() -> App.getSender().sendMessage(App.mainUser.getId(), 
                App.currentChat.getId(), 
                chatMessage)).start();
            showMessagesForCurrentChat();
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

    public void showUserSearchResults(List<User> users) {
        userSearchListView.setItems(FXCollections.observableArrayList(users));
        userSearchListView.setVisible(true);
        userSearchListView.setManaged(true);
    }

    private void createPrivateChatWithUser(User user) {
        Vector<String> userIds = new Vector<>();
        userIds.add(App.mainUser.getId());
        userIds.add(user.getId());
        new Thread(() -> App.getSender().createChat(user.getUsername(), "private", userIds)).start();
    }
}