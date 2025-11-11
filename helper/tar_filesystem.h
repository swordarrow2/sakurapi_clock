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

    // 将八进制字符串转换为整数
    long octal_to_long(const char *str, size_t len);

public:
    // Tar文件头结构
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

    // 加载tar文件到内存
    bool loadTar(const std::string &filename);

    // 获取文件列表
    std::vector<std::string> listFiles() const;

    // 检查文件是否存在
    bool fileExists(const std::string &filename) const;

    // 获取文件内容（只读指针）
    const char *getFileData(const std::string &filename) const;

    // 获取文件大小
    size_t getFileSize(const std::string &filename) const;

    // 获取文件内容的字符串形式
    std::string getFileAsString(const std::string &filename) const;

    // 创建内存中的istream（用于其他库读取）
    std::unique_ptr<std::istream> createFileStream(const std::string &filename) const;

    // 将文件保存到实际文件系统（调试用）
    bool saveToDisk(const std::string &filename, const std::string &output_path) const;

    SDL_RWops *createRWOps(const std::string &filename) const;

    // 使用SDL_image从内存加载纹理
    SDL_Texture *loadTexture(SDL_Renderer *renderer, const std::string &filename);

    // 加载BMP图像（不使用SDL_image）
    SDL_Surface *loadBMP(const std::string &filename);

    // 从内存加载字体
    TTF_Font *loadFont(const std::string &filename, int ptsize);

    // 从内存加载字体（带样式参数）
    TTF_Font *loadFont(const std::string &filename, int ptsize, long style);
};

#endif
