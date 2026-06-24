//
// Created by sjf on 2025/10/3.
//

#include "fps_text.h"

void FpsText::init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
                   SDL_Color outlineColor, int outlineSize, int initialX, int initialY) {
    DraggableText::init(name, text, textFont, textColor, outlineColor, outlineSize, initialX, initialY);
}

void FpsText::update() {
    frameCount++;
    if (SDL_GetTicks() - fpsUpdateTime >= 1000) {
        fps = frameCount * 1000.0f / (SDL_GetTicks() - fpsUpdateTime);
        fpsUpdateTime = SDL_GetTicks();
        frameCount = 0;
    }

    std::ostringstream fpsStream;
    fpsStream.precision(2);
    fpsStream << std::fixed << fps << " FPS";
    setText(fpsStream.str());
}
