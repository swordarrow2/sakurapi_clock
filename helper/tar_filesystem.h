#ifndef SAKURAPI_CLOCK_TAR_FILESYSTEM_H
#define SAKURAPI_CLOCK_TAR_FILESYSTEM_H

#include <vector>
#include <string>
#include <unordered_map>
#include <iostream>
#include <memory>
#include <SDL2/SDL_ttf.h>

class MemoryTarFileSystem {
private:
    struct FileInfo {
        std::vector<char> data;
        size_t size;
        mode_t mode;
    };

    std::unordered_map<std::string, FileInfo> files;

    // Convert octal string to long
    static long octal_to_long(const char *str, size_t len);

public:
    // Tar header structure
    struct TarHeader {
        char name[100];
        char mode[8];
        char uid[8];
        char gid[8];
        char size[12];
        char mtime[12];
        char chksum[8];
        char typeflag;
        char linkname[100];
        char magic[6];
        char version[2];
        char uname[32];
        char gname[32];
        char devmajor[8];
        char devminor[8];
        char prefix[155];
        char padding[12];
    };

    // Load tar file into memory
    bool loadTar(const std::string &filename);

    // Read config.ini content directly from tar file (without keeping tar in memory)
    static std::string readConfigFromTar(const std::string &tarPath);

    // Get file list
    std::vector<std::string> listFiles() const;

    // Check if file exists
    bool fileExists(const std::string &filename) const;

    // Get file content (read-only pointer)
    const char *getFileData(const std::string &filename) const;

    // Get file size
    size_t getFileSize(const std::string &filename) const;

    // Get file content as string
    std::string getFileAsString(const std::string &filename) const;

    // Create in-memory istream (for use by other libraries)
    std::unique_ptr<std::istream> createFileStream(const std::string &filename) const;

    // Save file to actual filesystem (for debugging)
    bool saveToDisk(const std::string &filename, const std::string &output_path) const;

    SDL_RWops *createRWOps(const std::string &filename) const;

    // Load texture from memory using SDL_image
    SDL_Texture *loadTexture(SDL_Renderer *renderer, const std::string &filename);

    // Load BMP image (without using SDL_image)
    SDL_Surface *loadBMP(const std::string &filename);

    // Load font from memory
    TTF_Font *loadFont(const std::string &filename, int ptsize);

    // Load font from memory (with style parameters)
    TTF_Font *loadFont(const std::string &filename, int ptsize, long style);
};

#endif
