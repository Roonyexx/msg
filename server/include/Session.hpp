#pragma once
#include <boost/asio.hpp>
#include <nlohmann/json.hpp>
#include <iostream>
#include <thread>
#include <memory>
#include <queue>
#include <mutex>
#include <condition_variable>

using tcp = boost::asio::ip::tcp;
using json = nlohmann::json;

class Session : public std::enable_shared_from_this<Session>
{
public:
    Session(tcp::socket socket);
    ~Session() = default;

    // создаем сессию
    // запускаем поток для получения сообщений от клиента
    // запускаем поток для обработки сообщений
    void start();

    // останавливаем сессию
    // все потоки завершатся, а сокет закроется
    void stop();

private:
    // читаем сообщения от клиента
    // и помещаем их в очередь сообщений
    void handleClient();

    // обрабатываем сообщения из очереди
    // строго говоря, читаем их из очереди и вызываем обработчик 
    void processMessages();

    std::shared_ptr<tcp::socket> socket;

    std::queue<json> messages;
    std::mutex queueMutex;
    std::condition_variable cv;
    std::atomic<bool> running;
};

