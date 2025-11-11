//
// Created by sjf on 2025/10/1.
//

#ifndef CLOCK_TAR_PACKER_H
#define CLOCK_TAR_PACKER_H


#include <string>
#include <vector>

class TarPacker {
public:
    static bool packDirectory(const std::string &sourceDir, const std::string &outputTarFile);

    static bool unpackDirectory(const std::string &tarFile, const std::string &targetDir);

private:
    static bool executeTarCommand(const std::vector<std::string> &args);
};


#endif //CLOCK_TAR_PACKER_H
