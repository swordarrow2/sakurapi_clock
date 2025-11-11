#include "clock_app.h"
#include "helper/config_manager.h"
#include "sakurapi_clock.h"
#include "helper/tar_packer.h"
#include "helper/globals.h"
#include <iostream>
#include <string>


using std::endl;
using std::cerr;
using std::cout;

void printUsage() {
    cout << "Usage:" << endl;
    cout << "  sakurapi_clock [theme_directory]           - Run clock with specified theme directory" << endl;
    cout << "  sakurapi_clock --pack [theme_name]         - Pack specified theme to TAR format" << endl;
    cout << "  sakurapi_clock --unpack [tar_file]         - Unpack TAR file to themes directory" << endl;
    cout << "  sakurapi_clock --help                      - Show this help message" << endl;
    cout << endl;
    cout << "Examples:" << endl;
    cout << "  ./sakurapi_clock --pack theme_sanae" << endl;
    cout << "  ./sakurapi_clock --unpack theme_sanae.tar" << endl;
}

int main(int argc, char *argv[]) {
    // #ifdef ARMBIAN
    // std::string themeDirectory = "/sakurapi_clock_themes";
    // #elif defined(UBUNTU)
    std::string themeDirectory = "themes";
    // #endif

    // 处理帮助命令
    if (argc == 2 && std::string(argv[1]) == "--help") {
        printUsage();
        return 0;
    }
    if (argc > 2) {
        std::string command = argv[1];
        if (command == "--pack") {
            std::string themeName = argv[2];
            std::string sourceDir = "../" + themeDirectory + "/" + themeName;
            std::string outputFile = "themes/" + themeName + ".tar"; // 使用 .tar 扩展名
            if (!TarPacker::packDirectory(sourceDir, outputFile)) {
                cerr << "Failed to pack theme '" << themeName << "'" << endl;
                return -1;
            }
            cout << "Successfully packed theme '" << themeName << "' to " << outputFile << endl;
            cout << "TAR structure:" << endl;
            std::string listCommand = "tar -tf " + outputFile;
            system(listCommand.c_str());
            return 0;
        }
        if (command == "--unpack") {
            std::string tarFile = "themes/";
            std::string themeName = argv[2];
            tarFile += argv[2];
            size_t dotPos = themeName.find_last_of('.');
            if (dotPos != std::string::npos && themeName.substr(dotPos) == ".tar") {
                themeName = themeName.substr(0, dotPos);
            }
            std::string targetDir = "../" + themeDirectory + "/" + themeName;
            if (TarPacker::unpackDirectory(tarFile, targetDir)) {
                cout << "Successfully unpacked " << tarFile << " to " << targetDir << endl;
                return 0;
            }
            cerr << "Failed to unpack " << tarFile << endl;
            return -1;
        }
    }

    if (argc > 1) {
        std::string firstArg = argv[1];
        if (firstArg == "--pack" || firstArg == "--unpack" || firstArg == "--help") {
            printUsage();
            return 0;
        }
        Globals::themeName = argv[1];
        cout << "target theme" << Globals::themeName << endl;
        std::string sourceDir = "../" + themeDirectory + "/" + Globals::themeName;
        std::string outputFile = "themes/" + Globals::themeName + ".tar"; // 使用 .tar 扩展名
        if (!TarPacker::packDirectory(sourceDir, outputFile)) {
            cerr << "Failed to pack theme '" << Globals::themeName << "'" << endl;
            return -1;
        }
        cout << "Successfully packed theme '" << Globals::themeName << "' to " << outputFile << endl;
        cout << "TAR structure:" << endl;
        std::string listCommand = "tar -tf " + outputFile;
        system(listCommand.c_str());
    }

    ConfigManager &config = ConfigManager::getInstance();
    if (!config.loadConfigFromTarDirectory(themeDirectory)) {
        return -1;
    }

    ClockApp app;
    if (!app.initialize()) {
        cerr << "Failed to initialize application!" << endl;
        return 1;
    }
    app.run();
    return 0;
}
