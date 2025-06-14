#include "DatabaseManage.hpp"

std::shared_ptr<DatabaseManage> DatabaseManage::instance = nullptr;


std::shared_ptr<DatabaseManage> DatabaseManage::getInstance(const std::string &connectionString)
{
    instance = std::shared_ptr<DatabaseManage>(new DatabaseManage(connectionString));
    return instance;
}

std::shared_ptr<DatabaseManage> DatabaseManage::getInstance()
{
    if (!instance) 
    {
        throw std::runtime_error("DatabaseManage instance not initialized");
    }
    return instance;
}

bool DatabaseManage::isUsernameExists(const std::string &username)
{
    pqxx::nontransaction txn(*conn);
    pqxx::result res = txn.exec_params("select 1 from app_user where username = $1", username);
    return !res.empty();
}

std::string DatabaseManage::createUser(const std::string &username, const std::string &passwordHash)
{
    pqxx::work txn(*conn);
    try 
    {
        pqxx::result res = 
            txn.exec(pqxx::zview("insert into app_user (username, password_hash, status_id) values ($1, $2, $3) returning id"),
            pqxx::params(username, passwordHash, static_cast<uint64_t>(userStatus::online)));
        txn.commit();
        return !res.empty() ? res[0]["id"].as<std::string>() : "";
    } 
    catch (const pqxx::sql_error& e) 
    {
        std::cerr << "SQL error: " << e.what() << std::endl;
        return "";
    } 
}

std::string DatabaseManage::authenticateUser(const std::string &username, const std::string &passwordHash)
{
    pqxx::nontransaction txn(*conn);
    pqxx::result res = txn.exec(
        pqxx::zview("select id from app_user where username = $1 and password_hash = $2"),
        pqxx::params(username, passwordHash)
    );
    return !res.empty() ? res[0]["id"].as<std::string>() : "";
}

bool DatabaseManage::updateUserStatus(const std::string &userId, const userStatus status)
{
    pqxx::work txn(*conn);
    try 
    {
        txn.exec(pqxx::zview("update app_user set status_id = $1 where id = $2"), pqxx::params(static_cast<uint64_t>(status), userId));
        txn.commit();
        return true;
    } 
    catch (const pqxx::sql_error& e) 
    {
        std::cerr << "SQL error: " << e.what() << std::endl;
        return false;
    }
}

bool DatabaseManage::saveMessage(const std::string &userId, const std::string &chatId, const std::string &content)
{
    pqxx::work txn(*conn);
    try
    {
        txn.exec(
            pqxx::zview("insert into message (sender_id, chat_id, content) values ($1, $2, $3)"),
            pqxx::params(userId, chatId, content));
        txn.commit();
        return true;
    }
    catch (const pqxx::sql_error& e) 
    {
        std::cerr << "SQL error: " << e.what() << std::endl;
        return false;
    }
}

bool DatabaseManage::deleteMessageGlobal(const uint64_t messageId)
{
    pqxx::work txn(*conn);
    try
    {
        txn.exec(pqxx::zview("delete from message where id = $1"), pqxx::params(messageId));
        txn.commit();
        return true;
    }
    catch (const pqxx::sql_error& e) 
    {
        std::cerr << "SQL error: " << e.what() << std::endl;
        return false;
    }
}

bool DatabaseManage::deleteMessageLocal(const std::string &userId, const uint64_t &messageId, const std::string &chatId)
{
    pqxx::work txn(*conn);
    try
    {
        txn.exec(pqxx::zview("insert into deleted_message (user_id, message_id, chat_id) values ($1, $2, $3)"), 
                    pqxx::params(userId, messageId, chatId));
        txn.commit();
        return true;
    }
    catch (const pqxx::sql_error& e) 
    {
        std::cerr << "SQL error: " << e.what() << std::endl;
        return false;
    }
}

std::string DatabaseManage::createChat(const std::string &chatTitle, const chatType type, const std::vector<std::string> &userIds)
{
    pqxx::work txn(*conn);
    try
    {
        userChatRole role = (type == chatType::chatGroup) ? userChatRole::owner : userChatRole::member;
        pqxx::row row = txn.exec(pqxx::zview("insert into chat (title, chat_type_id) values ($1, $2) returning id"), 
                                    pqxx::params(chatTitle, static_cast<uint64_t>(type)))[0];

        std::string chatId = row["id"].as<std::string>();

        for (const auto& userId : userIds) 
        {
            addChatParticipant(chatId, userId, role, txn);
            role = userChatRole::member;
        }

        txn.commit();
        return chatId;
    }
    catch (const pqxx::sql_error& e) 
    {
        std::cerr << "SQL error: " << e.what() << std::endl;
        return "";
    }
}

bool DatabaseManage::addUserToChat(const std::string &chatId, const std::string &userId, const userChatRole role)
{
    pqxx::work txn(*conn);
    return addChatParticipant(chatId, userId, role, txn);
}

// std::vector<json> DatabaseManage::getUserChats(const std::string &userId)
// {
//     pqxx::nontransaction txn(*conn);
//     pqxx::result res = txn.exec(
//         pqxx::zview("select cp.chat_id, c.title, c.chat_type_id "
//                     "from chat_participant cp "
//                     "join chat c on cp.chat_id = c.id "
//                     "where cp.user_id = $1"), 
//         pqxx::params(userId)
//     );

//     std::vector<json> chats;
//     for (const auto& row : res)
//     {
//         json chat;
//         chat["chat_id"] = row["chat_id"].as<std::string>();
//         chat["title"] = row["title"].as<std::string>();
//         chat["chat_type"] = chatTypeToString(static_cast<chatType>(row["chat_type_id"].as<uint64_t>()));
//         chats.push_back(chat);
//     }
//     return chats;
// }

std::vector<json> DatabaseManage::getUserChats(const std::string &userId)
{
    pqxx::nontransaction txn(*conn);
    pqxx::result res = txn.exec(
        pqxx::zview(
            "select cp.chat_id, c.title, c.chat_type_id, c.last_message_at, "
            "   (select m.content from message m where m.chat_id = cp.chat_id order by m.sent_at desc limit 1) as last_message "
            "from chat_participant cp "
            "join chat c on cp.chat_id = c.id "
            "where cp.user_id = $1"
        ),
        pqxx::params(userId)
    );

    std::vector<json> chats;
    for (const auto& row : res)
    {
        json chat;
        chat["chat_id"] = row["chat_id"].as<std::string>();
        chat["title"] = row["title"].as<std::string>();
        chat["chat_type"] = chatTypeToString(static_cast<chatType>(row["chat_type_id"].as<uint64_t>()));
        // Добавляем последнее сообщение (может быть null, если сообщений нет)
        if (row["last_message"].is_null())
        {
            chat["last_message"] = nullptr;
            chat["last_message_at"] = nullptr;
        }
        else
        {
            chat["last_message"] = row["last_message"].as<std::string>();
            //chat["last_message_at"] = row["last_message_at"].as<std::string>();
        }
        chats.push_back(chat);
    }
    return chats;
}

std::vector<json> DatabaseManage::getChatMessages(const std::string &chatId)
{
    pqxx::nontransaction txn(*conn);
    pqxx::result res = txn.exec(
        pqxx::zview("select m.id, m.sender_id, m.content, m.sent_at "
                    "from message m "
                    "where m.chat_id = $1 "
                    "order by m.sent_at asc"), 
        pqxx::params(chatId)
    );

    std::vector<json> messages;
    for (const auto& row : res)
    {
        json message;
        message["id"] = row["id"].as<uint64_t>();
        message["user_id"] = row["sender_id"].as<std::string>();
        message["content"] = row["content"].as<std::string>();
        message["timestamp"] = row["sent_at"].as<std::string>();
        messages.push_back(message);
    }
    return messages;
}

bool DatabaseManage::leaveChat(const std::string &chatId, const std::string &userId)
{
    pqxx::work txn(*conn);
    try
    {
        txn.exec(pqxx::zview("delete from chat_participant where chat_id = $1 and user_id = $2"), pqxx::params(chatId, userId));
        txn.commit();
        return true;
    }
    catch (const pqxx::sql_error& e) 
    {
        std::cerr << "SQL error: " << e.what() << std::endl;
        return false;
    }
}

bool DatabaseManage::addChatParticipant(const std::string &chatId, const std::string &userId, const userChatRole role, pqxx::work &txn)
{
    try
    {
        txn.exec(
            pqxx::zview("insert into chat_participant (user_id, chat_id, chat_role_id) values ($1, $2, $3)"),
            pqxx::params(userId, chatId, static_cast<uint64_t>(role)));

        return true;
    }
    catch (const pqxx::sql_error& e) 
    {
        std::cerr << "SQL error: " << e.what() << std::endl;
        return false;
    }
}

std::string DatabaseManage::chatTypeToString(chatType type)
{
    switch (type) 
    {
        case chatType::chatPrivate: return "private";
        case chatType::chatGroup: return "group";
        default: return "unknown";
    }
}
