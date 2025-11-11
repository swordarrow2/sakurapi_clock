cmake_minimum_required(VERSION 3.10)
project(clock)

set(CMAKE_CXX_STANDARD 14)


# 自动检测操作系统和发行版
message(STATUS "System: ${CMAKE_SYSTEM_NAME}")
message(STATUS "Processor: ${CMAKE_SYSTEM_PROCESSOR}")

# 检测Linux发行版
if (UNIX AND NOT APPLE)
    # 读取 /etc/os-release 来检测具体发行版
    execute_process(
            COMMAND bash -c "grep ^ID= /etc/os-release | cut -d= -f2 | tr -d '\\\"'"
            OUTPUT_VARIABLE OS_ID
            OUTPUT_STRIP_TRAILING_WHITESPACE
            RESULT_VARIABLE OS_ID_RESULT
    )

    execute_process(
            COMMAND bash -c "grep ^NAME= /etc/os-release | cut -d= -f2 | tr -d '\\\"'"
            OUTPUT_VARIABLE OS_NAME
            OUTPUT_STRIP_TRAILING_WHITESPACE
            RESULT_VARIABLE OS_NAME_RESULT
    )

    if (OS_ID_RESULT EQUAL 0)
        message(STATUS "Linux Distribution ID: ${OS_ID}")
        message(STATUS "Linux Distribution Name: ${OS_NAME}")

        # 设置平台定义
        if (OS_ID STREQUAL "ubuntu")
            message(STATUS "Ubuntu detected - adding UBUNTU definition")
            add_definitions(-DUBUNTU)
            set(PLATFORM_UBUNTU 1)

        elseif (OS_ID STREQUAL "armbian")
            message(STATUS "Armbian detected - adding ARMBIAN definition")
            add_definitions(-DARMBIAN)
            add_definitions(-DDEBIAN)
            set(PLATFORM_ARMBIAN 1)

        elseif (OS_ID STREQUAL "debian")
            message(STATUS "Debian detected - adding DEBIAN definition")
            add_definitions(-DARMBIAN)
            add_definitions(-DDEBIAN)
            set(PLATFORM_DEBIAN 1)

        else ()
            message(STATUS "Other Linux distribution: ${OS_ID}")
            add_definitions(-DLINUX)
        endif ()
    else ()
        message(WARNING "Cannot determine Linux distribution, using generic Linux")
        add_definitions(-DLINUX)
    endif ()

    # 检测是否是ARM架构
    execute_process(
            COMMAND uname -m
            OUTPUT_VARIABLE ARCH
            OUTPUT_STRIP_TRAILING_WHITESPACE
    )
    message(STATUS "Architecture: ${ARCH}")

    if (ARCH MATCHES "arm" OR ARCH MATCHES "aarch64")
        message(STATUS "ARM architecture detected - adding ARM_ARCH definition")
        add_definitions(-DARM_ARCH)
        set(ARM_ARCHITECTURE 1)
    endif ()

elseif (WIN32)
    message(STATUS "Windows detected - adding WINDOWS definition")
    add_definitions(-DWINDOWS)

elseif (APPLE)
    message(STATUS "macOS detected - adding MACOS definition")
    add_definitions(-DMACOS)

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

# 根据平台调整包含目录和链接库
if (PLATFORM_ARMBIAN)
    message(STATUS "Configuring for Armbian specific settings")
    # Armbian可能需要额外的包含路径或库
    # include_directories(/usr/arm-linux-gnueabihf/include)
elseif (PLATFORM_UBUNTU)
    message(STATUS "Configuring for Ubuntu specific settings")
    # Ubuntu特定设置
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
        widget/texts/cpu_text.cpp
        widget/texts/memory_text.cpp
        widget/texts/memory_text.h
        widget/texts/storage_text.cpp
        widget/texts/storage_text.h
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
