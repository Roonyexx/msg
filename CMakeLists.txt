cmake_minimum_required(VERSION 3.17)

set(CMAKE_TOOLCHAIN_FILE "C:/local/vcpkg/scripts/buildsystems/vcpkg.cmake" CACHE STRING "")

project(rnxMsg LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(Boost CONFIG REQUIRED system)
find_package(libpqxx CONFIG REQUIRED)

include_directories(${PROJECT_SOURCE_DIR}/include)
add_subdirectory(server)