#ifndef SAKURAPI_CLOCK_DRAGGABLE_TEXT_H
#define SAKURAPI_CLOCK_DRAGGABLE_TEXT_H

#include <SDL.h>
#include <SDL_ttf.h>
#include <string>
#include <functional>

class DraggableText {
public:
    virtual void init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
                      SDL_Color outlineColor, int outlineSize, int initialX, int initialY);

    virtual ~DraggableText() =0;

    void setText(const std::string &newText);

    void setPosition(int x, int y);

    void setTextColor(SDL_Color newColor);

    void setOutlineColor(SDL_Color newColor);

    void setOutlineSize(int size);

    void setFont(TTF_Font *font);

    bool handleEvent(SDL_Event *e);

    virtual void render(SDL_Renderer *renderer);

    SDL_Rect getRect() const;

    std::string getText() const;

    void setMouseButtonUpCallback(std::function<void(int, int)> callback);

    std::string getName() const;

    virtual void update() =0;

protected:
    std::string text;
    std::string name;
    TTF_Font *font;
    SDL_Color textColor;
    SDL_Color outlineColor;
    SDL_Texture *texture;
    SDL_Rect rect;

    bool isDragging;
    int dragOffsetX;
    int dragOffsetY;
    int outlineSize;


    std::function<void(int, int)> mouseButtonUpCallback;

    void createTexture(SDL_Renderer *renderer);

    void updateTexture(SDL_Renderer *renderer);

    void createOutlinedTextTexture(SDL_Renderer *renderer, SDL_Color textColor,
                                   SDL_Color outlineColor, int outlineSize);
};

#endif // SAKURAPI_CLOCK_DRAGGABLE_TEXT_H
