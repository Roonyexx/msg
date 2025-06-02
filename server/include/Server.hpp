#pragma once
#include <boost/asio.hpp>
#include <iostream>
#include <thread>
#include <memory>
#include "Session.hpp"

using tcp = boost::asio::ip::tcp;

class Server
{
public:
    Server(boost::asio::io_context& io_context, boost::asio::ip::port_type port);
    ~Server() = default;

    void run();

private:
    tcp::acceptor acceptor;
};