//
// Created by SJF on 2025/9/19.
//

#ifndef SAKURAPI_CLOCK_BACKGROUND_MANAGER_H
#define SAKURAPI_CLOCK_BACKGROUND_MANAGER_H

#include <SDL.h>
#include "font_manager.h"

class BackgroundManager {
public :
    static BackgroundManager &getInstance();

    void init(SDL_Renderer *renderer);

    void update();

    void render(SDL_Renderer *renderer);

    void setPreStyle(SDL_Renderer *renderer);

    void setNextStyle(SDL_Renderer *renderer);

    void setStyle(uint32_t key, SDL_Renderer *renderer);

private:
    SDL_Texture *background = nullptr;
    uint32_t currentStyle = 0;
};

#endif //SAKURAPI_CLOCK_BACKGROUND_MANAGER_H
