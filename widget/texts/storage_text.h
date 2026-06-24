//
// Created by sjf on 2025/10/4.
//

#ifndef SAKURAPI_CLOCK_STORAGE_TEXT_H
#define SAKURAPI_CLOCK_STORAGE_TEXT_H

#include "../draggable_text.h"

class StorageText : public DraggableText {
public:
    struct StorageInfo {
        unsigned long total;
        unsigned long free;
        unsigned long used;
        float usagePercent;
    };

    StorageInfo storageInfo;

    void init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
              SDL_Color outlineColor, int outlineSize, int initialX, int initialY) override;

    void update() override;

    void drawProgressBar(SDL_Renderer *renderer, int x, int y, int width, int height, float percent, SDL_Color bg_color,
                         SDL_Color fg_color);

    void render(SDL_Renderer *renderer) override;

private:
    StorageInfo getStorageInfo(const char *path);

    SDL_Color getProgressColor(float percent);

    uint32_t updateCount;
    int progressBarWidth;
    int progressBarHeight;
    int displayMode;
    float progressMin;
    float progressMax;
};

#endif //SAKURAPI_CLOCK_STORAGE_TEXT_H
