package com.rnxmsg.serverEventHandlers;

import org.json.*;
import java.util.*;

import com.rnxmsg.User;
import com.rnxmsg.controlers.MainController;
import javafx.application.Platform;

public class SearchUsersHandler implements ServerEventHandler {

    @Override
    public void handle(JSONObject response) {
        if ("success".equals(response.optString("status"))) {
            JSONArray users = response.optJSONArray("users");
            List<User> userList = new ArrayList<>();
            for (int i = 0; i < users.length(); i++) {
                JSONObject userObj = users.getJSONObject(i);
                userList.add(new User(userObj.getString("user_id"), userObj.getString("username")));
            }

            Platform.runLater(() -> {
                MainController controller = MainController.getInstance();
                if (controller != null) {
                    controller.showUserSearchResults(userList);
                }
            });
        }
    }
}