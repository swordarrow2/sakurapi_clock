# 配置键参考

`config.ini` 的可用配置键。格式：`key = value`。

---

## 全局 / 系统

| 键 | 类型 | 默认值 | 说明 |
|-----|------|---------|-------------|
| `cfg.name` | string | - | 主题名称 |
| `cfg.delay` | int | - | 背景轮播间隔（帧数） |
| `hardware.screen_width` | int | - | 屏幕宽度（像素） |
| `hardware.screen_height` | int | - | 屏幕高度（像素） |
| `gui.show_cursor` | int (0/1) | - | 显示鼠标光标 |
| `performance.max_fps` | int | - | UI 更新间隔（1 = 每帧）。值越大更新越慢 |
| `background.dir` | string | - | 背景图片目录（相对于主题 tar 根目录）。设置后该目录下的图片用作轮播 |
| `background.file` | string | - | 指定背景图片文件名。同时设置 `background.dir` 和 `background.file` 时，`background.file` 优先，使用单张静态背景；否则 `background.dir` 下的所有图片循环轮播 |

---

## 组件

以下是可用的组件。每个组件有自己的配置键（见下方章节）。

| 组件 | 说明 |
|-----------|-------------|
| `time` | 当前时间显示 |
| `date` | 当前日期显示 |
| `fps` | FPS 计数器 |
| `cpu_usage` | CPU 使用率 |
| `cpu_temp` | 仅 CPU 温度 |
| `memory_state` | RAM 使用率 |
| `storage_state` | 磁盘使用率 |
| `ip_state` | IP 地址显示 |

---

## 通用组件键

以下键适用于每个组件。将 `<name>` 替换为组件名称（例如 `time`、`cpu_usage`、`memory_state` 等）。

### 字体

| 键 | 类型 | 默认值 | 说明 |
|-----|------|---------|-------------|
| `<name>.font` | string | - | 字体文件名（在主题 tar 包内） |
| `<name>.size` | int | - | 字号（磅值） |

### 颜色与描边

| 键 | 类型 | 默认值 | 说明 |
|-----|------|---------|-------------|
| `<name>.color` | hex/RGBA | - | 文字前景色 |
| `<name>.colorOutline` | hex/RGBA | - | 文字描边颜色 |
| `<name>.outlineSize` | int | 2 | 描边粗细（像素） |

颜色格式（十六进制不区分大小写）：

| 格式 | 示例 | 说明 |
|--------|---------|-------------|
| `0xRRGGBBAA` | `0x7ECF2AFF` | 8 位十六进制带 `0x` 前缀，字节顺序：RR GG BB AA |
| `RRGGBBAA` | `7ECF2AFF` | 8 位十六进制无前缀 |
| `0xRRGGBB` | `0x7ECF2A` | 6 位十六进制带 `0x` 前缀，Alpha 默认为 FF |
| `RRGGBB` | `7ECF2A` | 6 位十六进制无前缀，Alpha 默认为 FF |
| `R,G,B,A` | `126,207,42,255` | 十进制 RGBA 值，逗号分隔 |
| `R,G,B` | `126,207,42` | 十进制 RGB，Alpha 默认为 255 |

### 位置

| 键 | 类型 | 默认值 | 说明 |
|-----|------|---------|-------------|
| `<name>.x` | int | - | X 坐标（像素） |
| `<name>.y` | int | - | Y 坐标（像素） |

### 进度条

以下键适用于显示进度条的组件（`cpu_usage`、`memory_state`、`storage_state`）。

| 键 | 类型 | 默认值 | 说明 |
|-----|------|---------|-------------|
| `<name>.mode` | int | 2 | 显示模式：0 = 仅数字，1 = 标签 + 进度条，2 = 两者都显示 |
| `<name>.progress_width` | int | 80 | 进度条宽度（像素） |
| `<name>.progress_height` | int | 8 | 进度条高度（像素） |
| `<name>.progress_min` | float | 0.0 | 进度条范围最小值 |
| `<name>.progress_max` | float | 100.0 | 进度条范围最大值 |

---

## 组件特有键

### ip_state

| 键 | 类型 | 默认值 | 说明 |
|-----|------|---------|-------------|
| `ip_state.interface` | string | (空) | 要显示的网络接口（例如 `wlan0`、`eth0`）。如果为空，则显示所有非回环接口 |

---

## 示例

```ini
hardware.screen_width = 480
hardware.screen_height = 272
performance.max_fps = 1

cpu_usage.font = font.ttf
cpu_usage.size = 24
cpu_usage.color = 0x7ECF2AFF
cpu_usage.outlineSize = 2
cpu_usage.x = 15
cpu_usage.y = 1
cpu_usage.mode = 1
cpu_usage.progress_width = 100
cpu_usage.progress_height = 10
cpu_usage.progress_min = 0
cpu_usage.progress_max = 100
```
