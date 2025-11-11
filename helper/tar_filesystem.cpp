#include "tar_filesystem.h"
#include <fstream>
#include <cstring>
#include <sstream>
#include <SDL2/SDL_image.h>

// 将八进制字符串转换为整数
long MemoryTarFileSystem::octal_to_long(const char *str, size_t len) {
    long value = 0;
    for (size_t i = 0; i < len && str[i] != '\0'; i++) {
        if (str[i] >= '0' && str[i] <= '7') {
            value = value * 8 + (str[i] - '0');
        }
    }
    return value;
}

// 加载tar文件到内存
bool MemoryTarFileSystem::loadTar(const std::string &filename) {
    std::ifstream file(filename, std::ios::binary);
    if (!file) {
        std::cerr << "无法打开tar文件: " << filename << std::endl;
        return false;
    }

    while (file) {
        TarHeader header;
        file.read(reinterpret_cast<char *>(&header), sizeof(TarHeader));

        if (file.gcount() == 0) break;
        if (header.name[0] == '\0') break; // 结束标记

        // 只处理普通文件和空文件
        if (header.typeflag == '0' || header.typeflag == '\0') {
            long file_size = octal_to_long(header.size, sizeof(header.size));
            std::string filename_str(header.name);

            FileInfo file_info;
            file_info.size = file_size;
            file_info.mode = octal_to_long(header.mode, sizeof(header.mode));

            if (file_size > 0) {
                file_info.data.resize(file_size);
                file.read(file_info.data.data(), file_size);

                // 跳过填充字节（tar文件按512字节对齐）
                long padding = (512 - (file_size % 512)) % 512;
                if (padding > 0) {
                    file.seekg(padding, std::ios::cur);
                }
            }

            files[filename_str] = std::move(file_info);
            std::cout << "load file: " << filename_str << " (" << file_size << " Bytes)" << std::endl;
        } else {
            // 对于非普通文件，跳过数据区
            long file_size = octal_to_long(header.size, sizeof(header.size));
            if (file_size > 0) {
                long padding = (512 - (file_size % 512)) % 512;
                file.seekg(file_size + padding, std::ios::cur);
            }
        }
    }

    std::cout << "load " << files.size() << " files into RAM" << std::endl;
    return true;
}

// 获取文件列表
std::vector<std::string> MemoryTarFileSystem::listFiles() const {
    std::vector<std::string> result;
    for (const auto &pair: files) {
        result.push_back(pair.first);
    }
    return result;
}

// 检查文件是否存在
bool MemoryTarFileSystem::fileExists(const std::string &filename) const {
    return files.find(filename) != files.end();
}

// 获取文件内容（只读指针）
const char *MemoryTarFileSystem::getFileData(const std::string &filename) const {
    auto it = files.find(filename);
    if (it != files.end() && !it->second.data.empty()) {
        return it->second.data.data();
    }
    return nullptr;
}

// 获取文件大小
size_t MemoryTarFileSystem::getFileSize(const std::string &filename) const {
    auto it = files.find(filename);
    if (it != files.end()) {
        return it->second.size;
    }
    return 0;
}

// 获取文件内容的字符串形式
std::string MemoryTarFileSystem::getFileAsString(const std::string &filename) const {
    auto it = files.find(filename);
    if (it != files.end() && !it->second.data.empty()) {
        return std::string(it->second.data.data(), it->second.size);
    }
    return "";
}

// 创建内存中的istream（用于其他库读取）
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

// 将文件保存到实际文件系统（调试用）
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

    // 创建RWops从内存数据
    SDL_RWops *rw = SDL_RWFromConstMem(it->second.data.data(), it->second.size);
    return rw;
}

// 使用SDL_image从内存加载纹理
SDL_Texture *MemoryTarFileSystem::loadTexture(SDL_Renderer *renderer, const std::string &filename) {
    SDL_RWops *rw = createRWOps(filename);
    if (!rw) {
        std::cerr << "无法为文件创建RWops: " << filename << std::endl;
        return nullptr;
    }

    // 使用SDL_image加载图像
    SDL_Surface *surface = IMG_Load_RW(rw, 1); // 1表示自动关闭RWops
    if (!surface) {
        std::cerr << "无法加载图像: " << filename << " - " << IMG_GetError() << std::endl;
        return nullptr;
    }

    SDL_Texture *texture = SDL_CreateTextureFromSurface(renderer, surface);
    SDL_FreeSurface(surface);

    if (!texture) {
        std::cerr << "无法创建纹理: " << filename << " - " << SDL_GetError() << std::endl;
    }

    return texture;
}

// 加载BMP图像（不使用SDL_image）
SDL_Surface *MemoryTarFileSystem::loadBMP(const std::string &filename) {
    SDL_RWops *rw = createRWOps(filename);
    if (!rw) {
        return nullptr;
    }

    SDL_Surface *surface = SDL_LoadBMP_RW(rw, 1); // 1表示自动关闭RWops
    return surface;
}

// 从内存加载字体
TTF_Font *MemoryTarFileSystem::loadFont(const std::string &filename, int ptsize) {
    SDL_RWops *rw = createRWOps(filename);
    if (!rw) {
        std::cerr << "无法为字体文件创建RWops: " << filename << std::endl;
        return nullptr;
    }

    // 使用SDL_ttf从RWops加载字体
    TTF_Font *font = TTF_OpenFontRW(rw, 1, ptsize); // 1表示自动关闭RWops
    if (!font) {
        std::cerr << "无法加载字体: " << filename << " - " << TTF_GetError() << std::endl;
    }

    return font;
}

// 从内存加载字体（带样式参数）
TTF_Font *MemoryTarFileSystem::loadFont(const std::string &filename, int ptsize, long style) {
    TTF_Font *font = loadFont(filename, ptsize);
    if (font) {
        TTF_SetFontStyle(font, style);
    }
    return font;
}
