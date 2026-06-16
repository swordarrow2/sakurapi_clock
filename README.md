# sakurapi_clock

English | [中文](README.zh.md)

#### Introduction
A desktop clock designed for Sakura Pi. Sakura Pi: https://docs.sakurapi.org/article/sakurapi-rk3308b/introduce

> Theoretically compatible with any Linux device capable of running the SDL2 library

#### Hardware Preparation

Install screen：https://docs.sakurapi.org/article/sakurapi-rk3308b/playground/rgb-display

#### Compiling the Code
1. Install compilation tools and dependency libraries

```
sudo apt update
```

```
sudo apt install build-essential cmake libsdl2-dev libsdl2-image-dev libsdl2-ttf-dev libdrm-dev libegl1-mesa-dev
```

2. Clone this project
   Copy the command line from the`Clone Project`button on the git website

3. Create a build directory


```
root@sakurapi-rk3308b:~/sakurapi_clock$ mkdir build
```

4. Enter the build directory

```
root@sakurapi-rk3308b:~/sakurapi_clock$ cd build/
```

5. Run Cmake

```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ cmake ..
```

6. Run the compiler

```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ make
```

- You can use multi-threading to speed up compilation.

```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ make -j4
```

7. Run the program.

```
./sakurapi_clock
```

#### Package a Theme

1. Go to the `themes` directory and check the theme you want to use.

2. Run the program with the argument `--pack [theme_dir_name]`


```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ ./sakurapi_clock --pack theme_sanae
```

The packaged theme will be automatically placed in the `build/theme` directory.

#### Pack All Themes

Run the program with `--packall` to pack all themes at once:

```
./sakurapi_clock --packall
```

All themes from the `themes` directory will be packed and placed in the `build/theme` directory.

#### Unpack a Theme

1. Go to the `build/theme directory` and check the theme you want to unpack.

2. Run the program with the argument `--unpack [theme_name]`

```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ ./sakurapi_clock --unpack theme_sanae
```
The unpacked theme will be automatically placed in the `theme` directory.


#### Direct Launch with Specified Theme

1. Enter the `themes` directory to view the theme you want to use.

2. Run the program with the parameter `[theme_dir_name]`


```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ ./sakurapi_clock theme_sanae
```

The program will launch with the specified theme and automatically package the theme into the `build/theme` directory

#### Auto-start on Boot

Run the program with `--autorun` to install a systemd service for auto-start on boot:

```
sudo ./sakurapi_clock --autorun
```

This will:
- Create a systemd service file at `/etc/systemd/system/sakurapi_clock.service`
- Enable the service to start automatically on boot

Useful commands:
- `sudo systemctl start sakurapi_clock` - Start service now
- `sudo systemctl stop sakurapi_clock` - Stop service
- `sudo systemctl status sakurapi_clock` - Check service status
- `sudo systemctl disable sakurapi_clock` - Disable auto-start

#### Others


1. The code distinguishes between armbian/debian and ubuntu for conditional compilation.

The program compiled in the armbian/debian environment is a normal clock program. The program compiled under Ubuntu will create a new window where interface elements can be dragged and their positions displayed, facilitating custom theme creation.

2. Currently learning C++, the code is entirely created through incantation magic and Java experience.