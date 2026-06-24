//
// Created by sjf on 2025/10/4.
//

#include "cpu_usage_text.h"

#include <iomanip>

#include "../../helper/config_manager.h"

void CpuUsageText::init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
                        SDL_Color outlineColor, int outlineSize, int initialX, int initialY) {
    DraggableText::init(name, text, textFont, textColor, outlineColor, outlineSize, initialX, initialY);
    updateCount = ConfigManager::getInstance().getInt("performance.max_fps");
    progressBarWidth = ConfigManager::getInstance().getInt("cpu_usage.progress_width", 80);
    progressBarHeight = ConfigManager::getInstance().getInt("cpu_usage.progress_height", 8);
    displayMode = ConfigManager::getInstance().getInt("cpu_usage.mode", 2);
    progressMin = ConfigManager::getInstance().getFloat("cpu_usage.progress_min", 0.0f);
    progressMax = ConfigManager::getInstance().getFloat("cpu_usage.progress_max", 100.0f);
}

void CpuUsageText::update() {
    if (updateCount == ConfigManager::getInstance().getInt("performance.max_fps")) {
        cpuInfo.usage = getCpuUsage();
        if (displayMode == 0 || displayMode == 2) {
            std::stringstream ss;
            ss << "CPU " << std::fixed << std::setprecision(1) << std::setw(5) << cpuInfo.usage << "%";
            setText(ss.str());
        } else if (displayMode == 1) {
            setText("CPU");
        }
        updateCount = 0;
    }
    updateCount++;
}

SDL_Color CpuUsageText::getProgressColor(float percent) {
    if (percent < 50.0f) {
        return (SDL_Color){80, 200, 120, 255};
    } else if (percent < 75.0f) {
        return (SDL_Color){255, 200, 80, 255};
    } else {
        return (SDL_Color){255, 100, 100, 255};
    }
}

void CpuUsageText::drawProgressBar(SDL_Renderer *renderer, int x, int y, int width, int height, float percent) {
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
        SDL_Color fgColor = getProgressColor(percent);
        SDL_SetRenderDrawColor(renderer, fgColor.r, fgColor.g, fgColor.b, fgColor.a);

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

void CpuUsageText::render(SDL_Renderer *renderer) {
    if (displayMode == 0 || displayMode == 1 || displayMode == 2) {
        int textWidth, textHeight;
        const char *fixedLabel = (displayMode == 0) ? "100.0%" : ((displayMode == 1) ? "CPU" : "CPU 100.0%");
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
        TTF_SizeText(font, "CPU 100.0%", &textWidth, &textHeight);

        float range = progressMax - progressMin;
        float normalized = (range > 0.0f) ? (cpuInfo.usage - progressMin) / range * 100.0f : 0.0f;
        if (normalized < 0.0f) normalized = 0.0f;
        if (normalized > 100.0f) normalized = 100.0f;

        int barX = rect.x + rect.w + 8;
        int barY = rect.y + (textHeight - progressBarHeight) / 2;
        drawProgressBar(renderer, barX, barY, progressBarWidth, progressBarHeight, normalized);
    }
}

float CpuUsageText::getCpuUsage() {
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
