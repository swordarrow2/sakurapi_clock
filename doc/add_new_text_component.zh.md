# 添加新文本组件

本文档说明如何在 sakurapi_clock 中添加一个新的文本显示组件。

## 步骤概览

1. 创建组件类（.h 和 .cpp）
2. 注册到系统
3. 添加到构建
4. 配置使用

---

## 1. 创建组件类

在 `widget/texts/` 目录下创建 `xxx_text.h` 和 `xxx_text.cpp`。

### 头文件模板

```cpp
//
// Created by [username] on [date].
//

#ifndef SAKURAPI_CLOCK_XXX_TEXT_H
#define SAKURAPI_CLOCK_XXX_TEXT_H

#include "../draggable_text.h"

class XxxText : public DraggableText {
public:
    // 你的数据结构（可选）
    struct XxxInfo {
        // ...
    };

    XxxInfo xxxInfo;

    void init(const std::string &name, const std::string &text, TTF_Font *textFont,
              SDL_Color textColor, SDL_Color outlineColor, int outlineSize,
              int initialX, int initialY) override;

    void update() override;

    void render(SDL_Renderer *renderer) override;

private:
    // 你的数据获取方法
    XxxInfo getXxxInfo();

    uint32_t updateCount;
};

#endif //SAKURAPI_CLOCK_XXX_TEXT_H
```

### 实现文件模板

```cpp
//
// Created by [username] on [date].
//

#include "xxx_text.h"
#include "../../helper/config_manager.h"

void XxxText::init(const std::string &name, const std::string &text,
                   TTF_Font *textFont, SDL_Color textColor,
                   SDL_Color outlineColor, int outlineSize,
                   int initialX, int initialY) {
    // 1. 调用父类 init
    DraggableText::init(name, text, textFont, textColor,
                        outlineColor, outlineSize, initialX, initialY);
    // 2. 设置更新计数器（每秒更新一次）
    updateCount = ConfigManager::getInstance().getInt("performance.max_fps");
}

void XxxText::update() {
    // 每帧调用，需要控制更新频率
    if (updateCount == ConfigManager::getInstance().getInt("performance.max_fps")) {
        // 获取数据并更新文本
        xxxInfo = getXxxInfo();
        std::stringstream ss;
        ss << "XXX: " << xxxInfo.value;
        setText(ss.str());
        updateCount = 0;
    }
    updateCount++;
}

void XxxText::render(SDL_Renderer *renderer) {
    // 渲染文本
    DraggableText::render(renderer);
    // 在这里可以绘制额外元素（如进度条等）
}
```

### 关键说明

- **`init()`** — 初始化组件，必须调用父类 `DraggableText::init()`
- **`update()`** — 每帧调用，用 `updateCount` 控制实际更新频率（通常每秒一次）
- **`render()`** — 绘制，先调用父类 `render()` 渲染文字
- 数据通过 `setText()` 设置文字内容，系统会自动创建纹理

---

## 2. 注册到系统

需要修改 3 个文件：

### 2.1 `helper/globals.h`

- 声明配置 key 常量
- 增大 `elements` 数组大小

```cpp
// 在 namespace Globals 中添加：
extern const std::string key_xxx_state;

// 修改 elements 数组大小（原 N 改为 N+1）：
extern const std::string elements[7];  // 原来是 6
```

### 2.2 `helper/globals.cpp`

- 定义 key 常量
- 加入 `elements` 数组

```cpp
// 添加常量定义：
const std::string key_xxx_state = "xxx_state";

// 加入 elements 数组：
const std::string elements[] = {
    key_time, key_date, key_fps,
    key_cpu_state, key_memory_state, key_storage_state,
    key_xxx_state  // 新增
};
```

### 2.3 `resource/text_manager.cpp`

- 添加 `#include` 头文件
- 在 `init()` 的 if-else 链中创建实例

```cpp
// 1. 添加 include：
#include "../widget/texts/xxx_text.h"

// 2. 在 init() 的 if-else 链中新增分支：
} else if (name == Globals::key_xxx_state) {
    text = new XxxText();
}
```

---

## 3. 添加到构建

修改 `CMakeLists.txt`，在 `add_executable` 中添加文件：

```cmake
add_executable(sakurapi_clock
    ...
    widget/texts/xxx_text.cpp
    widget/texts/xxx_text.h
)
```

---

## 4. 配置使用

在主题的 `config.ini` 中添加配置段（扁平格式，无需 `[section]`）：

```ini
xxx_state.font = font.ttf
xxx_state.size = 24
xxx_state.color = 0x7ECF2AFF
xxx_state.colorOutline = 0xFFFFFFFF
xxx_state.outlineSize = 2
xxx_state.x = 9
xxx_state.y = 100
```

### 配置项说明

| 配置项 | 说明 | 示例 |
|--------|------|------|
| `xxx_state.font` | 字体文件名 | `font.ttf` |
| `xxx_state.size` | 字号 | `24` |
| `xxx_state.color` | 文字颜色（0xAABBGGRR） | `0x7ECF2AFF` |
| `xxx_state.colorOutline` | 描边颜色 | `0xFFFFFFFF` |
| `xxx_state.outlineSize` | 描边粗细 | `2` |
| `xxx_state.x` / `y` | 显示位置 | `x = 9`, `y = 100` |

---

## 完整示例：ip_text

参考本项目中的 `widget/texts/ip_text.h` 和 `widget/texts/ip_text.cpp`，这是一个完整的新增组件的实现。
