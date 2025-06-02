cmake_minimum_required(VERSION 3.17)
project(rnxMsg LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(BOOST_ROOT "C:/local/boost_1_88_0")
set(Boost_USE_STATIC_LIBS ON)

find_package(BOOST REQUIRED COMPONENTS system)

add_subdirectory(server)