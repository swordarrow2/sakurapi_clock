# Config Keys Reference

Available configuration keys for `config.ini`. Format: `key = value`.

---

## Global / System

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `cfg.name` | string | - | Theme name |
| `cfg.delay` | int | - | Background slideshow interval (in frames) |
| `hardware.screen_width` | int | - | Screen width in pixels |
| `hardware.screen_height` | int | - | Screen height in pixels |
| `gui.show_cursor` | int (0/1) | - | Show mouse cursor |
| `performance.max_fps` | int | - | UI update interval (1 = every frame). Higher = slower update |
| `background.dir` | string | - | Background image directory (relative to theme tar root). If set, images in this directory are used as a slideshow |
| `background.file` | string | - | Specific background image filename. When both `background.dir` and `background.file` are set, `background.file` takes priority and a single static background is used; otherwise all images in `background.dir` cycle as a slideshow |

---

## Components

The following components are available. Each component has its own set of config keys (see sections below).

| Component | Description |
|-----------|-------------|
| `time` | Current time display |
| `date` | Current date display |
| `fps` | FPS counter |
| `cpu_usage` | CPU usage |
| `cpu_temp` | CPU temperature only |
| `memory_state` | RAM usage |
| `storage_state` | Disk usage |
| `ip_state` | IP address display |

---

## Common Component Keys

The following keys apply to every component. Replace `<name>` with the component name (e.g. `time`, `cpu_usage`, `memory_state`, etc.).

### Font

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `<name>.font` | string | - | Font filename (inside theme tar) |
| `<name>.size` | int | - | Font size in points |

### Color & Outline

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `<name>.color` | hex/RGBA | - | Text foreground color |
| `<name>.colorOutline` | hex/RGBA | - | Text outline color |
| `<name>.outlineSize` | int | 2 | Outline thickness in pixels |

Color formats (case-insensitive hex):

| Format | Example | Description |
|--------|---------|-------------|
| `0xRRGGBBAA` | `0x7ECF2AFF` | 8-digit hex with `0x` prefix, byte order: RR GG BB AA |
| `RRGGBBAA` | `7ECF2AFF` | 8-digit hex without prefix |
| `0xRRGGBB` | `0x7ECF2A` | 6-digit hex with `0x` prefix, alpha defaults to FF |
| `RRGGBB` | `7ECF2A` | 6-digit hex without prefix, alpha defaults to FF |
| `R,G,B,A` | `126,207,42,255` | Decimal RGBA values separated by commas |
| `R,G,B` | `126,207,42` | Decimal RGB, alpha defaults to 255 |

### Position

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `<name>.x` | int | - | X position in pixels |
| `<name>.y` | int | - | Y position in pixels |

### Progress Bar

These keys apply to components that display a progress bar (`cpu_usage`, `memory_state`, `storage_state`).

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `<name>.mode` | int | 2 | Display mode: 0 = number only, 1 = label + progress bar, 2 = both |
| `<name>.progress_width` | int | 80 | Progress bar width in pixels |
| `<name>.progress_height` | int | 8 | Progress bar height in pixels |
| `<name>.progress_min` | float | 0.0 | Progress bar range minimum |
| `<name>.progress_max` | float | 100.0 | Progress bar range maximum |

---

## Component-Specific Keys

### ip_state

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `ip_state.interface` | string | (empty) | Network interface to show (e.g. `wlan0`, `eth0`). If empty, all non-loopback interfaces are shown |

---

## Example

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
