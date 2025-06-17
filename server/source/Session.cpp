#include "Session.hpp"


Session::Session(tcp::socket socket)
    : socket{ std::make_shared<tcp::socket>(std::move(socket)) }, 
      running{ true }
{ }

void Session::start()
{
    // поток для получения сообщений от клиента
    std::thread{ &Session::handleClient, shared_from_this() }.detach();

    // поток для обработки сообщений
    std::thread{ &Session::processMessages, shared_from_this() }.detach();
    // там в 20 стандарте корутины появились, я их так и не потыкал, обидно
}

void Session::stop()
{
    running = false;
    cv.notify_all(); // пробуждаем все потоки, ожидающие сообщений
    MessageSender::getInstance().disconnect(userId);
    if (socket && socket->is_open())
        socket->close();
}


void Session::handleClient()
{
    try
    {
        for (;;)
        {
            uint32_t messageSize{ };
            boost::system::error_code errorCode;
            // читаем размер сообщения
            std::size_t bytesRead = boost::asio::read(*socket, boost::asio::buffer(&messageSize, sizeof(messageSize)), errorCode);
            
            if (errorCode == boost::asio::error::eof || bytesRead == 0)
            {
                this->stop();
                break;
            }
            if (errorCode)
            {
                std::cerr << "Error reading from socket: " << errorCode.message() << std::endl;
                this->stop();
                break;
            }

            // читаем само сообщение
            std::vector<char> buffer(messageSize);
            bytesRead = boost::asio::read(*socket, boost::asio::buffer(buffer.data(), messageSize), errorCode);

            if (errorCode || bytesRead != messageSize)
            {
                std::cerr << "Error reading from socket: " << errorCode.message() << std::endl;
                this->stop();
                break;
            }

            std::string msg{ buffer.begin(), buffer.end() };
            // запись сообщения в очередь
            try
            {
                json j = json::parse(msg);
                {
                    std::lock_guard<std::mutex> lock(queueMutex);
                    messages.push(j);
                }
                // будим поток, что обработает сообщения в очереди
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
                json response;
                std::string action = msg.value("action", "");
                auto msgHandler = MessageHandlerFactory::getInstance();
                auto handler = msgHandler.getHandler(action);
                if (handler) response = handler->handle(msg);
                else
                {
                    std::cerr << "No handler found for action: " << action << std::endl;
                    continue; 
                }

                if ((action == "register" || action == "login") && response["status"] == "success")
                {
                    userId = response["message"];
                    MessageSender::getInstance().newConnect(userId, socket);
                }

                // отправляем ответ клиенту, вообще, думаю, по-хорошему нужно это вынести в метод сервера, 
                // сделаю это если еще где-то сообщения понадобится отправлять, что вряд ли, на самом деле
                if (socket && socket->is_open())
                {
                    response["action"] = msg["action"];
                    std::string responseStr = response.dump();
                    uint32_t responseSize = static_cast<uint32_t>(responseStr.size());
                    boost::asio::write(*socket, boost::asio::buffer(&responseSize, sizeof(responseSize)));
                    boost::asio::write(*socket, boost::asio::buffer(responseStr.data(), responseSize));
                }
            }
            catch(const std::exception& e)
            {
                std::cerr << "Message processing exception: " << e.what() << '\n';
            }

            lock.lock(); // блокируем мьютекс для следующей итерации
        }
    }
}
