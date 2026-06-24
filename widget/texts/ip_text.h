//
// Created by sjf on 2025/10/4.
//

#ifndef SAKURAPI_CLOCK_IP_TEXT_H
#define SAKURAPI_CLOCK_IP_TEXT_H

#include "../draggable_text.h"
#include <vector>
#include <string>

class IpText : public DraggableText {
public:
    struct IpInfo {
        std::string interface;
        std::string ip;
    };

    std::vector<IpInfo> ipList;

    void init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
              SDL_Color outlineColor, int outlineSize, int initialX, int initialY) override;

    void update() override;

    void render(SDL_Renderer *renderer) override;

private:
    void getIpAddresses();

    std::string filterInterface;
    uint32_t updateCount;
};


#endif //SAKURAPI_CLOCK_IP_TEXT_H
