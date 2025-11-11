#include "storage_text.h"

//
// Created by sjf on 2025/10/4.
//

#include <iomanip>

#include "../../helper/config_manager.h"
#include <sys/statvfs.h>

void StorageText::init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
                       SDL_Color outlineColor, int outlineSize, int initialX, int initialY) {
    DraggableText::init(name, text, textFont, textColor, outlineColor, outlineSize, initialX, initialY);
    updateCount = ConfigManager::getInstance().getInt("performance.max_fps");
}

void StorageText::update() {
    if (updateCount == ConfigManager::getInstance().getInt("performance.max_fps")) {
        storageInfo = getStorageInfo("/");
        std::stringstream ss;
        ss << "DISK:" << std::setprecision(2) << storageInfo.usagePercent << "%";
        setText(ss.str());
        updateCount = 0;
    }
    updateCount++;
}

void StorageText::drawProgressBar(SDL_Renderer *renderer, int x, int y, int width, int height,
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

void StorageText::render(SDL_Renderer *renderer) {
    DraggableText::render(renderer);
    drawProgressBar(renderer, rect.x + rect.w + 2, rect.y + rect.h / 4, 100,
                    ConfigManager::getInstance().getInt("storage_state.size") / 2,
                    storageInfo.usagePercent, (SDL_Color){50, 50, 50, 255}, (SDL_Color){0, 200, 0, 255});
}


StorageText::StorageInfo StorageText::getStorageInfo(const char *path) {
    StorageInfo storage = {0};
    struct statvfs buf;

    if (statvfs(path, &buf) == 0) {
        storage.total = buf.f_blocks * buf.f_frsize / (1024 * 1024); // MB
        storage.free = buf.f_bfree * buf.f_frsize / (1024 * 1024);
        storage.used = storage.total - storage.free;
        storage.usagePercent = (float) storage.used / storage.total * 100.0f;
    }

    return storage;
}
