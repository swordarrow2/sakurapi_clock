//
// Created by sjf on 2025/10/4.
//

#include "cpu_text.h"

#include <iomanip>

#include "../../helper/config_manager.h"
#include <sys/statvfs.h>

void CpuText::init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
                   SDL_Color outlineColor, int outlineSize, int initialX, int initialY) {
    DraggableText::init(name, text, textFont, textColor, outlineColor, outlineSize, initialX, initialY);
    updateCount = ConfigManager::getInstance().getInt("performance.max_fps");
}

void CpuText::update() {
    if (updateCount == ConfigManager::getInstance().getInt("performance.max_fps")) {
        cpuInfo.usage = getCpuUsage();
        std::stringstream ss;
        ss << "CPU:" << std::setprecision(2) << cpuInfo.usage << "%";
        setText(ss.str());
        updateCount = 0;
    }
    updateCount++;
}

void CpuText::drawProgressBar(SDL_Renderer *renderer, int x, int y, int width, int height,
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

void CpuText::render(SDL_Renderer *renderer) {
    DraggableText::render(renderer);
    drawProgressBar(renderer, rect.x + rect.w + 2, rect.y + rect.h / 4, 100,
                    ConfigManager::getInstance().getInt("cpu_state.size") / 2,
                    cpuInfo.usage, (SDL_Color){50, 50, 50, 255}, (SDL_Color){0, 200, 0, 255});
}

float CpuText::getCpuUsage() {
    FILE *file = fopen("/proc/stat", "r");
    if (!file) return 0.0;

    unsigned long user, nice, system, idle, iowait, irq, softirq;
    fscanf(file, "cpu %lu %lu %lu %lu %lu %lu %lu",
           &user, &nice, &system, &idle, &iowait, &irq, &softirq);
    fclose(file);

    static unsigned long prev_idle = 0, prev_total = 0;
    unsigned long idle_time = idle + iowait;
    unsigned long total_time = user + nice + system + idle + iowait + irq + softirq;

    float usage = 0.0;
    if (prev_total > 0) {
        unsigned long total_diff = total_time - prev_total;
        unsigned long idle_diff = idle_time - prev_idle;
        usage = (float) (total_diff - idle_diff) / total_diff * 100.0f;
    }

    prev_idle = idle_time;
    prev_total = total_time;

    return usage;
}

float CpuText::getCpuTemperature() {
    FILE *file = fopen("/sys/class/thermal/thermal_zone0/temp", "r");
    if (!file) return 0.0;

    int temp;
    fscanf(file, "%d", &temp);
    fclose(file);

    return temp / 1000.0f; // 转换为摄氏度
}


CpuText::StorageInfo CpuText::getStorageInfo(const char *path) {
    StorageInfo storage = {0};
    struct statvfs buf;

    if (statvfs(path, &buf) == 0) {
        storage.total = (buf.f_blocks * buf.f_frsize) / (1024 * 1024); // MB
        storage.free = (buf.f_bfree * buf.f_frsize) / (1024 * 1024);
        storage.used = storage.total - storage.free;
        storage.usage_percent = ((float) storage.used / storage.total) * 100.0f;
    }

    return storage;
}
