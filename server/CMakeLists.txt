include_directories(${Boost_INCLUDE_DIRS})

file(GLOB SOURCES
    ./source/*.cpp
    ./include/*.hpp
)

add_executable(rnxMsg ${SOURCES})

target_include_directories(rnxMsg PRIVATE ./include)

target_link_libraries(rnxMsg PRIVATE ${Boost_LIBRARIES})