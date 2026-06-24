//
// Created by sjf on 2025/10/3.
//

#include "time_text.h"
#include <ctime>
#include <sstream>

void TimeText::init(const std::string &name, const std::string &text, TTF_Font *textFont,
                    SDL_Color textColor, SDL_Color outlineColor, int outlineSize,
                    int initialX, int initialY) {
    DraggableText::init(name, text, textFont, textColor, outlineColor, outlineSize, initialX, initialY);
}

void TimeText::update() {
    time_t now = time(NULL);
    struct tm *t = localtime(&now);

    std::ostringstream timeStream;
    timeStream << std::setfill('0') << std::setw(2) << t->tm_hour << ":"
            << std::setfill('0') << std::setw(2) << t->tm_min << ":"
            << std::setfill('0') << std::setw(2) << t->tm_sec;

    setText(timeStream.str());
}
