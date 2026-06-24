# Adding a New Text Component

This document explains how to add a new text display component to sakurapi_clock.

## Overview

1. Create the component class (`.h` and `.cpp`)
2. Register into the system
3. Add to the build
4. Configure and use

---

## 1. Create the Component Class

Create `xxx_text.h` and `xxx_text.cpp` in `widget/texts/`.

### Header Template

```cpp
//
// Created by [username] on [date].
//

#ifndef SAKURAPI_CLOCK_XXX_TEXT_H
#define SAKURAPI_CLOCK_XXX_TEXT_H

#include "../draggable_text.h"

class XxxText : public DraggableText {
public:
    // Your data structure (optional)
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
    // Your data fetching method
    XxxInfo getXxxInfo();

    uint32_t updateCount;
};

#endif //SAKURAPI_CLOCK_XXX_TEXT_H
```

### Implementation Template

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
    // 1. Call parent init
    DraggableText::init(name, text, textFont, textColor,
                        outlineColor, outlineSize, initialX, initialY);
    // 2. Set update counter (update once per second)
    updateCount = ConfigManager::getInstance().getInt("performance.max_fps");
}

void XxxText::update() {
    // Called every frame; control update frequency
    if (updateCount == ConfigManager::getInstance().getInt("performance.max_fps")) {
        // Fetch data and update text
        xxxInfo = getXxxInfo();
        std::stringstream ss;
        ss << "XXX: " << xxxInfo.value;
        setText(ss.str());
        updateCount = 0;
    }
    updateCount++;
}

void XxxText::render(SDL_Renderer *renderer) {
    // Render text
    DraggableText::render(renderer);
    // Additional drawing can go here (e.g. progress bar)
}
```

### Key Notes

- **`init()`** — Initialize the component; must call parent `DraggableText::init()`
- **`update()`** — Called every frame; use `updateCount` to throttle actual updates (typically once per second)
- **`render()`** — Draw; call parent `render()` first to render text
- Set text content via `setText()`, the system will create textures automatically

---

## 2. Register into the System

Modify 3 files:

### 2.1 `helper/globals.h`

- Declare config key constant
- Increase `elements` array size

```cpp
// In namespace Globals, add:
extern const std::string key_xxx_state;

// Modify elements array size (N → N+1):
extern const std::string elements[7];  // was 6
```

### 2.2 `helper/globals.cpp`

- Define key constant
- Add to `elements` array

```cpp
// Add constant definition:
const std::string key_xxx_state = "xxx_state";

// Add to elements array:
const std::string elements[] = {
    key_time, key_date, key_fps,
    key_cpu_state, key_memory_state, key_storage_state,
    key_xxx_state  // new
};
```

### 2.3 `resource/text_manager.cpp`

- Add `#include` header
- Add instantiation in `init()` if-else chain

```cpp
// 1. Add include:
#include "../widget/texts/xxx_text.h"

// 2. Add branch in init() if-else chain:
} else if (name == Globals::key_xxx_state) {
    text = new XxxText();
}
```

---

## 3. Add to Build

Modify `CMakeLists.txt`, add files to `add_executable`:

```cmake
add_executable(sakurapi_clock
    ...
    widget/texts/xxx_text.cpp
    widget/texts/xxx_text.h
)
```

---

## 4. Configure and Use

Add configuration to theme `config.ini` (flat format, no `[section]` needed):

```ini
xxx_state.font = font.ttf
xxx_state.size = 24
xxx_state.color = 0x7ECF2AFF
xxx_state.colorOutline = 0xFFFFFFFF
xxx_state.outlineSize = 2
xxx_state.x = 9
xxx_state.y = 100
```

### Configuration Reference

| Key | Description | Example |
|-----|-------------|---------|
| `xxx_state.font` | Font filename | `font.ttf` |
| `xxx_state.size` | Font size | `24` |
| `xxx_state.color` | Text color (8-digit hex: RRGGBBAA) | `0x7ECF2AFF` |
| `xxx_state.colorOutline` | Outline color | `0xFFFFFFFF` |
| `xxx_state.outlineSize` | Outline thickness | `2` |
| `xxx_state.x` / `y` | Screen position | `x = 9`, `y = 100` |

---

## Complete Example: ip_text

Refer to `widget/texts/ip_text.h` and `widget/texts/ip_text.cpp` in this project for a complete implementation of a new component.
