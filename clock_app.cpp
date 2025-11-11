#include "clock_app.h"
#include <iostream>
#include <ostream>

#include "helper/config_manager.h"
#include "resource/texture_manager.h"
#include "resource/color_manager.h"
#include "resource/font_manager.h"
#include "resource/text_manager.h"
#include "resource/background_manager.h"

uint32_t frameStart = SDL_GetTicks();
uint32_t frameTime = 0;

int max_fps = 60;

bool frameDrop = false;

using std::cout;
using std::endl;
using std::cerr;

ClockApp::ClockApp()
    : window(nullptr), renderer(nullptr), running(true),
      screenWidth(ConfigManager::getInstance().getInt("hardware.screen_width")),
      screenHeight(ConfigManager::getInstance().getInt("hardware.screen_height")),
      consoleThreadRunning(false) {
}

ClockApp::~ClockApp() {
    cleanup();
}

void ClockApp::consoleInputThread() {
    std::string command;
    while (consoleThreadRunning) {
        std::getline(std::cin, command);
        SDL_Delay(10);
        if (!command.empty()) {
            std::lock_guard<std::mutex> lock(commandMutex);
            commandQueue.push(command);
            commandCV.notify_one();
        }
    }
}

void ClockApp::processCommand(const std::string &command) {
    std::cout << "处理命令: " << command << std::endl;
    if (command == "quit" || command == "exit") {
        running = false;
    } else if (command == "n") {
        BackgroundManager::getInstance().setNextStyle(renderer);
    } else if (command == "p") {
        BackgroundManager::getInstance().setPreStyle(renderer);
    } else if (command == "help") {
        std::cout << "可用命令:" << std::endl;
        std::cout << "  quit/exit - 退出程序" << std::endl;
        std::cout << "  next/switch - 切换到下一个背景" << std::endl;
        std::cout << "  prev/previous - 切换到上一个背景" << std::endl;
        std::cout << "  help - 显示帮助信息" << std::endl;
    } else {
        std::cout << "未知命令: " << command << std::endl;
        std::cout << "输入 'help' 查看可用命令" << std::endl;
    }
}

bool ClockApp::initialize() {
#ifdef ARMBIAN
    setenv("SDL_VIDEODRIVER", "kmsdrm", 1);
#endif

    if (!initSDL()) return false;
    if (!createWindow()) return false;
    if (!createRenderer()) return false;
    if (!loadResources()) return false;

    consoleThreadRunning = true;
    consoleThread = std::thread(&ClockApp::consoleInputThread, this);
    return true;
}

bool ClockApp::initSDL() {
    if (SDL_Init(SDL_INIT_VIDEO) < 0) {
        std::cerr << "SDL init: " << SDL_GetError() << std::endl;
        return false;
    }

    if (TTF_Init() == -1) {
        std::cerr << "TTF init: " << TTF_GetError() << std::endl;
        SDL_Quit();
        return false;
    }
    return true;
}

bool ClockApp::createWindow() {
#ifdef UBUNTU
    window = SDL_CreateWindow(
        "嘤嘤表", // 窗口标题
        SDL_WINDOWPOS_CENTERED, // x位置
        SDL_WINDOWPOS_CENTERED, // y位置
        screenWidth, // 宽度
        screenHeight, // 高度
        SDL_WINDOW_SHOWN // 窗口标志
    );
#else
    window = SDL_CreateWindow("clock",
                              SDL_WINDOWPOS_UNDEFINED,
                              SDL_WINDOWPOS_UNDEFINED,
                              screenWidth, screenHeight,
                              SDL_WINDOW_FULLSCREEN | SDL_WINDOW_BORDERLESS);

#endif
    if (!window) {
        std::cerr << "create window: " << SDL_GetError() << std::endl;
        return false;
    }
    return true;
}

bool ClockApp::createRenderer() {
    renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
    if (!renderer) {
        std::cerr << "renderer: " << SDL_GetError() << std::endl;
        return false;
    }
    return true;
}

bool ClockApp::loadResources() {
    TextureManager::getInstance().init(renderer);
    BackgroundManager::getInstance().init(renderer);
    ColorManager::getInstance().init();
    FontManager::getInstance().init();
    TextManager::getInstance().init();

    return true;
}

void ClockApp::run() {
    // 主循环
    while (running) {
        try {
            frameStart = SDL_GetTicks();
            handleEvents();
            update();
            render();
            // 处理控制台命令
            std::string command;
            std::unique_lock<std::mutex> lock(commandMutex);
            if (commandCV.wait_for(lock, std::chrono::milliseconds(0), [this] { return !commandQueue.empty(); })) {
                command = commandQueue.front();
                commandQueue.pop();
            }
            if (!command.empty()) {
                processCommand(command);
            }
            frameTime = SDL_GetTicks() - frameStart;
            if (frameTime < 1000.0f / max_fps) {
                SDL_Delay((uint32_t) ((1000.0f / max_fps - frameTime)));
            } else {
                frameDrop = true;
            }
        } catch (const std::exception &e) {
            cerr << e.what() << endl;
        }
    }
}

void ClockApp::handleEvents() {
    SDL_Event event;
    while (SDL_PollEvent(&event)) {
        if (event.type == SDL_QUIT ||
            (event.type == SDL_KEYDOWN && event.key.keysym.sym == SDLK_ESCAPE)) {
            running = false;
        }
        TextManager::getInstance().handleEvent(&event);
    }
}

void ClockApp::update() {
#ifdef UBUNTU
    max_fps = ConfigManager::getInstance().getInt("performance.max_fps");
    SDL_ShowCursor(SDL_ENABLE);
#else
    max_fps = ConfigManager::getInstance().getInt("performance.max_fps");
    max_fps < 1 ? 1 : max_fps;
    if (ConfigManager::getInstance().getInt("gui.show_cursor")) {
        SDL_ShowCursor(SDL_ENABLE);
    } else {
        SDL_ShowCursor(SDL_DISABLE);
    }
#endif
    BackgroundManager::getInstance().update();
    TextManager::getInstance().update();
}

void ClockApp::render() {
    if (frameDrop) {
        frameDrop = false;
        return;
    }
    SDL_RenderClear(renderer);
    BackgroundManager::getInstance().render(renderer);
    TextManager::getInstance().render(renderer);
    SDL_RenderPresent(renderer);
}

void ClockApp::cleanup() {
    consoleThreadRunning = false;
    if (consoleThread.joinable()) {
        consoleThread.join();
    }
    if (renderer) SDL_DestroyRenderer(renderer);
    if (window) SDL_DestroyWindow(window);
    TTF_Quit();
    SDL_Quit();
}
