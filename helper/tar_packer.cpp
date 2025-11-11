#include "tar_packer.h"
#include <iostream>
#include <sys/stat.h>
#include <vector>
#include <dirent.h>

bool TarPacker::executeTarCommand(const std::vector<std::string> &args) {
    std::string command = "tar";

    for (const auto &arg: args) {
        command += " " + arg;
    }

    std::cout << "Executing: " << command << std::endl;
    int result = system(command.c_str());

    return result == 0;
}

bool TarPacker::packDirectory(const std::string &sourceDir, const std::string &outputTarFile) {
    // 检查源目录是否存在
    struct stat info;
    if (stat(sourceDir.c_str(), &info) != 0 || !(info.st_mode & S_IFDIR)) {
        std::cerr << "Source directory does not exist: " << sourceDir << std::endl;
        return false;
    }
    // 获取目录中的所有文件
    std::vector<std::string> files;
    DIR *dir;
    struct dirent *ent;
    if ((dir = opendir(sourceDir.c_str())) != NULL) {
        while ((ent = readdir(dir)) != NULL) {
            std::string filename = ent->d_name;
            // 跳过当前目录和父目录
            if (filename != "." && filename != "..") {
                files.push_back(filename);
            }
        }
        closedir(dir);
    } else {
        std::cerr << "Could not open directory: " << sourceDir << std::endl;
        return false;
    }
    if (files.empty()) {
        std::cerr << "No files found in directory: " << sourceDir << std::endl;
        return false;
    }
    std::vector<std::string> args = {"-cf", outputTarFile, "-C", sourceDir};
    for (const auto &file: files) {
        args.push_back(file);
    }
    return executeTarCommand(args);
}

bool TarPacker::unpackDirectory(const std::string &tarFile, const std::string &targetDir) {
    // 创建目标目录（如果不存在）
    struct stat info;
    if (stat(targetDir.c_str(), &info) != 0) {
        std::string createDir = "mkdir -p " + targetDir;
        system(createDir.c_str());
    }
    std::vector<std::string> args = {
        "-xf",
        tarFile,
        "-C",
        targetDir
    };
    return executeTarCommand(args);
}
