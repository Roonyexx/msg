#pragma once
#include <unordered_map>
#include "MessageHandler.hpp"


class MessageHandlerFactory
{
public:

    static MessageHandlerFactory& getInstance()
    {
        static MessageHandlerFactory instance;
        return instance;
    }

    void registerHandler(const std::string& type, std::shared_ptr<IMsgEventHandler> handler)
    {
        handlers[type] = handler;
    }

    std::shared_ptr<IMsgEventHandler> getHandler(const std::string& type)
    {
        auto it = handlers.find(type);
        if (it != handlers.end())
        {

            return it->second;
        }
        return nullptr;
    }

private:
    MessageHandlerFactory() { registerHandlers(); }
    // вроде бы мне нравится это решение с точки зрения архитектуры, но вот эта штука выполняется в рантайме
    // хоть и единожды.
    void registerHandlers()
    { 
        registerHandler("register", std::make_shared<RegistrationEventHandler>());
        registerHandler("login", std::make_shared<LoginEventHandler>());
        registerHandler("send_message", std::make_shared<SendMessageEventHandler>());
        registerHandler("get_messages", std::make_shared<GetMessagesEventHandler>());
        registerHandler("delete_message_global", std::make_shared<DeleteMessageGlobalEventHandler>());
        registerHandler("delete_message_local", std::make_shared<DeleteMessageLocalEventHandler>());
        registerHandler("create_chat", std::make_shared<CreateChatEventHandler>());
        registerHandler("leave_chat", std::make_shared<LeaveChatEventHandler>());
        registerHandler("chat_invite", std::make_shared<ChatInviteEventHandler>());
        registerHandler("get_user_chats", std::make_shared<GetUserChatsEventHandler>());
        registerHandler("search_users", std::make_shared<SearchUsersEventHandler>());

    }


    std::unordered_map<std::string, std::shared_ptr<IMsgEventHandler>> handlers;
};