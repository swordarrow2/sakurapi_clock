//
// Created by SJF on 2025/9/28.
//

#include <string>
#include "color_manager.h"
#include "../helper/config_manager.h"
#include "../helper/globals.h"

using std::string;

ColorManager &ColorManager::getInstance() {
    static ColorManager instance;
    return instance;
}

void ColorManager::init() {
    for (const string &str: Globals::elements) {
        colors[str + ".color"] = ConfigManager::getInstance().getColor(str + ".color");
        colors[str + ".colorOutline"] = ConfigManager::getInstance().getColor(str + ".colorOutline");
    }
}

SDL_Color ColorManager::getColor(const string &key) {
    map<string, SDL_Color>::iterator it = colors.find(key);
    if (it == colors.end()) {
        return SDL_Color{255, 255, 255, 255};
    }
    return it->second;
}
