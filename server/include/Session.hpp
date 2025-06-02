#pragma once
#include <boost/asio.hpp>
#include <iostream>
#include <thread>
#include <memory>

using tcp = boost::asio::ip::tcp;

class Session : public std::enable_shared_from_this<Session>
{
public:
    Session(tcp::socket socket);
    ~Session() = default;
    
    void start();

private:
    void handleClient();
    std::shared_ptr<tcp::socket> socket;
};