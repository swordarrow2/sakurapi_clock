//
// Created by sjf on 2025/10/3.
//

#include "date_text.h"
#include <ctime>
#include <sstream>

void DateText::init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
                    SDL_Color outlineColor, int outlineSize, int initialX, int initialY) {
    DraggableText::init(name, text, textFont, textColor, outlineColor, outlineSize, initialX, initialY);
}

void DateText::update() {
    time_t now = time(NULL);
    struct tm *t = localtime(&now);

    std::ostringstream dateStream;
    dateStream << (t->tm_year + 1900) << "-"
            << std::setfill('0') << std::setw(2) << (t->tm_mon + 1) << "-"
            << std::setfill('0') << std::setw(2) << t->tm_mday;

    setText(dateStream.str());
}
