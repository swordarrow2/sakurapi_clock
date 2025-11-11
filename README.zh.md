# sakurapi_clock

中文 | [English](README.en.md)

#### 介绍
为樱花派设计的桌面时钟。樱花派：https://docs.sakurapi.org/article/sakurapi-rk3308b/introduce

> 理论上来说能运行SDL2库的Linux设备都可以

#### 硬件准备

安装屏幕：https://docs.sakurapi.org/article/sakurapi-rk3308b/playground/rgb-display

#### 编译代码
1. 安装编译工具和依赖库

```
sudo apt update
```

```
sudo apt install build-essential cmake libsdl2-dev libsdl2-image-dev libsdl2-ttf-dev libdrm-dev libegl1-mesa-dev
```

2. clone本项目
   从git网站的`克隆项目`按钮中复制命令行即可

3. 新建build文件夹


```
root@sakurapi-rk3308b:~/sakurapi_clock$ mkdir build
```

4. 进入build文件夹

```
root@sakurapi-rk3308b:~/sakurapi_clock$ cd build/
```

5. 运行cmake

```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ cmake ..
```

6. 运行编译器

```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ make
```

- 可以使用多线程编译

```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ make -j4
```

7. 启动

```
./sakurapi_clock
```

#### 打包主题

1. 进入themes文件夹内查看想使用的主题

2. 运行主程序，同时带上参数`--pack [theme_dir_name]`


```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ ./sakurapi_clock --pack theme_sanae
```

打包的主题会自动放在build/theme目录

#### 解包主题

1. 进入build/theme内查看想解包的主题

2. 运行主程序，同时带上参数`--unpack [theme_name]`

```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ ./sakurapi_clock --unpack theme_sanae
```
解包的主题会自动放在theme目录


#### 直接使用指定主题启动

1. 进入themes文件夹内查看想使用的主题

2. 运行主程序，同时带上参数`[theme_dir_name]`


```
root@sakurapi-rk3308b:~/sakurapi_clock/build$ ./sakurapi_clock theme_sanae
```

程序将会以指定主题启动，并且会自动打包主题放置在build/theme目录

#### 其它


1. 代码中区分armbian/debian和ubuntu进行条件编译。

armbian/debian环境下编译出的是正常的时钟程序，Ubuntu下编译出的程序会新建一个窗口，同时界面元素可以拖动并显示元素位置，方便自行制作主题。

1. 正在学习c++，代码全靠咏唱魔法和jvav经验。