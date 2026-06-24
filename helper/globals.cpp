//
// Created by sjf on 2025/10/2.
//

#include "globals.h"

namespace Globals {
    std::string themeName;
    const std::string key_time = "time";
    const std::string key_date = "date";
    const std::string key_fps = "fps";
    const std::string key_cpu_usage = "cpu_usage";
    const std::string key_cpu_temp = "cpu_temp";
    const std::string key_memory_state = "memory_state";
    const std::string key_storage_state = "storage_state";
    const std::string key_ip_state = "ip_state";
    const std::string elements[] = {
        key_time, key_date, key_fps, key_cpu_usage, key_cpu_temp,
        key_memory_state, key_storage_state, key_ip_state
    };
}
