#include "Session.hpp"

Session::Session(tcp::socket socket)
    : socket{ std::make_shared<tcp::socket>(std::move(socket)) }
{ }

void Session::start()
{
    std::thread{ &Session::handleClient, shared_from_this() }.detach();
}

void Session::handleClient()
{
    char data[1024];
    try
    {
        for (;;)
        {
            boost::system::error_code errorCode;
            std::size_t length = socket->read_some(boost::asio::buffer(data), errorCode);

            if (length == 0) break; 
            if (errorCode == boost::asio::error::eof)
                break;

            std::string received{ data, length };
            std::cout << "client send: " << received << std::endl;

        }
    }
    catch (std::exception& e)
    {
        std::cerr << e.what() << std::endl;
    }
}