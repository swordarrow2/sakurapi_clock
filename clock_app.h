#ifndef SAKURAPI_CLOCK_CLOCK_APP_H
#define SAKURAPI_CLOCK_CLOCK_APP_H

#include <atomic>
#include <string>
#include <memory>
#include <mutex>
#include <queue>
#include <thread>
#include <condition_variable>
#include <SDL2/SDL.h>

class ClockApp {
public:
    ClockApp();

    ~ClockApp();

    bool initialize();

    void run();

private:
    bool initSDL();

    bool createWindow();

    bool createRenderer();

    bool loadResources();

    void handleEvents();

    void update();

    void render();

    void cleanup();

    void consoleInputThread(); // 控制台输入线程函数

    // 处理控制台命令
    void processCommand(const std::string &command);

    SDL_Window *window;
    SDL_Renderer *renderer;

    bool running;
    const int screenWidth;
    const int screenHeight;

    // 控制台输入相关
    std::thread consoleThread;
    std::atomic<bool> consoleThreadRunning;
    std::mutex commandMutex;
    std::condition_variable commandCV;
    std::queue<std::string> commandQueue;
};

#endif // SAKURAPI_CLOCK_CLOCK_APP_H
