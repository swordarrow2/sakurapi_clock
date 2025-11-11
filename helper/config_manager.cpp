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

ConfigManager &ConfigManager::getInstance() {
    static ConfigManager instance;
    return instance;
}

bool ConfigManager::loadConfigFromTarDirectory(const string &directory) {
    DIR *dir = opendir(directory.c_str());
    if (!dir) {
        cerr << "无法打开目录: " << directory << endl;
        return false;
    }

    struct dirent *entry;
    while ((entry = readdir(dir)) != nullptr) {
        string filename = entry->d_name;
        if (!Globals::themeName.empty()) {
            filename = Globals::themeName + ".tar";
        }
        if (filename.length() > 4 && filename.substr(filename.length() - 4) == ".tar") {
            string fullPath = directory + "/" + filename;
            cout << "加载tar文件: " << fullPath << endl;
            auto tarFS = std::make_unique<MemoryTarFileSystem>();
            for (const auto &filename: tarFS->listFiles()) {
                std::cout << " - " << filename << std::endl;
            }
            if (tarFS->loadTar(fullPath)) {
                string configContent = tarFS->getFileAsString("config.ini");
                if (configContent.empty()) {
                    cerr << "在tar文件中未找到config.ini: " << filename << endl;
                    continue;
                }
                map<string, string> configMap;
                if (!parseConfigFromString(configContent, configMap)) {
                    cerr << "解析配置失败: " << filename << endl;
                    continue;
                }
                configDatas.push_back(configMap);
                tarFileSystems.push_back(std::move(tarFS));
                cout << "成功加载配置从: " << filename << endl;
                if (!Globals::themeName.empty()) {
                    break;
                }
                continue;
            }
            cerr << "加载tar文件失败: " << filename << endl;
        }
    }

    closedir(dir);

    if (configDatas.empty()) {
        cerr << "未找到任何有效的tar配置文件" << endl;
        return false;
    }
    setConfigIndex(0);
    cout << "总共加载 " << configDatas.size() << " 个配置" << endl;
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
            if (!currentSection.empty()) {
                key = currentSection + "." + key;
            }
            configMap[key] = value;
            cout << "加载配置: [" << key << " = " << value << "]" << endl;
        }
    }

    return !configMap.empty();
}

void ConfigManager::setConfigIndex(uint32_t index) {
    if (index >= 0 && index < configDatas.size()) {
        currentIndex = index;
        cout << "切换到配置索引: " << index << endl;
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
                unsigned long ul = std::stoul(it->second, nullptr, 0);
                return (int32_t) ul;
            } catch (const std::exception &e) {
                cerr << "配置值 '" << it->second << "' 无法转换为整数: " << e.what() << endl;
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
                cerr << "配置值 '" << it->second << "' 无法转换为浮点数: " << e.what() << endl;
            }
        }
    }
    return defaultValue;
}

MemoryTarFileSystem *ConfigManager::getCurrentTarFileSystem() {
    if (currentIndex < tarFileSystems.size()) {
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
    // 移除所有空白字符
    string cleanedStr = colorStr;
    cleanedStr.erase(std::remove_if(cleanedStr.begin(), cleanedStr.end(),
                                    [](unsigned char c) { return std::isspace(c); }),
                     cleanedStr.end());
    // 检查是否是十六进制格式 (0xFFFFFFFF 或 FFFFFFFF)
    if (cleanedStr.length() >= 6) {
        // 处理 0x 前缀
        if (cleanedStr.compare(0, 2, "0x") == 0 || cleanedStr.compare(0, 2, "0X") == 0) {
            cleanedStr = cleanedStr.substr(2);
        }
        // 检查是否是有效的十六进制字符串
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
                // 根据长度调整
                if (cleanedStr.length() == 6) {
                    // 6位十六进制，添加Alpha通道
                    color = (0xFF << 24) | color;
                } else if (cleanedStr.length() == 8) {
                    // 8位十六进制，直接使用
                } else {
                    cerr << "十六进制颜色值 '" << colorStr << "' 长度不正确，应为6或8位" << endl;
                    return SDL_Color{255, 255, 255, 255};
                }
                return SDL_Color{
                    static_cast<uint8_t>((color >> 24) & 0xFF),
                    static_cast<uint8_t>((color >> 16) & 0xFF),
                    static_cast<uint8_t>((color >> 8) & 0xFF),
                    static_cast<uint8_t>((color >> 0) & 0xFF)
                };
            } catch (const std::exception &e) {
                cerr << "十六进制颜色值 '" << colorStr << "' 解析失败: " << e.what() << endl;
                return SDL_Color{255, 255, 255, 255};
            }
        }
    }
    // 检查是否是RGBA十进制格式 (如 "29,15,66,74" 或 "29,15,66")
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
            cerr << "颜色值 '" << colorStr << "' 解析失败: " << e.what() << endl;
            return SDL_Color{255, 255, 255, 255};
        }
    }

    cerr << "无法识别的颜色格式: " << colorStr << " (支持格式: 0xRRGGBBAA, RRGGBBAA, R,G,B,A 或 R,G,B)" << endl;
    return SDL_Color{255, 255, 255, 255};
}
