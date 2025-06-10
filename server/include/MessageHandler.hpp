#pragma once
#include <nlohmann/json.hpp>
#include <string>
#include <iostream>
#include "databaseManage.hpp"

using json = nlohmann::json;


class IMsgEventHandler 
{
public:
    IMsgEventHandler() = default;
    ~IMsgEventHandler() = default;
    virtual json handle(const json& msg) = 0;
};

// сообщение JSON  
// {
//   "action": "register",
//   "username": "",
//   "password": ""
// }
// ответ JSON
// {
//   "status": "",   
//   "message": ""
class RegistrationEventHandler : public IMsgEventHandler
{
public:
    json handle(const json& msg) override 
    {
        json response;
        try
        {
            std::string username { msg.value("username", "") };
            std::string password { msg.value("password", "") };
            auto db = DatabaseManage::getInstance();

            if (username.empty())
            {
                response["status"] = "error";
                response["message"] = "Username cannot be empty";
            }
            else if (password.length() < 6)
            {
                response["status"] = "error";
                response["message"] = "Password should be at least 6 characters long";
            }
            else if (db->isUsernameExists(username))
            {
                response["status"] = "error";
                response["message"] = "Username already exists";
            }
            // если не возникнет проблем с базой данных, createUser вернет его uuid 
            // в противном случае вернет пустую строку
            else if ( (response["message"] = db->createUser(username, password)).empty() )
            {
                response["status"] = "error";
                response["message"] = "Failed to create user in the database";
            }
            else
            {
                response["status"] = "success";
            }
            
        }
        catch(const std::exception& e)
        {
            std::cerr << e.what() << '\n';
            response["status"] = "error";
            response["message"] = e.what();
        }
        
        return response;
    }
};


// сообщение JSON  
// {
//   "action": "login",
//   "username": "",
//   "password": ""
// }
// ответ JSON
// {
//   "status": "",   
//   "message": ""
class LoginEventHandler : public IMsgEventHandler
{
public:
    json handle(const json& msg) override
    { 
        json response;
        try
        {
            std::string username { msg.value("username", "") };
            std::string password { msg.value("password", "") };
            auto db = DatabaseManage::getInstance();

            std::string userId = db->authenticateUser(username, password);
            if (userId.empty())
            {
                response["status"] = "error";
                response["message"] = "Invalid username or password";
            }
            else
            {
                db->updateUserStatus(userId, userStatus::online);
                response["status"] = "success";
                response["user_id"] = userId;
            }

        }
        catch(const std::exception& e)
        {
            std::cerr << e.what() << '\n';
            response["status"] = "error";
            response["message"] = e.what();
        }

        return response;
    }
};


// сообщение JSON  
// {
//   "action": "send_message",
//   "user_id": "",
//   "chat_id": "",
//   "content": ""
// }
// ответ JSON
// {
//   "status": "",   
//   "message": ""
class SendMessageEventHandler : public IMsgEventHandler
{
public:
    json handle(const json& msg) override 
    {
        json response;
        try
        {
            std::string userId { msg.value("user_id", "") };
            std::string chatId { msg.value("chat_id", "") };
            std::string content { msg.value("content", "") };

            auto db = DatabaseManage::getInstance();
            if (db->saveMessage(userId, chatId, content))
            {
                response["status"] = "success";
            }
            else
            {
                response["status"] = "error";
                response["message"] = "Failed to save message in the database";
            }
        }
        catch(const std::exception& e)
        {
            std::cerr << e.what() << '\n';
            response["status"] = "error";
            response["message"] = e.what();
        }

        return response;
    }
};

// сообщение JSON  
// {
//   "action": "get_messages",
//   "chat_id": "",
// }
// ответ JSON
// {
//   "status": "",   
//   "message": ""
class GetMessagesEventHandler : public IMsgEventHandler
{
public:
    json handle(const json& msg) override 
    {
        json response;
        try
        {
            std::string chatId { msg.value("chat_id", "") };
            auto db = DatabaseManage::getInstance();
            auto messages = db->getChatMessages(chatId);

            response["status"] = "success";
            response["messages"] = messages;
        }
        catch(const std::exception& e)
        {
            std::cerr << e.what() << '\n';
            response["status"] = "error";
            response["message"] = e.what();
        }

        return response;
    }
};

// {
//   "action": "delete_message_global",
//   "message_id": ,
//   "chat_id": "uuid"
// }
// ответ JSON
// {
//   "status": "",   
//   "message": ""
class DeleteMessageGlobalEventHandler : public IMsgEventHandler
{
public:
    json handle(const json& msg) override 
    {
        json response;
        try
        {
            uint64_t messageId = msg.value("message_id", 0);
            std::string chatId = msg.value("chat_id", "");

            auto db = DatabaseManage::getInstance();
            if (db->deleteMessageGlobal(messageId))
            {
                response["status"] = "success";
            }
            else
            {
                response["status"] = "error";
                response["message"] = "Failed to delete message from the database";
            }
        }
        catch(const std::exception& e)
        {
            std::cerr << e.what() << '\n';
            response["status"] = "error";
            response["message"] = e.what();
        }
        return response;
    }
};