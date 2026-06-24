#include "clock_app.h"
#include "helper/config_manager.h"
#include "sakurapi_clock.h"
#include "helper/tar_packer.h"
#include "helper/globals.h"
#include <iostream>
#include <string>
#include <fstream>
#include <unistd.h>
#include <limits.h>
#include <cstdlib>
#include <dirent.h>
#include <vector>


using std::endl;
using std::cerr;
using std::cout;

// Get absolute path of current executable
std::string getExecutablePath() {
    char result[PATH_MAX];
    ssize_t count = readlink("/proc/self/exe", result, PATH_MAX);
    if (count != -1) {
        result[count] = '\0';
        return std::string(result);
    }
    return "";
}

// Get current working directory
std::string getWorkingDirectory() {
    char cwd[PATH_MAX];
    if (getcwd(cwd, sizeof(cwd)) != nullptr) {
        return std::string(cwd);
    }
    return "";
}

// Install systemd auto-start service
bool installAutoRunService() {
    // Check if running with root privileges
    if (getuid() != 0) {
        cerr << "Error: --autorun requires root privileges. Please run with sudo." << endl;
        return false;
    }

    std::string execPath = getExecutablePath();
    if (execPath.empty()) {
        cerr << "Error: Failed to get executable path." << endl;
        return false;
    }

    std::string workDir = getWorkingDirectory();
    if (workDir.empty()) {
        cerr << "Error: Failed to get working directory." << endl;
        return false;
    }

    // systemd service file content
    std::string serviceContent = "[Unit]\n"
        "Description=Clock Display Service\n"
        "After=systemd-udev-settle.service\n"
        "Before=display-manager.service\n"
        "Requires=local-fs.target\n"
        "ConditionPathExists=/dev/fb0\n"
        "\n"
        "[Service]\n"
        "Type=simple\n"
        "User=root\n"
        "WorkingDirectory=" + workDir + "\n"
        "ExecStart=" + execPath + "\n"
        "Restart=always\n"
        "RestartSec=5\n"
        "StandardOutput=null\n"
        "StandardError=journal\n"
        "Environment=DISPLAY=:0\n"
        "\n"
        "[Install]\n"
        "WantedBy=multi-user.target\n";

    // Write service file
    std::string servicePath = "/etc/systemd/system/sakurapi_clock.service";
    std::ofstream serviceFile(servicePath);
    if (!serviceFile.is_open()) {
        cerr << "Error: Failed to create service file: " << servicePath << endl;
        return false;
    }
    serviceFile << serviceContent;
    serviceFile.close();

    cout << "Service file created: " << servicePath << endl;
    cout << "Executable path: " << execPath << endl;
    cout << "Working directory: " << workDir << endl;

    // Reload systemd configuration
    cout << "Reloading systemd daemon..." << endl;
    int ret = system("systemctl daemon-reload");
    if (ret != 0) {
        cerr << "Warning: systemctl daemon-reload returned non-zero." << endl;
    }

    // Enable service
    cout << "Enabling sakurapi_clock service..." << endl;
    ret = system("systemctl enable sakurapi_clock.service");
    if (ret != 0) {
        cerr << "Error: Failed to enable service." << endl;
        return false;
    }

    cout << "\nService installed successfully!" << endl;
    cout << "The clock will start automatically on next boot." << endl;
    cout << "\nUseful commands:" << endl;
    cout << "  sudo systemctl start sakurapi_clock   - Start service now" << endl;
    cout << "  sudo systemctl stop sakurapi_clock    - Stop service" << endl;
    cout << "  sudo systemctl status sakurapi_clock  - Check service status" << endl;
    cout << "  sudo systemctl disable sakurapi_clock - Disable auto-start" << endl;

    return true;
}

// Pack all themes
int packAllThemes(const std::string &themeDirectory) {
    std::string themesPath = "../" + themeDirectory;
    DIR *dir = opendir(themesPath.c_str());
    if (!dir) {
        cerr << "Error: Cannot open themes directory: " << themesPath << endl;
        return -1;
    }

    std::vector<std::string> themeDirs;
    struct dirent *entry;
    while ((entry = readdir(dir)) != nullptr) {
        if (entry->d_type == DT_DIR) {
            std::string name = entry->d_name;
            if (name != "." && name != "..") {
                themeDirs.push_back(name);
            }
        }
    }
    closedir(dir);

    if (themeDirs.empty()) {
        cout << "No themes found in " << themesPath << endl;
        return 0;
    }

    int successCount = 0;
    int failCount = 0;
    for (const auto &themeName : themeDirs) {
        std::string sourceDir = themesPath + "/" + themeName;
        std::string outputFile = "themes/" + themeName + ".tar";
        cout << "Packing '" << themeName << "'..." << endl;
        if (TarPacker::packDirectory(sourceDir, outputFile)) {
            cout << "  OK: " << outputFile << endl;
            successCount++;
        } else {
            cerr << "  FAILED: " << themeName << endl;
            failCount++;
        }
    }

    cout << "\nDone. " << successCount << " succeeded";
    if (failCount > 0) {
        cout << ", " << failCount << " failed";
    }
    cout << "." << endl;
    return failCount > 0 ? -1 : 0;
}

void printUsage() {
    cout << "Usage:" << endl;
    cout << "  sakurapi_clock [theme_directory]           - Run clock with specified theme directory" << endl;
    cout << "  sakurapi_clock --pack [theme_name]         - Pack specified theme to TAR format" << endl;
    cout << "  sakurapi_clock --packall                   - Pack all themes to TAR format" << endl;
    cout << "  sakurapi_clock --unpack [tar_file]         - Unpack TAR file to themes directory" << endl;
    cout << "  sakurapi_clock --autorun                   - Install systemd service for auto-start on boot" << endl;
    cout << "  sakurapi_clock --help                      - Show this help message" << endl;
    cout << endl;
    cout << "Examples:" << endl;
    cout << "  ./sakurapi_clock --pack theme_sanae" << endl;
    cout << "  ./sakurapi_clock --packall" << endl;
    cout << "  ./sakurapi_clock --unpack theme_sanae.tar" << endl;
    cout << "  sudo ./sakurapi_clock --autorun            - Install auto-start service (requires root)" << endl;
}

int main(int argc, char *argv[]) {
    std::string themeDirectory = "themes";
    std::string arg = (argc > 1) ? argv[1] : "";

    if (arg == "--help") {
        printUsage();
        return 0;
    }
    if (arg == "--autorun") {
        return installAutoRunService() ? 0 : 1;
    }
    if (arg == "--pack" && argc > 2) {
        std::string themeName = argv[2];
        std::string sourceDir = "../" + themeDirectory + "/" + themeName;
        std::string outputFile = "themes/" + themeName + ".tar";
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
    if (arg == "--packall") {
        return packAllThemes(themeDirectory);
    }
    if (arg == "--unpack" && argc > 2) {
        std::string themeName = argv[2];
        std::string tarFile = "themes/" + themeName;
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
    if (arg.compare(0, 2, "--") == 0) {
        printUsage();
        return 0;
    }
    if (argc > 1) {
        Globals::themeName = arg;
        cout << "target theme" << Globals::themeName << endl;
        std::string sourceDir = "../" + themeDirectory + "/" + Globals::themeName;
        std::string outputFile = "themes/" + Globals::themeName + ".tar";
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
