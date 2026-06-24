#include "tar_filesystem.h"
#include <fstream>
#include <cstring>
#include <sstream>
#include <SDL2/SDL_image.h>

// Read config.ini content directly from tar file (scan headers, without loading entire tar into memory)
std::string MemoryTarFileSystem::readConfigFromTar(const std::string &tarPath) {
    std::ifstream file(tarPath, std::ios::binary);
    if (!file) {
        std::cerr << "Cannot open tar file: " << tarPath << std::endl;
        return "";
    }
    TarHeader header;
    while (file.read(reinterpret_cast<char *>(&header), sizeof(TarHeader))) {
        if (header.name[0] == '\0') break;  // End marker
        long file_size = octal_to_long(header.size, sizeof(header.size));
    // Find config.ini
        if ((header.typeflag == '0' || header.typeflag == '\0') &&
            std::string(header.name) == "config.ini") {
            if (file_size > 0) {
                std::string content(static_cast<size_t>(file_size), '\0');
                file.read(&content[0], file_size);
                return content;
            }
            return "";  // Empty file
        }
        // Skip current file data (with 512-byte alignment)
        if (file_size > 0) {
            long padding = (512 - (file_size % 512)) % 512;
            file.seekg(file_size + padding, std::ios::cur);
        }
    }
    return "";  // config.ini not found
}

// Convert octal string to long
long MemoryTarFileSystem::octal_to_long(const char *str, size_t len) {
    long value = 0;
    for (size_t i = 0; i < len && str[i] != '\0'; i++) {
        if (str[i] >= '0' && str[i] <= '7') {
            value = value * 8 + (str[i] - '0');
        }
    }
    return value;
}

// Load tar file into memory
bool MemoryTarFileSystem::loadTar(const std::string &filename) {
    std::ifstream file(filename, std::ios::binary);
    if (!file) {
        std::cerr << "Cannot open tar file: " << filename << std::endl;
        return false;
    }

    while (file) {
        TarHeader header;
        file.read(reinterpret_cast<char *>(&header), sizeof(TarHeader));

        if (file.gcount() == 0) break;
        if (header.name[0] == '\0') break; // End marker

        // Only process regular files and empty files
        if (header.typeflag == '0' || header.typeflag == '\0') {
            long file_size = octal_to_long(header.size, sizeof(header.size));
            std::string filename_str(header.name);

            FileInfo file_info;
            file_info.size = file_size;
            file_info.mode = octal_to_long(header.mode, sizeof(header.mode));

            if (file_size > 0) {
                file_info.data.resize(file_size);
                file.read(file_info.data.data(), file_size);

                // Skip padding bytes (tar files are 512-byte aligned)
                long padding = (512 - (file_size % 512)) % 512;
                if (padding > 0) {
                    file.seekg(padding, std::ios::cur);
                }
            }

            files[filename_str] = std::move(file_info);
            std::cout << "load file: " << filename_str << " (" << file_size << " Bytes)" << std::endl;
        } else {
    // Private: for non-regular files, skip data
            long file_size = octal_to_long(header.size, sizeof(header.size));
            if (file_size > 0) {
                long padding = (512 - (file_size % 512)) % 512;
                file.seekg(file_size + padding, std::ios::cur);
            }
        }
    }

    std::cout << "load " << files.size() << " files into memory" << std::endl;
    return true;
}

// Get file list
std::vector<std::string> MemoryTarFileSystem::listFiles() const {
    std::vector<std::string> result;
    for (const auto &pair: files) {
        result.push_back(pair.first);
    }
    return result;
}

// Check if file exists
bool MemoryTarFileSystem::fileExists(const std::string &filename) const {
    return files.find(filename) != files.end();
}

// Get file content (read-only pointer)
const char *MemoryTarFileSystem::getFileData(const std::string &filename) const {
    auto it = files.find(filename);
    if (it != files.end() && !it->second.data.empty()) {
        return it->second.data.data();
    }
    return nullptr;
}

// Get file size
size_t MemoryTarFileSystem::getFileSize(const std::string &filename) const {
    auto it = files.find(filename);
    if (it != files.end()) {
        return it->second.size;
    }
    return 0;
}

// Get file content as string
std::string MemoryTarFileSystem::getFileAsString(const std::string &filename) const {
    auto it = files.find(filename);
    if (it != files.end() && !it->second.data.empty()) {
        return std::string(it->second.data.data(), it->second.size);
    }
    return "";
}

// Create in-memory istream (for use by other libraries)
std::unique_ptr<std::istream> MemoryTarFileSystem::createFileStream(const std::string &filename) const {
    auto it = files.find(filename);
    if (it != files.end() && !it->second.data.empty()) {
        auto stream = std::make_unique<std::stringstream>(
            std::string(it->second.data.data(), it->second.size)
        );
        return stream;
    }
    return nullptr;
}

// Save file to actual filesystem (for debugging)
bool MemoryTarFileSystem::saveToDisk(const std::string &filename, const std::string &output_path) const {
    auto it = files.find(filename);
    if (it == files.end()) {
        return false;
    }

    std::ofstream out_file(output_path, std::ios::binary);
    if (!out_file) {
        return false;
    }

    if (!it->second.data.empty()) {
        out_file.write(it->second.data.data(), it->second.size);
    }

    return true;
}

SDL_RWops *MemoryTarFileSystem::createRWOps(const std::string &filename) const {
    auto it = files.find(filename);
    if (it == files.end() || it->second.data.empty()) {
        return nullptr;
    }

        // Create RWops from memory data
    SDL_RWops *rw = SDL_RWFromConstMem(it->second.data.data(), it->second.size);
    return rw;
}

// Load texture from memory using SDL_image
SDL_Texture *MemoryTarFileSystem::loadTexture(SDL_Renderer *renderer, const std::string &filename) {
    SDL_RWops *rw = createRWOps(filename);
    if (!rw) {
        std::cerr << "Cannot create RWops for file: " << filename << std::endl;
        return nullptr;
    }

    // Use SDL_image to load image
    SDL_Surface *surface = IMG_Load_RW(rw, 1); // 1 means auto-close RWops
    if (!surface) {
        std::cerr << "Cannot load image: " << filename << " - " << IMG_GetError() << std::endl;
        return nullptr;
    }

    SDL_Texture *texture = SDL_CreateTextureFromSurface(renderer, surface);
    SDL_FreeSurface(surface);

    if (!texture) {
        std::cerr << "Cannot create texture: " << filename << " - " << SDL_GetError() << std::endl;
    }

    return texture;
}

// Load BMP image (without using SDL_image)
SDL_Surface *MemoryTarFileSystem::loadBMP(const std::string &filename) {
    SDL_RWops *rw = createRWOps(filename);
    if (!rw) {
        return nullptr;
    }

    SDL_Surface *surface = SDL_LoadBMP_RW(rw, 1); // 1 means auto-close RWops
    return surface;
}

// Load font from memory
TTF_Font *MemoryTarFileSystem::loadFont(const std::string &filename, int ptsize) {
    SDL_RWops *rw = createRWOps(filename);
    if (!rw) {
        std::cerr << "Cannot create RWops for font file: " << filename << std::endl;
        return nullptr;
    }

    // Use SDL_ttf to load font from RWops
    TTF_Font *font = TTF_OpenFontRW(rw, 1, ptsize); // 1 means auto-close RWops
    if (!font) {
        std::cerr << "Cannot load font: " << filename << " - " << TTF_GetError() << std::endl;
    }

    return font;
}

// Load font from memory (with style parameters)
TTF_Font *MemoryTarFileSystem::loadFont(const std::string &filename, int ptsize, long style) {
    TTF_Font *font = loadFont(filename, ptsize);
    if (font) {
        TTF_SetFontStyle(font, style);
    }
    return font;
}
