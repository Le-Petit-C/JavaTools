cmake_minimum_required(VERSION 3.10)
project(jni_dummy)

include_directories(
        "D:/Program Files/Zulu/zulu-25/include"
        "D:/Program Files/Zulu/zulu-25/include/win32"
)

add_executable(dummy src/main/c/main.c)