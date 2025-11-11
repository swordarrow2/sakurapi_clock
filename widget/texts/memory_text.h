//
// Created by sjf on 2025/10/4.
//

#ifndef SAKURAPI_CLOCK_MEMORY_TEXT_H
#define SAKURAPI_CLOCK_MEMORY_TEXT_H


#include "../draggable_text.h"

class MemoryText : public DraggableText {
public:
    struct MemoryInfo {
        unsigned long total;
        unsigned long free;
        unsigned long available;
        float usagePercent;
    };

    MemoryInfo memoryInfo;

    void init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
              SDL_Color outlineColor, int outlineSize, int initialX, int initialY) override;

    void update() override;

    void drawProgressBar(SDL_Renderer *renderer, int x, int y, int width, int height, float percent, SDL_Color bg_color,
                         SDL_Color fg_color);

    void render(SDL_Renderer *renderer) override;

private:
    MemoryInfo getMemoryInfo();

    uint32_t updateCount;
};


#endif //SAKURAPI_CLOCK_MEMORY_TEXT_H
