file(GLOB_RECURSE SOURCES
    "./source/*.cpp"
)

file(GLOB_RECURSE HEADERS
    "./include/*.hpp"
    "${PROJECT_SOURCE_DIR}/include/*.hpp" 
)


add_executable(${PROJECT_NAME} ${SOURCES} ${HEADERS})


target_compile_features(${PROJECT_NAME} PRIVATE cxx_std_20)


target_include_directories(${PROJECT_NAME} PRIVATE 
    ./include
    ${PROJECT_SOURCE_DIR}/include
    ${Boost_INCLUDE_DIRS}
)

target_link_libraries(${PROJECT_NAME} PRIVATE 
    Boost::system
    libpqxx::pqxx
)
