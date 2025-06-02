//#define BOOST_DISABLE_CURRENT_LOCATION
#include "Session.hpp"
#include "Server.hpp"


int main() 
{
    boost::asio::io_context io_context;
    Server serv{ io_context, 5001 };
    serv.run();
}