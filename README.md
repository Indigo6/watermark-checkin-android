# 水印打卡相机 Android 版

一款用于外勤打卡的 Android 水印相机应用。

## 应用版本

本仓库通过同一套代码构建两个应用版本：

- `manual`：手动填写位置文字，不申请网络权限，也不集成地图 SDK。
- `smart`：支持智能定位流程，申请网络权限，并预留后端逆地理编码扩展接口。

## 构建命令

```bash
./gradlew :app:assembleManualDebug
./gradlew :app:assembleSmartDebug
```

构建发布版本：

```bash
./gradlew :app:assembleManualRelease
./gradlew :app:assembleSmartRelease
```

## 环境要求

必需环境：

- JDK 17
- Android SDK
- 与 Android Gradle Plugin 兼容的 Gradle

初始服务器安装用户级 JDK 17 和 Android SDK 后可以运行 Gradle，但 CPU 和内存资源有限。建议使用 GitHub Actions 或性能更强的本地计算机来构建 APK。

GitHub Actions 已在 `.github/workflows/android-ci.yml` 中配置。每次向 `main` 分支推送代码时，它都会运行测试、构建两个版本的调试 APK，并将其作为工作流构建产物上传。

## 当前 MVP 功能

- 获得相机权限后，直接打开 CameraX 预览界面。
- 实时显示水印叠加层。
- 在手动版中，工作人员可以点击水印并编辑位置文字。
- 拍照时先将相机图像写入缓存，再把水印渲染到位图中，最后将成片保存到相册。
- 共享领域逻辑涵盖水印文字生成、坐标、来源元数据和位置解析。
- 智能版目前已包含本地匹配、缓存和后端抽象，仍需接入真实的后端接口。

## 智能定位策略

智能版应按以下顺序解析用于显示的位置：

1. GPS 坐标附近已配置的本地打卡地点。
2. GPS 坐标附近的缓存地址。
3. 后端逆地理编码代理。
4. 使用坐标作为兜底显示内容。

请勿将地图服务商的密钥直接写入 Android 应用。
