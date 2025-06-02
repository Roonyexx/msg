#include "Session.hpp"


Session::Session(tcp::socket socket)
    : socket{ std::make_shared<tcp::socket>(std::move(socket)) }, 
      msgHandler{ std::make_shared<MessageProcessor>() },
      running{ true }
{ }

void Session::start()
{
    // поток для получения сообщений от клиента
    std::thread{ &Session::handleClient, shared_from_this() }.detach();

    // поток для обработки сообщений
    std::thread{ &Session::processMessages, shared_from_this() }.detach();
}

void Session::stop()
{
    running = false;
    cv.notify_all(); // пробуждаем все потоки, ожидающие сообщений
    if (socket && socket->is_open())
        socket->close(); 
}

void Session::handleClient()
{
    boost::asio::streambuf buffer;
    std::istream inputStream(&buffer);

    try
    {
        for (;;)
        {
            boost::system::error_code errorCode;
            std::size_t length = boost::asio::read_until(*socket, buffer, '\n', errorCode);

            if (errorCode == boost::asio::error::eof || length == 0)
                break;
            if (errorCode)
            {
                std::cerr << "Error reading from socket: " << errorCode.message() << std::endl;
                break;
            }

            std::string msg;
            std::getline(inputStream, msg);
            
            // запись сообщения в очередь
            try
            {
                json j = json::parse(msg);
                {
                    std::lock_guard<std::mutex> lock(queueMutex);
                    messages.push(j);
                }
                // уведомление обработчика сообщений
                // т.е. будим поток, что обработает сообщения в очереди
                cv.notify_one();
            }
            catch(const std::exception& e)
            {
                std::cerr << "JSON exception: " << e.what() << std::endl;
            }
        }
    }
    catch (std::exception& e)
    {
        std::cerr << e.what() << std::endl;
    }
}

void Session::processMessages()
{
    while (running || !messages.empty())
    {
        std::unique_lock<std::mutex> lock(queueMutex);
        // ожидание наличия сообщений в очереди или завершения работы
        cv.wait(lock, [this] { return !messages.empty() || !running; });

        while(!messages.empty())
        {
            json msg = messages.front();
            messages.pop();

            lock.unlock(); // освобождаем мьютекс для обработки сообщений
            
            try
            {
                msgHandler->onMessage(msg);
            }
            catch(const std::exception& e)
            {
                std::cerr << "Message processing exception: " << e.what() << '\n';
            }

            lock.lock(); // блокируем мьютекс для следующей итерации
        }
    }
}
