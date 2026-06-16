//
// Created by sjf on 2025/10/4.
//

#include "memory_text.h"

#include <iomanip>
#include <sstream>

#include "../../helper/config_manager.h"

void MemoryText::init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
                      SDL_Color outlineColor, int outlineSize, int initialX, int initialY) {
    DraggableText::init(name, text, textFont, textColor, outlineColor, outlineSize, initialX, initialY);
    updateCount = ConfigManager::getInstance().getInt("performance.max_fps");
}

void MemoryText::update() {
    if (updateCount == ConfigManager::getInstance().getInt("performance.max_fps")) {
        memoryInfo = getMemoryInfo();
        std::stringstream ss;
        ss << "RAM:" << std::setprecision(2) << memoryInfo.usagePercent << "%";
        setText(ss.str());
        updateCount = 0;
    }
    updateCount++;
}

void MemoryText::drawProgressBar(SDL_Renderer *renderer, int x, int y, int width, int height,
                                 float percent, SDL_Color bg_color, SDL_Color fg_color) {
    // 背景
    SDL_Rect bg_rect = {x, y, width, height};
    SDL_SetRenderDrawColor(renderer, bg_color.r, bg_color.g, bg_color.b, bg_color.a);
    SDL_RenderFillRect(renderer, &bg_rect);
    // 进度
    int progress_width = (int) (width * percent / 100.0f);
    if (progress_width > 0) {
        SDL_Rect progress_rect = {x, y, progress_width, height};
        SDL_SetRenderDrawColor(renderer, fg_color.r, fg_color.g, fg_color.b, fg_color.a);
        SDL_RenderFillRect(renderer, &progress_rect);
    }
    // 边框
    SDL_SetRenderDrawColor(renderer, 255, 255, 255, 255);
    SDL_RenderDrawRect(renderer, &bg_rect);
}

void MemoryText::render(SDL_Renderer *renderer) {
    DraggableText::render(renderer);
    // drawProgressBar(renderer, rect.x + rect.w + 2, rect.y + rect.h / 4, 100,
    //                 ConfigManager::getInstance().getInt("memory_state.size") / 2,
    //                 memoryInfo.usagePercent, (SDL_Color){50, 50, 50, 255},
    //                 (SDL_Color){0, 200, 0, 255});
}

MemoryText::MemoryInfo MemoryText::getMemoryInfo() {
    MemoryInfo mem = {0};
    FILE *file = fopen("/proc/meminfo", "r");
    if (!file) return mem;

    char line[128];
    while (fgets(line, sizeof(line), file)) {
        if (sscanf(line, "MemTotal: %lu kB", &mem.total) == 1) {
            mem.total /= 1024; // 转换为MB
        } else if (sscanf(line, "MemFree: %lu kB", &mem.free) == 1) {
            mem.free /= 1024;
        } else if (sscanf(line, "MemAvailable: %lu kB", &mem.available) == 1) {
            mem.available /= 1024;
        }
    }
    mem.usagePercent = 100.0f * (memoryInfo.total - memoryInfo.available) / memoryInfo.total;
    fclose(file);
    return mem;
}
