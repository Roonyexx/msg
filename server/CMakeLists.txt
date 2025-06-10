include_directories(${Boost_INCLUDE_DIRS})
include_directories(${PROJECT_SOURCE_DIR}/include)
#link_directories(${PROJECT_SOURCE_DIR}/lib)

file(GLOB SOURCES
    ./source/*.cpp
    ./include/*.hpp
)

add_executable(${PROJECT_NAME} ${SOURCES})

target_include_directories(${PROJECT_NAME} PRIVATE ./include)

target_link_libraries(${PROJECT_NAME} PRIVATE ${Boost_LIBRARIES})
target_link_libraries(${PROJECT_NAME} PRIVATE libpqxx::pqxx)