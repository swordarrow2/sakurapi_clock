#include "draggable_text.h"
#include <iostream>

void DraggableText::init(const std::string &name, const std::string &text, TTF_Font *textFont,
                         SDL_Color textColor, SDL_Color outlineColor, int outlineSize, int initialX, int initialY) {
    this->name = name;
    this->text = text;
    this->font = textFont;
    this->textColor = textColor;
    this->outlineColor = outlineColor;
    this->outlineSize = outlineSize;
    this->texture = nullptr;
    this->isDragging = false;
    this->dragOffsetX = 0;
    this->dragOffsetY = 0;
    rect.x = initialX;
    rect.y = initialY;
    rect.w = 0;
    rect.h = 0;
}

DraggableText::~DraggableText() {
    if (texture) {
        SDL_DestroyTexture(texture);
    }
}

void DraggableText::setText(const std::string &newText) {
    if (text != newText) {
        text = newText;
        if (texture) {
            SDL_DestroyTexture(texture);
        }
        texture = nullptr;
    }
}

void DraggableText::setPosition(int x, int y) {
    rect.x = x;
    rect.y = y;
}

void DraggableText::setTextColor(SDL_Color newColor) {
    if ((textColor.r != newColor.r) || (textColor.g != newColor.g) || (textColor.b != newColor.b) || (
            textColor.a != newColor.a)) {
        textColor = newColor;
        if (texture) {
            SDL_DestroyTexture(texture);
            texture = nullptr;
        }
    }
}

void DraggableText::setOutlineColor(SDL_Color newColor) {
    if ((outlineColor.r != newColor.r) || (outlineColor.g != newColor.g) || (outlineColor.b != newColor.b) || (
            outlineColor.a != newColor.a)) {
        outlineColor = newColor;
        if (texture) {
            SDL_DestroyTexture(texture);
            texture = nullptr;
        }
    }
}

void DraggableText::setOutlineSize(int size) {
    outlineSize = size;
}

void DraggableText::setFont(TTF_Font *font) {
    this->font = font;
}

bool DraggableText::handleEvent(SDL_Event *e) {
    if (e->type == SDL_MOUSEBUTTONDOWN) {
        // 检查鼠标是否在文本区域内
        int x, y;
        SDL_GetMouseState(&x, &y);
        if (x >= rect.x && x <= rect.x + rect.w && y >= rect.y && y <= rect.y + rect.h) {
            isDragging = true;
            dragOffsetX = x - rect.x;
            dragOffsetY = y - rect.y;
            return true;
        }
    } else if (e->type == SDL_MOUSEBUTTONUP) {
        if (isDragging) {
            isDragging = false;
            if (mouseButtonUpCallback) {
                mouseButtonUpCallback(rect.x, rect.y);
            }
            return true;
        }
    } else if (e->type == SDL_MOUSEMOTION) {
        if (isDragging) {
            int x, y;
            SDL_GetMouseState(&x, &y);
            rect.x = x - dragOffsetX;
            rect.y = y - dragOffsetY;
            return true;
        }
    }
    return false;
}

void DraggableText::render(SDL_Renderer *renderer) {
    if (!texture) {
        // createTexture(renderer);
        createOutlinedTextTexture(renderer, textColor, outlineColor, outlineSize);
    }
    if (texture) {
        SDL_RenderCopy(renderer, texture, NULL, &rect);
    }
}

SDL_Rect DraggableText::getRect() const {
    return rect;
}

std::string DraggableText::getText() const {
    return text;
}

void DraggableText::setMouseButtonUpCallback(std::function<void(int, int)> callback) {
    mouseButtonUpCallback = callback;
}

std::string DraggableText::getName() const {
    return name;
}

void DraggableText::createTexture(SDL_Renderer *renderer) {
    if (texture) {
        SDL_DestroyTexture(texture);
    }

    SDL_Surface *textSurface = TTF_RenderText_Blended(font, text.c_str(), textColor);
    if (textSurface) {
        texture = SDL_CreateTextureFromSurface(renderer, textSurface);
        rect.w = textSurface->w;
        rect.h = textSurface->h;
        SDL_FreeSurface(textSurface);
    }
}


void DraggableText::updateTexture(SDL_Renderer *renderer) {
    // createTexture(renderer);
    createOutlinedTextTexture(renderer, textColor, outlineColor, outlineSize);
}

void DraggableText::createOutlinedTextTexture(SDL_Renderer *renderer,
                                              SDL_Color textColor,
                                              SDL_Color outlineColor, int outlineSize) {
    // 获取原始文字尺寸
    int textWidth, textHeight;
    if (TTF_SizeText(font, text.c_str(), &textWidth, &textHeight) != 0 || textWidth == 0) {
        return;
    }
    // 计算带描边的纹理尺寸
    int totalWidth = textWidth + outlineSize * 2;
    int totalHeight = textHeight + outlineSize * 2;
    // 创建透明背景的表面
    SDL_Surface *finalSurface = SDL_CreateRGBSurfaceWithFormat(0, totalWidth, totalHeight, 32, SDL_PIXELFORMAT_RGBA32);
    if (!finalSurface) {
        return;
    }
    // 设置表面为透明
    SDL_FillRect(finalSurface, NULL, SDL_MapRGBA(finalSurface->format, 0, 0, 0, 0));
    // 1. 先渲染描边文字（在多个偏移位置）
    SDL_Surface *outlineSurface = TTF_RenderText_Blended(font, text.c_str(), outlineColor);
    if (outlineSurface) {
        // 在8个方向绘制描边
        int directions[8][2] = {
            {-outlineSize, -outlineSize}, {0, -outlineSize}, {outlineSize, -outlineSize},
            {-outlineSize, 0}, {outlineSize, 0},
            {-outlineSize, outlineSize}, {0, outlineSize}, {outlineSize, outlineSize}
        };
        for (auto &direction: directions) {
            SDL_Rect destRect = {
                outlineSize + direction[0],
                outlineSize + direction[1],
                textWidth,
                textHeight
            };
            SDL_BlitSurface(outlineSurface, NULL, finalSurface, &destRect);
        }
        SDL_FreeSurface(outlineSurface);
    }
    // 2. 再渲染主体文字（在中心位置）
    SDL_Surface *textSurface = TTF_RenderText_Blended(font, text.c_str(), textColor);
    if (textSurface) {
        SDL_Rect destRect = {outlineSize, outlineSize, textWidth, textHeight};
        SDL_BlitSurface(textSurface, NULL, finalSurface, &destRect);
        SDL_FreeSurface(textSurface);
    }
    if (texture) {
        SDL_DestroyTexture(texture);
    }
    // 转换为纹理
    texture = SDL_CreateTextureFromSurface(renderer, finalSurface);
    rect.w = totalWidth;
    rect.h = totalHeight;

    SDL_FreeSurface(finalSurface);
}
