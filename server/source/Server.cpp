#include "Server.hpp"


Server::Server(boost::asio::io_context& io_context, boost::asio::ip::port_type port)
    : acceptor{ io_context, tcp::endpoint(tcp::v4(), port) }
{
    std::cout << "Server started on port: " << port << std::endl;
}


void Server::run()
{
    try 
    {
        for (;;)
        {
            tcp::socket socket{ acceptor.get_executor() };
            acceptor.accept(socket);

            std::cout << "New conect from: " << socket.remote_endpoint() << std::endl;

            std::make_shared<Session>(std::move(socket))->start();
        }
    }
    catch (std::exception& e)
    {
        std::cerr << e.what() << std::endl;
    }
}