# Network Speed Indicator

一个安卓流量悬浮窗应用，显示实时网络速度。

## 功能特性

- ✅ 悬浮窗开启/关闭功能
- ✅ 位置移动/锁定功能，搭配位置微调按钮
- ✅ 低速隐藏功能
- ✅ 三种网速格式：总 @网速/上下行横排/上下行竖排
- ✅ 文字对齐（左/中/右）
- ✅ 可自定义文字颜色和大小
- ✅ 可显示在状态栏上方

## 安装说明

1. 克隆仓库：
   ```bash
   git clone https://github.com/HMwancheng/Network-Speed-indicator-Auto.git
   ```

2. 使用Android Studio打开项目
3. 编译并运行

## 使用说明

1. 首次运行需要授予悬浮窗权限和通知权限
2. 在主界面可以开启/关闭悬浮窗
3. 解锁状态下可以拖动悬浮窗调整位置
4. 可以通过微调按钮精确调整位置
5. 支持自定义文字颜色、大小、对齐方式
6. 支持多种网速显示格式
7. 开启低速隐藏后，网速较低时悬浮窗会自动隐藏

## 构建要求

- Android Studio Giraffe或更高版本
- Android SDK 34
- Kotlin 1.9.0
- Gradle 8.4

## 自动构建

项目配置了GitHub Actions自动构建，推送代码到main分支后会自动编译并生成APK文件。
