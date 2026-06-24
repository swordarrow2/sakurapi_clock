#ifndef SAKURAPI_CLOCK_TEXTURE_LOADER_H
#define SAKURAPI_CLOCK_TEXTURE_LOADER_H

#include <map>
#include <SDL2/SDL.h>
#include <string>
#include "../helper/tar_filesystem.h"

class TextureManager {
public:
    static TextureManager &getInstance();

    bool init(SDL_Renderer *renderer);

    SDL_Texture *loadTexture(std::string path, SDL_Renderer *renderer);

    const std::vector<std::string> &getBackgroundFrames() const { return backgroundFrames; }

    SDL_Texture *getFrameTexture(uint32_t frameIndex) const;

    size_t getFrameCount() const { return backgroundFrames.size(); }

private:
    std::map<std::string, SDL_Texture *> texturesMap;

    std::vector<std::string> backgroundFrames;

    void processImageFolder(const std::string &folderPath, SDL_Renderer *renderer);

    TextureManager() = default;

    TextureManager(const TextureManager &) = delete;

    TextureManager &operator=(const TextureManager &) = delete;
};

#endif // TEXTURE_LOADER_H
