//
// Created by SJF on 2025/9/28.
//

#ifndef SAKURAPI_CLOCK_FONT_MANAGER_H
#define SAKURAPI_CLOCK_FONT_MANAGER_H

#include <map>
#include <SDL_ttf.h>
#include <string>

class FontManager {
public:
    static FontManager &getInstance();

    void init();

    TTF_Font *getFont(const std::string &name);

    FontManager(const FontManager &) = delete;

    FontManager &operator=(const FontManager &) = delete;

private:
    std::map<const std::string, TTF_Font *> fonts;

    FontManager() = default;
};

#endif //SAKURAPI_CLOCK_FONT_MANAGER_H
