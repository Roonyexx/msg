#pragma once
#include <nlohmann/json.hpp>
#include <string>
#include <iostream>

using json = nlohmann::json;

class MsgEventHandler {
public:
    virtual void onMessage(json& msg) = 0;
};


class MessageProcessor : public MsgEventHandler
{
public:
    MessageProcessor() = default;
    ~MessageProcessor() = default;

    void onMessage(json& msg) override
    {
        std::cout << "Received message: " << msg.dump() << std::endl;
    }
};