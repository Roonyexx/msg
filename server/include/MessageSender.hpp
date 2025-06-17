#pragma once
#include <unordered_map>
#include <boost/asio.hpp>
#include <nlohmann/json.hpp>

using tcp = boost::asio::ip::tcp;
using json = nlohmann::json;

class MessageSender
{
public:
    static MessageSender& getInstance()
    {
        static MessageSender* insatnce = new MessageSender(); 
        return *insatnce;
    }

    void newConnect(const std::string userId, std::shared_ptr<tcp::socket> socket)
    {
        if (sessions.find(userId) == sessions.end())
        {
            sessions[userId] = socket;
        }
    }

    void sendMessage(const std::string userId, json msg)
    {
        auto it = sessions.find(userId);
        if (it == sessions.end()) return;

        auto socket = it->second;
        if (socket && socket->is_open())
        {
            std::string responseStr = msg.dump();
            uint32_t responseSize = static_cast<uint32_t>(responseStr.size());
            boost::asio::write(*socket, boost::asio::buffer(&responseSize, sizeof(responseSize)));
            boost::asio::write(*socket, boost::asio::buffer(responseStr.data(), responseSize));
        }

    }

    void disconnect(const std::string userId)
    {
        sessions.erase(userId);
    }

private:
    MessageSender() { };
    std::unordered_map<std::string, std::shared_ptr<tcp::socket>> sessions;
};