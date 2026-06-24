//
// Created by sjf on 2025/10/4.
//

#include "storage_text.h"

#include <iomanip>
#include <sstream>

#include "../../helper/config_manager.h"
#include <sys/statvfs.h>

void StorageText::init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
                       SDL_Color outlineColor, int outlineSize, int initialX, int initialY) {
    DraggableText::init(name, text, textFont, textColor, outlineColor, outlineSize, initialX, initialY);
    updateCount = ConfigManager::getInstance().getInt("performance.max_fps");
    progressBarWidth = ConfigManager::getInstance().getInt("storage_state.progress_width", 80);
    progressBarHeight = ConfigManager::getInstance().getInt("storage_state.progress_height", 8);
    displayMode = ConfigManager::getInstance().getInt("storage_state.mode", 2);
    progressMin = ConfigManager::getInstance().getFloat("storage_state.progress_min", 0.0f);
    progressMax = ConfigManager::getInstance().getFloat("storage_state.progress_max", 100.0f);
}

void StorageText::update() {
    if (updateCount == ConfigManager::getInstance().getInt("performance.max_fps")) {
        storageInfo = getStorageInfo("/");
        if (displayMode == 0 || displayMode == 2) {
            std::stringstream ss;
            ss << "DISK " << std::fixed << std::setprecision(1) << std::setw(5) << storageInfo.usagePercent << "%";
            setText(ss.str());
        } else if (displayMode == 1) {
            setText("DISK");
        }
        updateCount = 0;
    }
    updateCount++;
}

SDL_Color StorageText::getProgressColor(float percent) {
    if (percent < 50.0f) {
        return (SDL_Color){80, 200, 120, 255};
    } else if (percent < 75.0f) {
        return (SDL_Color){255, 200, 80, 255};
    } else {
        return (SDL_Color){255, 100, 100, 255};
    }
}

void StorageText::drawProgressBar(SDL_Renderer *renderer, int x, int y, int width, int height,
                                  float percent, SDL_Color bg_color, SDL_Color fg_color) {
    int radius = height / 2;
    if (radius > width / 2) radius = width / 2;

    SDL_SetRenderDrawBlendMode(renderer, SDL_BLENDMODE_BLEND);

    SDL_Color bgColor = {40, 40, 40, 180};
    SDL_SetRenderDrawColor(renderer, bgColor.r, bgColor.g, bgColor.b, bgColor.a);
    for (int w = 0; w < width; ++w) {
        for (int h = 0; h < height; ++h) {
            int dx = (w < radius) ? (radius - w) : ((w >= width - radius) ? (w - (width - radius - 1)) : 0);
            int dy = (h < radius) ? (radius - h) : ((h >= height - radius) ? (h - (height - radius - 1)) : 0);
            if (w >= radius && w < width - radius) {
                SDL_RenderDrawPoint(renderer, x + w, y + h);
            } else if (dx * dx + dy * dy <= radius * radius) {
                SDL_RenderDrawPoint(renderer, x + w, y + h);
            }
        }
    }

    int progressWidth = (int) ((width - 4) * percent / 100.0f);
    if (progressWidth > 0) {
        SDL_Color progressColor = getProgressColor(percent);
        SDL_SetRenderDrawColor(renderer, progressColor.r, progressColor.g, progressColor.b, progressColor.a);

        int innerRadius = (height - 4) / 2;
        if (innerRadius > (width - 4) / 2) innerRadius = (width - 4) / 2;

        for (int w = 0; w < progressWidth; ++w) {
            for (int h = 0; h < height - 4; ++h) {
                int dx = (w < innerRadius) ? (innerRadius - w) : ((w >= progressWidth - innerRadius && progressWidth > innerRadius * 2) ? (w - (progressWidth - innerRadius - 1)) : 0);
                int dy = (h < innerRadius) ? (innerRadius - h) : ((h >= height - 4 - innerRadius) ? (h - (height - 4 - innerRadius - 1)) : 0);
                bool inRoundedArea = (w < innerRadius || w >= progressWidth - innerRadius);
                bool inCorner = inRoundedArea && (dx * dx + dy * dy <= innerRadius * innerRadius);

                if (!inRoundedArea || inCorner) {
                    SDL_RenderDrawPoint(renderer, x + 2 + w, y + 2 + h);
                }
            }
        }
    }

    SDL_SetRenderDrawBlendMode(renderer, SDL_BLENDMODE_NONE);
}

void StorageText::render(SDL_Renderer *renderer) {
    if (displayMode == 0 || displayMode == 1 || displayMode == 2) {
        int textWidth, textHeight;
        const char *fixedLabel = (displayMode == 0) ? "100.0%" : ((displayMode == 1) ? "DISK" : "DISK 100.0%");
        TTF_SizeText(font, fixedLabel, &textWidth, &textHeight);

        if (texture) {
            SDL_DestroyTexture(texture);
            texture = nullptr;
        }
        createOutlinedTextTexture(renderer, textColor, outlineColor, outlineSize);

        if (displayMode != 0) {
            rect.w = textWidth + outlineSize * 2;
        }

        DraggableText::render(renderer);
    }

    if (displayMode == 1 || displayMode == 2) {
        int textWidth, textHeight;
        TTF_SizeText(font, "DISK 100.0%", &textWidth, &textHeight);

        float range = progressMax - progressMin;
        float normalized = (range > 0.0f) ? (storageInfo.usagePercent - progressMin) / range * 100.0f : 0.0f;
        if (normalized < 0.0f) normalized = 0.0f;
        if (normalized > 100.0f) normalized = 100.0f;

        int barX = rect.x + rect.w + 8;
        int barY = rect.y + (textHeight - progressBarHeight) / 2;
        drawProgressBar(renderer, barX, barY, progressBarWidth, progressBarHeight, normalized,
                        (SDL_Color){0, 0, 0, 0}, (SDL_Color){0, 0, 0, 0});
    }
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
