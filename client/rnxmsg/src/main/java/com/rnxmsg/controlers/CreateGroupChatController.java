package com.rnxmsg.controlers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import com.rnxmsg.*;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class CreateGroupChatController {

    @FXML
    private TextField chatNameField;

    @FXML
    private ListView<User> usersListView;

    @FXML
    private Button createButton;

    @FXML
    private Button cancelButton;

    @FXML
    public void initialize() {
        // Получаем пользователей из приватных чатов
        List<User> users = App.chatList.values().stream()
            .filter(chat -> chat instanceof PrivateChat)
            .map(chat -> {
                PrivateChat pc = (PrivateChat) chat;
                return pc.getOtherUser();
            })
            .filter(user -> user != null && !user.getId().equals(App.mainUser.getId()))
            .distinct()
            .collect(Collectors.toList());

        usersListView.setItems(FXCollections.observableArrayList(users));
        usersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        usersListView.setCellFactory(listView -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText((empty || user == null) ? null : user.getUsername());
            }
        });

        createButton.setOnAction(e -> handleCreate());
        cancelButton.setOnAction(e -> handleCancel());
    }

    private void handleCreate() {
        String chatName = chatNameField.getText();
        List<User> selectedUsers = usersListView.getSelectionModel().getSelectedItems();
        if (chatName == null || chatName.isEmpty() || selectedUsers.isEmpty()) {
            // Можно показать предупреждение
            return;
        }
        Vector<String> userIds = new Vector<>();
        userIds.add(App.mainUser.getId());
        for (User user : selectedUsers) {
            userIds.add(user.getId());
        }
        new Thread(() -> {
            App.getSender().createChat(chatName, "group", userIds);
            Platform.runLater(() -> {
                try {
                    App.setRoot("mainwindow");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }).start();
    }

    private void handleCancel() {
        try {
            App.setRoot("mainwindow");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}