#pragma once
#include <nlohmann/json.hpp>
#include <string>
#include <iostream>

using json = nlohmann::json;


class IMsgEventHandler {
public:
    IMsgEventHandler() = default;
    ~IMsgEventHandler() = default;
    virtual void handle(const json& msg) = 0;
};


class RegistrationEventHandler : public IMsgEventHandler
{
public:
    void handle(const json& msg) override
    {
        throw std::runtime_error("RegistrationEventHandler is not implemented yet");
    }
};


class LoginEventHandler : public IMsgEventHandler
{
public:
    void handle(const json& msg) override
    { 
        throw std::runtime_error("LoginEventHandler is not implemented yet");
    }
};
