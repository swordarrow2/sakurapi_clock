#include "text_manager.h"
#include <string>
#include <iostream>
#include <sstream>
#include <vector>
#include <iomanip>

#include "../helper/config_manager.h"
#include "color_manager.h"
#include "font_manager.h"
#include "../helper/globals.h"
#include "../widget/texts/time_text.h"
#include "../widget/texts/date_text.h"
#include "../widget/texts/fps_text.h"
#include "../widget/texts/cpu_text.h"
#include "../widget/texts/memory_text.h"
#include "../widget/texts/storage_text.h"

using std::vector;

TextManager &TextManager::getInstance() {
    static TextManager instance;
    return instance;
}

void TextManager::handleEvent(SDL_Event *event) {
    for (std::pair<const string, DraggableText *> &text: texts) {
        if (text.second->handleEvent(event)) {
            break;
        }
    }
}

void TextManager::update() {
    for (auto &text: texts) {
        text.second->update();
    }
}

void TextManager::render(SDL_Renderer *renderer) {
    for (std::pair<const string, DraggableText *> &text: texts) {
        text.second->render(renderer);
    }
}

void TextManager::init() {
    ConfigManager &cfgManager = ConfigManager::getInstance();
    ColorManager &colorManager = ColorManager::getInstance();
    FontManager &fontManager = FontManager::getInstance();

    for (auto &name: Globals::elements) {
        map<string, DraggableText *>::iterator it = texts.find(name);
        if (it == texts.end()) {
            if (cfgManager.getString(name).empty() && cfgManager.getString(name + ".font").empty()) {
                continue;
            }
            DraggableText *text = nullptr;
            if (name == Globals::key_time) {
                text = new TimeText();
            } else if (name == Globals::key_date) {
                text = new DateText();
            } else if (name == Globals::key_fps) {
                text = new FpsText();
            } else if (name == Globals::key_cpu_state) {
                text = new CpuText();
            } else if (name == Globals::key_memory_state) {
                text = new MemoryText();
            } else if (name == Globals::key_storage_state) {
                text = new StorageText();
            }

            if (text) {
                text->init(name, "", fontManager.getFont(name),
                           cfgManager.getColor(name + ".color"),
                           cfgManager.getColor(name + ".colorOutline"),
                           cfgManager.getInt(name + ".outlineSize", 2),
                           cfgManager.getInt(name + ".x"), cfgManager.getInt(name + ".y"));
                text->setMouseButtonUpCallback([name](int x, int y) {
                    std::cout << "text " << name << " @ x=" << x << ",y=" << y << std::endl;
                });
            }
            texts[name] = text;
        } else {
            DraggableText *text = it->second;
            if (fontManager.getFont(name) == nullptr ) {
                text->~DraggableText();
                texts.erase(name);
                continue;
            }
            text->setPosition(cfgManager.getInt(name + ".x"), cfgManager.getInt(name + ".y"));
            text->setTextColor(colorManager.getColor(name + ".color"));
            text->setOutlineColor(cfgManager.getColor(name + ".colorOutline"));
            text->setOutlineSize(cfgManager.getInt(name + ".outlineSize", 2));
            text->setFont(fontManager.getFont(name));
        }
    }
}
