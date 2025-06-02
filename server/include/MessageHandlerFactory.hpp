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
    void registerHandlers()
    { 
        registerHandler("registration", std::make_shared<RegistrationEventHandler>());
        registerHandler("login", std::make_shared<LoginEventHandler>());
    }


    std::unordered_map<std::string, std::shared_ptr<IMsgEventHandler>> handlers;
};