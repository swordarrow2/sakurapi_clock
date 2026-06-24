//
// Created by sjf on 2025/10/4.
//

#include "cpu_temp_text.h"

#include <iomanip>
#include <sstream>

#include "../../helper/config_manager.h"

void CpuTempText::init(const std::string &name, const std::string &text, TTF_Font *textFont, SDL_Color textColor,
                       SDL_Color outlineColor, int outlineSize, int initialX, int initialY) {
    DraggableText::init(name, text, textFont, textColor, outlineColor, outlineSize, initialX, initialY);
    updateCount = ConfigManager::getInstance().getInt("performance.max_fps");
}

void CpuTempText::update() {
    if (updateCount == ConfigManager::getInstance().getInt("performance.max_fps")) {
        float temp = getCpuTemperature();
        std::stringstream ss;
        ss << "TEMP:" << std::fixed << std::setprecision(1) << std::setw(5) << temp << "°C";
        setText(ss.str());
        updateCount = 0;
    }
    updateCount++;
}

void CpuTempText::render(SDL_Renderer *renderer) {
    DraggableText::render(renderer);
}

float CpuTempText::getCpuTemperature() {
    FILE *file = fopen("/sys/class/thermal/thermal_zone0/temp", "r");
    if (!file) return 0.0;

    int temp;
    fscanf(file, "%d", &temp);
    fclose(file);

    return temp / 1000.0f;
}
