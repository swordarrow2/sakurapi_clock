//
// Created by SJF on 2025/9/17.
//
#include "config_manager.h"
#include <iostream>
#include <algorithm>
#include <cctype>
#include <dirent.h>
#include <regex>
#include <sstream>

#include "globals.h"

using std::string;
using std::endl;
using std::cerr;
using std::cout;
using std::map;

// All known configuration keys used by the application
static const vector<string> knownConfigKeys = {
    // --- Global / System ---
    "cfg.name",
    "cfg.delay",
    "hardware.screen_width",
    "hardware.screen_height",
    "gui.show_cursor",
    "performance.max_fps",
    "background.dir",
    "background.file",

    // --- Common text component keys (font, size, color, colorOutline, outlineSize, x, y) ---
    // time
    "time.font", "time.size", "time.color", "time.colorOutline", "time.outlineSize", "time.x", "time.y",
    // date
    "date.font", "date.size", "date.color", "date.colorOutline", "date.outlineSize", "date.x", "date.y",
    // fps
    "fps.font", "fps.size", "fps.color", "fps.colorOutline", "fps.outlineSize", "fps.x", "fps.y",
    // cpu_usage
    "cpu_usage.font", "cpu_usage.size", "cpu_usage.color", "cpu_usage.colorOutline", "cpu_usage.outlineSize", "cpu_usage.x", "cpu_usage.y",
    // cpu_temp
    "cpu_temp.font", "cpu_temp.size", "cpu_temp.color", "cpu_temp.colorOutline", "cpu_temp.outlineSize", "cpu_temp.x", "cpu_temp.y",
    // memory_state
    "memory_state.font", "memory_state.size", "memory_state.color", "memory_state.colorOutline", "memory_state.outlineSize", "memory_state.x", "memory_state.y",
    // storage_state
    "storage_state.font", "storage_state.size", "storage_state.color", "storage_state.colorOutline", "storage_state.outlineSize", "storage_state.x", "storage_state.y",
    // ip_state
    "ip_state.font", "ip_state.size", "ip_state.color", "ip_state.colorOutline", "ip_state.outlineSize", "ip_state.x", "ip_state.y",
    "ip_state.interface",

    // --- Component-specific keys ---

    // cpu_usage
    "cpu_usage.mode",
    "cpu_usage.progress_width",
    "cpu_usage.progress_height",
    "cpu_usage.progress_min",
    "cpu_usage.progress_max",

    // memory_state
    "memory_state.mode",
    "memory_state.progress_width",
    "memory_state.progress_height",
    "memory_state.progress_min",
    "memory_state.progress_max",

    // storage_state
    "storage_state.mode",
    "storage_state.progress_width",
    "storage_state.progress_height",
    "storage_state.progress_min",
    "storage_state.progress_max",
};

const vector<string> &ConfigManager::getKnownConfigKeys() {
    return knownConfigKeys;
}

ConfigManager &ConfigManager::getInstance() {
    static ConfigManager instance;
    return instance;
}

bool ConfigManager::loadConfigFromTarDirectory(const string &directory) {
    DIR *dir = opendir(directory.c_str());
    if (!dir) {
        cerr << "Cannot open directory: " << directory << endl;
        return false;
    }

    dirent *entry;
    while ((entry = readdir(dir)) != nullptr) {
        string filename = entry->d_name;
        if (!Globals::themeName.empty()) {
            filename = Globals::themeName + ".tar";
        }
        if (filename.length() > 4 && filename.substr(filename.length() - 4) == ".tar") {
            string fullPath = directory + "/" + filename;
            cout << "Scanning tar file: " << fullPath << endl;

            string configContent = MemoryTarFileSystem::readConfigFromTar(fullPath);
            if (configContent.empty()) {
                cerr << "config.ini not found in tar file: " << filename << endl;
                continue;
            }
            map<string, string> configMap;
            if (!parseConfigFromString(configContent, configMap)) {
                cerr << "Failed to parse config: " << filename << endl;
                continue;
            }
            configDatas.push_back(configMap);
            tarFileSystems.push_back(nullptr);  // Placeholder, loaded on demand
            tarFilePaths.push_back(fullPath);
            cout << "Successfully recorded config from: " << filename << endl;
            if (!Globals::themeName.empty()) {
                break;
            }
        }
    }

    closedir(dir);

    if (configDatas.empty()) {
        cerr << "No valid tar config files found" << endl;
        return false;
    }
    setConfigIndex(0);
    cout << "Loaded " << configDatas.size() << " configs total" << endl;
    return true;
}

bool ConfigManager::parseConfigFromString(const string &configContent, map<string, string> &configMap) {
    std::istringstream iss(configContent);
    string line;
    string currentSection;

    while (std::getline(iss, line)) {
        line.erase(0, line.find_first_not_of(" \t\r\n"));
        line.erase(line.find_last_not_of(" \t\r\n") + 1);
        if (line.empty() || line[0] == ';' || line[0] == '#') {
            continue;
        }
        if (line[0] == '[' && line[line.size() - 1] == ']') {
            currentSection = line.substr(1, line.size() - 2);
            configMap[currentSection] = currentSection;
            continue;
        }
        size_t delimiterPos = line.find('=');
        if (delimiterPos != string::npos) {
            string key = line.substr(0, delimiterPos);
            string value = line.substr(delimiterPos + 1);
            key.erase(0, key.find_first_not_of(" \t\r\n"));
            key.erase(key.find_last_not_of(" \t\r\n") + 1);
            value.erase(0, value.find_first_not_of(" \t\r\n"));
            value.erase(value.find_last_not_of(" \t\r\n") + 1);
            if (!currentSection.empty() && key.find(currentSection + ".") != 0) {
                key = currentSection + "." + key;
            }
            configMap[key] = value;
            cout << "Loading config: [" << key << " = " << value << "]" << endl;
        }
    }

    return !configMap.empty();
}

void ConfigManager::setConfigIndex(uint32_t index) {
    if (index >= 0 && index < configDatas.size()) {
        // Unload previous tar when switching themes to free memory
        if (index != currentIndex && currentIndex < tarFileSystems.size() && tarFileSystems[currentIndex]) {
            tarFileSystems[currentIndex].reset();
            std::cout << "Unloaded tar file for theme " << currentIndex << std::endl;
        }
        currentIndex = index;
        cout << "Switched to config index: " << index << endl;
    }
}

string ConfigManager::getString(const string &key, const string &defaultValue) const {
    if (currentIndex < configDatas.size()) {
        auto it = configDatas[currentIndex].find(key);
        if (it != configDatas[currentIndex].end()) {
            return it->second;
        }
    }
    return defaultValue;
}

int32_t ConfigManager::getInt(const string &key, int defaultValue) const {
    if (currentIndex < configDatas.size()) {
        auto it = configDatas[currentIndex].find(key);
        if (it != configDatas[currentIndex].end()) {
            try {
#ifdef DEV
                if ("gui.show_cursor" == key) {
                    return 1;
                }
                unsigned long ul = std::stoul(it->second, nullptr, 0);
                if ("performance.max_fps" == key || "cfg.delay" == key) {
                    ul *= 60;
                }
                return (int32_t) ul;
#endif
                return (int32_t) std::stoul(it->second, nullptr, 0);
            } catch (const std::exception &e) {
                cerr << "Config value '" << it->second << "' cannot be converted to integer: " << e.what() << endl;
            }
        }
    }
    return defaultValue;
}

float ConfigManager::getFloat(const string &key, float defaultValue) const {
    if (currentIndex < configDatas.size()) {
        auto it = configDatas[currentIndex].find(key);
        if (it != configDatas[currentIndex].end()) {
            try {
                return std::stof(it->second);
            } catch (const std::exception &e) {
                cerr << "Config value '" << it->second << "' cannot be converted to float: " << e.what() << endl;
            }
        }
    }
    return defaultValue;
}

MemoryTarFileSystem *ConfigManager::getCurrentTarFileSystem() {
    if (currentIndex < tarFileSystems.size()) {
        // Lazy load: only load tar content into memory when needed
        if (!tarFileSystems[currentIndex] && currentIndex < tarFilePaths.size()) {
            std::cout << "Lazy loading tar file: " << tarFilePaths[currentIndex] << std::endl;
            auto tarFS = std::make_unique<MemoryTarFileSystem>();
            if (tarFS->loadTar(tarFilePaths[currentIndex])) {
                tarFileSystems[currentIndex] = std::move(tarFS);
            } else {
                std::cerr << "Lazy loading tar file failed: " << tarFilePaths[currentIndex] << std::endl;
            }
        }
        return tarFileSystems[currentIndex].get();
    }
    return nullptr;
}

size_t ConfigManager::getConfigCount() const {
    return configDatas.size();
}

SDL_Color ConfigManager::getColor(const string &key) const {
    if (currentIndex < configDatas.size()) {
        map<string, string>::const_iterator it = configDatas[currentIndex].find(key);
        if (it != configDatas[currentIndex].end()) {
            return parseColorString(it->second);
        }
    }
    return SDL_Color{255, 255, 255, 255};
}

SDL_Color ConfigManager::parseColorString(const string &colorStr) const {
    // Remove all whitespace characters
    string cleanedStr = colorStr;
    cleanedStr.erase(std::remove_if(cleanedStr.begin(), cleanedStr.end(),
                                    [](unsigned char c) { return std::isspace(c); }),
                     cleanedStr.end());
    // Check if hex format (0xFFFFFFFF or FFFFFFFF)
    if (cleanedStr.length() >= 6) {
    // Handle 0x prefix
        if (cleanedStr.compare(0, 2, "0x") == 0 || cleanedStr.compare(0, 2, "0X") == 0) {
            cleanedStr = cleanedStr.substr(2);
        }
        // Check if valid hex string
        bool isHex = true;
        for (char c: cleanedStr) {
            if (!std::isxdigit(c)) {
                isHex = false;
                break;
            }
        }
        if (isHex) {
            try {
                uint32_t color = std::stoul(cleanedStr, nullptr, 16);
                // Adjust based on length
                if (cleanedStr.length() == 6) {
                    // RRGGBB: shift left 8 bits and set alpha to FF
                    color = (color << 8) | 0xFF;
                } else if (cleanedStr.length() == 8) {
                    // 8-bit hex, use directly
                } else {
                    cerr << "Hex color value '" << colorStr << "' has incorrect length, expected 6 or 8 digits" << endl;
                    return SDL_Color{255, 255, 255, 255};
                }
                return SDL_Color{
                    static_cast<uint8_t>((color >> 24) & 0xFF),
                    static_cast<uint8_t>((color >> 16) & 0xFF),
                    static_cast<uint8_t>((color >> 8) & 0xFF),
                    static_cast<uint8_t>((color >> 0) & 0xFF)
                };
            } catch (const std::exception &e) {
                cerr << "Failed to parse hex color value '" << colorStr << "': " << e.what() << endl;
                return SDL_Color{255, 255, 255, 255};
            }
        }
    }
    // Check if RGBA decimal format (e.g. "29,15,66,74" or "29,15,66")
    std::regex rgbaRegex(R"(^(\d+),(\d+),(\d+)(?:,(\d+))?$)");
    std::smatch match;

    if (std::regex_match(cleanedStr, match, rgbaRegex)) {
        try {
            uint8_t r = static_cast<uint8_t>(std::stoi(match[1]));
            uint8_t g = static_cast<uint8_t>(std::stoi(match[2]));
            uint8_t b = static_cast<uint8_t>(std::stoi(match[3]));
            uint8_t a = match[4].matched ? static_cast<uint8_t>(std::stoi(match[4])) : 255;
            return SDL_Color{r, g, b, a};
        } catch (const std::exception &e) {
            cerr << "Failed to parse color value '" << colorStr << "': " << e.what() << endl;
            return SDL_Color{255, 255, 255, 255};
        }
    }

    cerr << "Unrecognized color format: " << colorStr << " (supported formats: 0xRRGGBBAA, RRGGBBAA, R,G,B,A or R,G,B)" << endl;
    return SDL_Color{255, 255, 255, 255};
}
