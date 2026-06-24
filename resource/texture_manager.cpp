#include "texture_manager.h"

#include <algorithm>
#include <SDL2/SDL_image.h>
#include <iostream>
#include <stdexcept>
#include <string>
#include <dirent.h>
#include "../helper/config_manager.h"

using std::cout;
using std::endl;
using std::cerr;
using std::runtime_error;

TextureManager &TextureManager::getInstance() {
    static TextureManager instance;
    return instance;
}

bool TextureManager::init(SDL_Renderer *renderer) {
    try {
        for (auto &pair: texturesMap) {
            if (pair.second) {
                SDL_DestroyTexture(pair.second);
            }
        }
        texturesMap.clear();
        backgroundFrames.clear(); // Clear frame list
        std::string bgDir = ConfigManager::getInstance().getString("background.dir");
        if (!bgDir.empty()) {
            processImageFolder(bgDir, renderer);
        } else {
            std::string bgFile = ConfigManager::getInstance().getString("background.file");
            if (!bgFile.empty()) {
                texturesMap["bg_0"] = loadTexture(bgFile, renderer);
                backgroundFrames.push_back("bg_0");
            }
        }
    } catch (const std::exception &e) {
        cerr << e.what() << endl;
        return false;
    }
    return true;
}

void TextureManager::processImageFolder(const std::string &folderPath, SDL_Renderer *renderer) {
    std::vector<std::string> imageExtensions = {".png", ".jpg", ".jpeg", ".bmp"};
    MemoryTarFileSystem *tarFS = ConfigManager::getInstance().getCurrentTarFileSystem();
    if (!tarFS) {
        std::cerr << "No tar file system available for folder: " << folderPath << std::endl;
        return;
    }
    std::vector<std::string> allFiles = tarFS->listFiles();
    // Temporary storage for file info, used for sorting
    struct FileInfo {
        std::string path;
        std::string name;
        std::string key;
    };
    std::vector<FileInfo> imageFiles;

    for (const std::string &filePath: allFiles) {
        if (filePath.find(folderPath + "/") != 0) {
            continue; // File not in target folder
        }
        // Extract filename (without path)
        size_t lastSlash = filePath.find_last_of('/');
        std::string fileName = (lastSlash != std::string::npos) ? filePath.substr(lastSlash + 1) : filePath;
        // Skip "." and ".."
        if (fileName == "." || fileName == "..") {
            continue;
        }
        // Extract file extension
        size_t dotPos = fileName.find_last_of('.');
        if (dotPos == std::string::npos) {
            continue; // No extension
        }
        std::string extension = fileName.substr(dotPos);
        std::string lowerExtension = extension;
        std::transform(lowerExtension.begin(), lowerExtension.end(), lowerExtension.begin(), ::tolower);
        // Check if it's an image file
        if (std::find(imageExtensions.begin(), imageExtensions.end(), lowerExtension) != imageExtensions.end()) {
            // Generate texture key: bg_filename
            std::string textureKey = "bg_" + fileName;
            // Store file info for sorting
            imageFiles.push_back({filePath, fileName, textureKey});
        }
    }
    // Sort by filename (to ensure correct animation frame order)
    std::sort(imageFiles.begin(), imageFiles.end(),
              [](const FileInfo &a, const FileInfo &b) {
                  return a.name < b.name;
              });
    // Load textures in sorted order
    for (const auto &fileInfo: imageFiles) {
        // Load texture
        SDL_Texture *texture = loadTexture(fileInfo.path, renderer);
        if (texture) {
            texturesMap[fileInfo.key] = texture;
            backgroundFrames.push_back(fileInfo.key);
            std::cout << "Loaded texture from tar: " << fileInfo.key << " from " << fileInfo.path << std::endl;
        } else {
            std::cerr << "Failed to load texture from tar: " << fileInfo.path << std::endl;
        }
    }
    std::cout << "Loaded " << backgroundFrames.size() << " background frames" << std::endl;
}

SDL_Texture *TextureManager::getFrameTexture(uint32_t frameIndex) const {
    if (frameIndex >= static_cast<uint32_t>(backgroundFrames.size())) {
        return nullptr;
    }

    const std::string &textureKey = backgroundFrames[frameIndex];
    auto it = texturesMap.find(textureKey);
    if (it != texturesMap.end()) {
        return it->second;
    }
    return nullptr;
}

SDL_Texture *TextureManager::loadTexture(std::string path, SDL_Renderer *renderer) {
    if (!renderer) {
        cout << "none renderer" << endl;
        return nullptr;
    }
    MemoryTarFileSystem *tarFS = ConfigManager::getInstance().getCurrentTarFileSystem();
    SDL_Texture *background = nullptr;
    cout << "load SDL_Texture:" << path << endl;
    background = tarFS->loadTexture(renderer, path);
    if (!background) {
        throw runtime_error("texture failed: " + std::string(SDL_GetError()));
    }
    return background;
}
