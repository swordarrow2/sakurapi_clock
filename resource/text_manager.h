#ifndef SAKURAPI_CLOCK_FONT_LOADER_H
#define SAKURAPI_CLOCK_FONT_LOADER_H

#include <map>
#include <SDL.h>
#include <vector>

#include "../widget/draggable_text.h"

class TextManager {
public:
    static TextManager &getInstance();

    TextManager(const TextManager &) = delete;

    TextManager &operator=(const TextManager &) = delete;

    void update();

    void render(SDL_Renderer *renderer);

    void handleEvent(SDL_Event *e);

    void init();

private:
    std::map<std::string, DraggableText *> texts;

    TextManager() = default;
};

#endif // SAKURAPI_CLOCK_FONT_LOADER_H
