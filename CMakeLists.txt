cmake_minimum_required(VERSION 3.17)

#set(CMAKE_TOOLCHAIN_FILE "C:/local/vcpkg/scripts/buildsystems/vcpkg.cmake" CACHE STRING "")
#set(CMAKE_PREFIX_PATH "C:/local/vcpkg/installed/x64-windows" ${CMAKE_PREFIX_PATH})

project(rnxMsg LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_BUILD_TYPE Debug)
set(CMAKE_CXX_STANDARD_REQUIRED ON)


set(BOOST_ROOT "C:/local/boost_1_88_0")
set(Boost_USE_STATIC_LIBS ON)


include_directories(${PROJECT_SOURCE_DIR}/include)


find_package(BOOST REQUIRED COMPONENTS system)
find_package(libpqxx CONFIG REQUIRED)


add_subdirectory(server)