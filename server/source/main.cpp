//#define BOOST_DISABLE_CURRENT_LOCATION
#include "Session.hpp"
#include "Server.hpp"
#include "DatabaseManage.hpp"


int main() 
{
    try
    {
        auto& db = DatabaseManage::getInstance("dbname=rnxMsg user=postgres password=1 host=localhost port=5432");
    } 
    catch (const std::exception& e) 
    {
        std::cerr << "Database connection error: " << e.what() << std::endl;
        return 1;
    }
    boost::asio::io_context io_context;
    Server serv{ io_context, 5001 };
    serv.run();
}