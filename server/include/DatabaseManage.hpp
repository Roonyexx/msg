#pragma once
#include <string>
#include <memory>
#include <pqxx/pqxx>
#include <nlohmann/json.hpp>
#include <iostream>
#include <vector>

using json = nlohmann::json;

enum class chatType : uint64_t
{
    chatPrivate = 1,
    chatGroup = 2
};

enum class userStatus : uint64_t
{
    offline = 1,
    online = 2
};

enum class userChatRole : uint64_t
{
    member = 1,
    owner = 2,
    admin = 3
};



class DatabaseManage 
{
public:
    // реализация синглтона, не обязатольно на самом деле мне просто не очень нравится идея хранить указатель на базу данных в обработчиках
    // ну и к тому же конекты к разным базам данных мне не нужны, а конект к одной несколько раз очевидная ошибка.
    static std::shared_ptr<DatabaseManage> getInstance(const std::string& connectionString);
    static std::shared_ptr<DatabaseManage> getInstance();

    // true - юзернейм есть в базе, false - нет 
    bool isUsernameExists(const std::string& username);

    // создает пользователя в базе данных, возвращает его uuid
    // если возникнет ошибка, то вернет пустую строку
    std::string createUser(const std::string& username, const std::string& passwordHash);

    // вошли - uuid, не вошли - пустая строка 
    std::string authenticateUser(const std::string& username, const std::string& passwordHash);

    // обновляет статус пользователя, возвращает true если все ок
    // статус у нас онлайн или оффлайн, но в енам выше можно и новенькое что-нибудь запихнуть
    bool updateUserStatus(const std::string& userId, const userStatus status);

    // это чтобы сообщение отправить в базу данных
    std::string saveMessage(const std::string& userId, 
                     const std::string& chatId, 
                     const std::string& content);

    // это чтобы удалить сообщение из базы данных
    bool deleteMessageGlobal(const uint64_t messageId);

    // добавляет строки в соответствующую таблицу сообщения из этой с таблицы не будут отданы пользователю 
    // ну грубо говоря, это чтобы удалить сообщение только для себя
    bool deleteMessageLocal(const std::string& userId, 
                            const uint64_t& messageId, 
                            const std::string& chatId);

    // создает чат, возвращает его uuid
    std::string createChat(const std::string& chatTitle, 
                           const chatType type, 
                           const std::vector<std::string>& userIds);

    // просто создает связь между пользователем и чатом
    bool addUserToChat(const std::string& chatId, 
                       const std::string& userId, 
                       const userChatRole role = userChatRole::member);

    // возвращает список чатов пользователя, список содержит полную информацию о чате
    std::vector<json> getUserChats(const std::string& userId);

    // возвращает список сообщений чата, список содержит полную информацию о сообщении
    std::vector<json> getChatMessages(const std::string& chatId);

    // возвращает список uuid пользователей в чате
    std::vector<std::string> getUsersInChat(const std::string& chatId);

    // возвращает имя пользователя по его uuid
    std::string getUsername(const std::string& userId);

    // удаляет связь между пользователем и чатом
    bool leaveChat(const std::string& chatId, const std::string& userId);
 
private:
    bool addChatParticipant(const std::string& chatId, 
                            const std::string& userId, 
                            const userChatRole role, 
                            pqxx::work& txn);

    // преобразует тип чата в строку, просто мне не очень понравилась идею в json отпроавлять числовое id 
    std::string chatTypeToString(chatType type);
    
    DatabaseManage(const std::string& connectionString)
        : conn(std::make_unique<pqxx::connection>(connectionString)) 
    {
        if (!conn->is_open()) 
        {
            throw std::runtime_error("Failed to open database connection");
        }
    }

    std::unique_ptr<pqxx::connection> conn;
    static std::shared_ptr<DatabaseManage> instance;
};

