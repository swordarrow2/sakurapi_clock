//
// Created by SJF on 2025/9/19.
//

#include <SDL2/SDL.h>

#include "background_manager.h"
#include "texture_manager.h"
#include "text_manager.h"
#include "color_manager.h"
#include "../helper/config_manager.h"

int screenWidth;
int screenHeight;
uint32_t showedFrames = 0;
uint32_t animIndex = 0;

BackgroundManager &BackgroundManager::getInstance() {
    static BackgroundManager instance;
    return instance;
}

void BackgroundManager::init(SDL_Renderer *renderer) {
    screenWidth = ConfigManager::getInstance().getInt("hardware.screen_width");
    screenHeight = ConfigManager::getInstance().getInt("hardware.screen_height");
}

void BackgroundManager::update() {
    showedFrames++;
    if (animIndex == TextureManager::getInstance().getFrameCount() - 1) {
        animIndex = 0;
    } else {
        animIndex++;
    }
}

void BackgroundManager::render(SDL_Renderer *renderer) {
    int delay = ConfigManager::getInstance().getInt("cfg.delay");
#ifdef UBUNTU
    if (delay * 60 < showedFrames && delay > 0) {
        setNextStyle(renderer);
        showedFrames = 0;
    }
#else
    if (delay < showedFrames && delay > 0) {
        setNextStyle(renderer);
        showedFrames = 0;
    }
#endif

    background = TextureManager::getInstance().getFrameTexture(animIndex);
    if (background) {
        int texWidth, texHeight;
        SDL_QueryTexture(background, NULL, NULL, &texWidth, &texHeight);
        float scale = std::min(
            static_cast<float>(screenWidth) / texWidth,
            static_cast<float>(screenHeight) / texHeight
        );
        int scaledWidth = static_cast<int>(texWidth * scale);
        int scaledHeight = static_cast<int>(texHeight * scale);
        int x = (screenWidth - scaledWidth) / 2;
        int y = (screenHeight - scaledHeight) / 2;
        SDL_Rect dstRect = {x, y, scaledWidth, scaledHeight};
        SDL_RenderCopy(renderer, background, NULL, &dstRect);
    } else {
        std::cout << "background is null" << std::endl;
        SDL_SetRenderDrawColor(renderer, 0, 0, 0, 255);
        SDL_RenderClear(renderer);
        SDL_SetRenderDrawColor(renderer, 255, 255, 255, 255);
    }
}

void BackgroundManager::setStyle(uint32_t key, SDL_Renderer *renderer) {
    std::cout << "切换到主题: " << key << std::endl;
    showedFrames = 0;
    animIndex = 0;
    ConfigManager::getInstance().setConfigIndex(key);
    TextureManager::getInstance().init(renderer);
    ColorManager::getInstance().init();
    FontManager::getInstance().init();
    TextManager::getInstance().init();
    init(renderer);
}


void BackgroundManager::setNextStyle(SDL_Renderer *renderer) {
    if (currentStyle == ConfigManager::getInstance().getConfigCount() - 1) {
        currentStyle = 0;
    } else {
        currentStyle++;
    }
    setStyle(currentStyle, renderer);
}

void BackgroundManager::setPreStyle(SDL_Renderer *renderer) {
    if (currentStyle == 0) {
        currentStyle = ConfigManager::getInstance().getConfigCount();
    } else {
        currentStyle--;
    }
    setStyle(currentStyle, renderer);
}
