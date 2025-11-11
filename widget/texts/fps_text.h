//
// Created by sjf on 2025/10/3.
//

#ifndef SAKURAPI_CLOCK_CLOCK_FPSTEXT_H
#define SAKURAPI_CLOCK_CLOCK_FPSTEXT_H

#include "../draggable_text.h"
#include <SDL.h>
#include <sstream>
#include <iomanip>

class FpsText : public DraggableText {
private:
    uint32_t fpsUpdateTime = SDL_GetTicks();
    int frameCount = 0;
    float fps = 0.0f;

public:
    void init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
              SDL_Color outlineColor, int outlineSize, int initialX, int initialY) override;

    void update() override;
};

#endif //SAKURAPI_CLOCK_CLOCK_FPSTEXT_H
