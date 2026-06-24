cmake_minimum_required(VERSION 3.10)
project(clock)

set(CMAKE_CXX_STANDARD 14)


# 自动检测操作系统和发行版
message(STATUS "System: ${CMAKE_SYSTEM_NAME}")
message(STATUS "Processor: ${CMAKE_SYSTEM_PROCESSOR}")

# 检测环境类型：Debian/Armbian 为生产环境(PRD)，其余为开发环境(DEV)
if (UNIX AND NOT APPLE)
    execute_process(
            COMMAND bash -c "grep ^ID= /etc/os-release | cut -d= -f2 | tr -d '\\\"'"
            OUTPUT_VARIABLE OS_ID
            OUTPUT_STRIP_TRAILING_WHITESPACE
            RESULT_VARIABLE OS_ID_RESULT
    )

    if (OS_ID_RESULT EQUAL 0 AND (OS_ID STREQUAL "debian" OR OS_ID STREQUAL "armbian"))
        message(STATUS "Production environment detected: ${OS_ID}")
        add_definitions(-DPRD)
        set(PLATFORM_PRODUCTION 1)
    else ()
        message(STATUS "Development environment detected: ${OS_ID}")
        add_definitions(-DDEV)
        set(PLATFORM_DEVELOPMENT 1)
    endif ()
endif ()


# 查找依赖包
#find_package(SDL2 REQUIRED)
#find_package(SDL2_ttf REQUIRED)
#find_package(SDL2_image REQUIRED)

# 包含目录
#include_directories(
#    ${SDL2_INCLUDE_DIRS}
#    ${SDL2_TTF_INCLUDE_DIRS}
#    ${SDL2_IMAGE_INCLUDE_DIRS}
#)

include_directories(
        /usr/include/libdrm
        /usr/include/SDL2)

# 根据环境调整配置
if (PLATFORM_PRODUCTION)
    message(STATUS "Configuring for production environment")
else ()
    message(STATUS "Configuring for development environment")
endif ()


# 添加可执行文件
add_executable(sakurapi_clock
        sakurapi_clock.cpp
        clock_app.cpp
        helper/config_manager.cpp
        widget/draggable_text.cpp
        resource/text_manager.cpp
        resource/texture_manager.cpp
        resource/background_manager.cpp
        resource/font_manager.cpp
        resource/color_manager.cpp
        helper/tar_filesystem.cpp
        helper/tar_packer.cpp
        helper/globals.cpp
        widget/texts/time_text.cpp
        widget/texts/date_text.cpp
        widget/texts/fps_text.cpp
        widget/texts/cpu_usage_text.cpp
        widget/texts/cpu_temp_text.cpp
        widget/texts/memory_text.cpp
        widget/texts/storage_text.cpp
        widget/texts/ip_text.cpp
)

# 链接库
#target_link_libraries(clock
#    ${SDL2_LIBRARIES}
#    ${SDL2_TTF_LIBRARIES}
#    ${SDL2_IMAGE_LIBRARIES}
#)

target_link_libraries(
        sakurapi_clock
        drm
        SDL2
        SDL2_image
        SDL2_ttf
        pthread)

# 安装目标（可选）
#install(TARGETS sakurapi_clock DESTINATION bin)
