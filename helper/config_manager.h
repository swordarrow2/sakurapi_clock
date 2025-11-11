//
// Created by SJF on 2025/9/17.
//
#ifndef SAKURAPI_CLOCK_CONFIG_MANAGER_H
#define SAKURAPI_CLOCK_CONFIG_MANAGER_H

#include <string>
#include <map>
#include <vector>
#include "tar_filesystem.h"

using std::string;
using std::vector;
using std::map;

class ConfigManager {
public:
    static ConfigManager &getInstance();

    bool loadConfigFromTarDirectory(const string &directory);

    string getString(const string &key, const string &defaultValue = "") const;

    int32_t getInt(const string &key, int defaultValue = 0) const;

    float getFloat(const string &key, float defaultValue = 0.0f) const;

    SDL_Color getColor(const string &key) const;

    void setConfigIndex(uint32_t index);

    MemoryTarFileSystem *getCurrentTarFileSystem();

    size_t getConfigCount() const;

private:
    ConfigManager() = default;

    ConfigManager(const ConfigManager &) = delete;

    ConfigManager &operator=(const ConfigManager &) = delete;

    bool parseConfigFromString(const string &configContent, map<string, string> &configMap);

    SDL_Color parseColorString(const string &colorStr) const;

    vector<map<string, string> > configDatas;
    vector<std::unique_ptr<MemoryTarFileSystem> > tarFileSystems;
    int currentIndex = 0;
};

#endif //SAKURAPI_CLOCK_CONFIG_MANAGER_H
