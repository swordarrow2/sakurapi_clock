//
// Created by SJF on 2025/9/28.
//

#include "font_manager.h"
#include <string>
#include <stdexcept>

#include "../helper/config_manager.h"
#include "../helper/tar_filesystem.h"
#include "../helper/globals.h"

FontManager &FontManager::getInstance() {
    static FontManager instance;
    return instance;
}

void FontManager::init() {
    MemoryTarFileSystem *tarFS = ConfigManager::getInstance().getCurrentTarFileSystem();
    if (tarFS) {
        for (auto &name: Globals::elements) {
            TTF_Font *font = fonts[name];
            if (font) {
                TTF_CloseFont(font);
                fonts.erase(name);
            }
            font = tarFS->loadFont(ConfigManager::getInstance().getString(name + ".font"),
                                   ConfigManager::getInstance().getInt(name + ".size"));
            if (!font) {
                std::cout << "Failed to load font: " << ConfigManager::getInstance().getString(name + ".font") <<
                        std::endl;
                continue;
            }
            fonts[name] = font;
        }
    }
}

TTF_Font *FontManager::getFont(const std::string &name) {
    map<const string, _TTF_Font *>::iterator it = fonts.find(name);
    if (it == fonts.end()) {
        return nullptr;
    }
    return it->second;
}
