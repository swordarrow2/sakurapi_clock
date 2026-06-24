//
// Created by sjf on 2025/10/4.
//

#ifndef SAKURAPI_CLOCK_CPU_USAGE_TEXT_H
#define SAKURAPI_CLOCK_CPU_USAGE_TEXT_H

#include "../draggable_text.h"

class CpuUsageText : public DraggableText {
public:
    struct CpuInfo {
        float usage;
    };

    CpuInfo cpuInfo;

    void init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
              SDL_Color outlineColor, int outlineSize, int initialX, int initialY) override;

    void update() override;

    void render(SDL_Renderer *renderer) override;

private:
    float getCpuUsage();

    SDL_Color getProgressColor(float percent);

    void drawProgressBar(SDL_Renderer *renderer, int x, int y, int width, int height, float percent);

    uint32_t updateCount;
    int progressBarWidth;
    int progressBarHeight;
    int displayMode;
    float progressMin;
    float progressMax;
};


#endif //SAKURAPI_CLOCK_CPU_USAGE_TEXT_H
