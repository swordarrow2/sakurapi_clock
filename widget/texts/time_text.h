//
// Created by sjf on 2025/10/3.
//

#ifndef SAKURAPI_CLOCK_CLOCK_TIMETEXT_H
#define SAKURAPI_CLOCK_CLOCK_TIMETEXT_H

#include "../draggable_text.h"
#include <iomanip>
#include <SDL_pixels.h>

class DraggableText;

class TimeText : public DraggableText {
public:
    void init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
              SDL_Color outlineColor, int outlineSize, int initialX, int initialY) override;

    void update() override;
};

#endif //SAKURAPI_CLOCK_CLOCK_TIMETEXT_H
