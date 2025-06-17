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
                std::cout << "User created successfully: " << response["message"] << std::endl;
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
                response["message"] = userId;
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
            if (!(response["message"] = db->saveMessage(userId, chatId, content)).empty())
            {
                response["status"] = "success";
                auto usersToPing = db->getUsersInChat(chatId);
                usersToPing.erase(
                    std::remove(usersToPing.begin(), usersToPing.end(), userId),
                    usersToPing.end()
                );
                json ping;
                ping["action"] = "new_message";
                ping["chat_id"] = chatId;
                ping["from_user_id"] = userId;
                ping["username"] = db->getUsername(userId);
                ping["content"] = content;
                ping["message_id"] = response["message"];
                
                auto sender = MessageSender::getInstance();
                for (const auto& userid : usersToPing)
                {
                    sender.sendMessage(userid, ping);
                }

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
//   "chat_id": ""
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
            std::string chatId { msg.value("chat_id", "") };

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



// {
//   "action": "delete_message_local",
//   "message_id": ,
//   "user_id": ""
//   "chat_id": ""
// }
// ответ JSON
// {
//   "status": "",   
//   "message": ""
class DeleteMessageLocalEventHandler : public IMsgEventHandler
{
public:
    json handle(const json& msg) override 
    {
        json response;
        try
        {
            uint64_t messageId = msg.value("message_id", 0);
            std::string userId { msg.value("user_id", "") };
            std::string chatId { msg.value("chat_id", "") };

            auto db = DatabaseManage::getInstance();
            if (db->deleteMessageLocal(userId, messageId, chatId))
            {
                response["status"] = "success";
            }
            else
            {
                response["status"] = "error";
                response["message"] = "Failed to delete local message from the database";
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

// {
//   "action": "create_chat",
//   "chat_name": ""
//   "chat_type": ""
//   "chat_members": [] 
// }
// ответ JSON
// {
//   "status": "",   
//   "message": ""
class CreateChatEventHandler : public IMsgEventHandler
{
public:
    json handle(const json& msg) override 
    {
        json response;
        try
        {
            std::string chatName { msg.value("chat_name", "") };
            std::string t { msg.value("chat_type", "private") };
            // мне стыдно за 3 строки ниже, это повториться
            chatType type { chatType::chatPrivate };
            if (t == "group") type = chatType::chatGroup;
            else if (t != "private") throw std::invalid_argument("Invalid chat type");

            std::vector<std::string> chatMembers = msg.value("chat_members", json::array());

            auto db = DatabaseManage::getInstance();
            if (!(response["message"] = db->createChat(chatName, type, chatMembers)).empty())
            {
                response["status"] = "success";
            }
            else
            {
                response["status"] = "error";
                response["message"] = "Failed to create chat in the database";
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


// {
//   "action": "leave_chat",
//   "user_id": ""
//   "chat_id": ""
// }
// ответ JSON
// {
//   "status": "",   
//   "message": ""
class LeaveChatEventHandler : public IMsgEventHandler
{
public:
    json handle(const json& msg) override 
    {
        json response;
        try
        {
            std::string userId { msg.value("user_id", "") };
            std::string chatId { msg.value("chat_id", "") };

            auto db = DatabaseManage::getInstance();
            if (db->leaveChat(chatId, userId))
            {
                response["status"] = "success";
            }
            else
            {
                response["status"] = "error";
                response["message"] = "Failed to leave chat in the database";
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


// {
//   "action": "chat_invite",
//   "user_id": ""
//   "chat_id": ""
// }
// ответ JSON
// {
//   "status": "",   
//   "message": ""
class ChatInviteEventHandler : public IMsgEventHandler
{
public:
    json handle(const json& msg) override 
    {
        json response;
        try
        {
            std::string userId { msg.value("user_id", "") };
            std::string chatId { msg.value("chat_id", "") };

            auto db = DatabaseManage::getInstance();
            if (db->addUserToChat(chatId, userId))
            {
                response["status"] = "success";
            }
            else
            {
                response["status"] = "error";
                response["message"] = "Failed to invite user to chat in the database";
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


// {
//   "action": "get_user_chats",
//   "user_id": ""
// }
// ответ JSON
// {
//   "status": "",   
//   "message": ""
class GetUserChatsEventHandler : public IMsgEventHandler
{
public:
    json handle(const json& msg) override 
    {
        json response;
        try
        {
            std::string userId { msg.value("user_id", "") };
            auto db = DatabaseManage::getInstance();
            auto chats = db->getUserChats(userId);

            response["status"] = "success";
            response["chats"] = chats;
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