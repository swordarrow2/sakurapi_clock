//
// Created by SJF on 2025/9/28.
//

#ifndef SAKURAPI_CLOCK_COLOR_MANAGER_H
#define SAKURAPI_CLOCK_COLOR_MANAGER_H

#include <map>
#include <SDL.h>
#include <vector>

class ColorManager {
public:
    static ColorManager &getInstance();

    void init();

    SDL_Color getColor(const std::string &key);

private:
    std::map<std::string, SDL_Color> colors;

    ColorManager() = default;

    ColorManager(const ColorManager &) = delete;

    ColorManager &operator=(const ColorManager &) = delete;
};

#endif //SAKURAPI_CLOCK_COLOR_MANAGER_H
