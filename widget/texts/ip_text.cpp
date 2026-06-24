//
// Created by sjf on 2025/10/4.
//

#include "ip_text.h"

#include <ifaddrs.h>
#include <arpa/inet.h>
#include <net/if.h>
#include <sstream>
#include "../../helper/config_manager.h"

void IpText::init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
                  SDL_Color outlineColor, int outlineSize, int initialX, int initialY) {
    DraggableText::init(name, text, textFont, textColor, outlineColor, outlineSize, initialX, initialY);
    updateCount = 0;
    filterInterface = ConfigManager::getInstance().getString("ip_state.interface", "");
    getIpAddresses();
}

void IpText::update() {
    int max_fps = ConfigManager::getInstance().getInt("performance.max_fps");
    if (updateCount >= max_fps) {
        getIpAddresses();
        updateCount = 0;
    }
    updateCount++;
}

void IpText::render(SDL_Renderer *renderer) {
    DraggableText::render(renderer);
}

void IpText::getIpAddresses() {
    ipList.clear();

    ifaddrs *ifaddr, *ifa;
    if (getifaddrs(&ifaddr) == -1) {
        return;
    }

    for (ifa = ifaddr; ifa != nullptr; ifa = ifa->ifa_next) {
        if (ifa->ifa_addr == nullptr) {
            continue;
        }

        if (ifa->ifa_addr->sa_family == AF_INET) {
            if (!(ifa->ifa_flags & IFF_LOOPBACK)) {
                // If a specific interface is configured, skip others
                if (!filterInterface.empty() && ifa->ifa_name != filterInterface) {
                    continue;
                }

                IpInfo info;
                info.interface = ifa->ifa_name;

                char ip[INET_ADDRSTRLEN];
                auto *sa = (struct sockaddr_in *)ifa->ifa_addr;
                inet_ntop(AF_INET, &sa->sin_addr, ip, INET_ADDRSTRLEN);
                info.ip = ip;

                ipList.push_back(info);
            }
        }
    }

    freeifaddrs(ifaddr);

    std::stringstream ss;
    if (ipList.empty()) {
        ss << "IP: N/A";
    } else {
        for (size_t i = 0; i < ipList.size(); ++i) {
            if (i > 0) ss << "\n";
            ss << ipList[i].interface << ": " << ipList[i].ip;
        }
    }
    setText(ss.str());
}
