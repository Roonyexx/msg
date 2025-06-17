package com.rnxmsg.controlers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.rnxmsg.App;


public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    public void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> handleRegister());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            // добавить вывод ошибки пользователю
            return;
        }
        new Thread(() -> {
            App.getSender().sendLogin(username, password);
        }).start();

        App.mainUser.setUsername(username);
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            //  добавить вывод ошибки пользователю
            return;
        }
        new Thread(() -> {
            App.getSender().sendRegister(username, password);
        }).start();

        App.mainUser.setUsername(username);
    }
}