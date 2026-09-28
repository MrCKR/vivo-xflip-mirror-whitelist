# 开发与实现说明

普通用户请先阅读 [首页使用指南](../README.md)。本页仅面向修改源码或自行构建的开发者。

## 标准 Android 构建

- JDK 17
- Gradle 8.11.1
- Android Gradle Plugin 8.10.1
- Android SDK Platform 35，Build Tools 35.0.0

导入 Android Studio 后配置上述工具，或使用已安装的固定版本 Gradle：

```sh
gradle assembleDebug
```

Debug APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。仓库未包含 Gradle Wrapper，需要另行安装 Gradle。

标准 Gradle 路径尚未在当前环境中完整运行验证。[轻量构建流程](LOCAL_BUILD.md)已在 aarch64 Alpine / PRoot 中验证，可从当前源码生成 1.3.1 未签名 APK；可选签名工具也已通过编译。

## 签名与更新

维护者的签名密钥不在仓库中。独立构建生成的 APK 不能直接覆盖不同签名的已安装版本；不要为覆盖安装而把私钥提交到 Git。

## 白名单与原生数据库

App 自身持有 `WRITE_SECURE_SETTINGS` 后维护以下两个键：

- `Settings.Secure/fold_adaptive_screen_app_list`：候选允许列表。
- `Settings.Secure/fold_adaptive_app_not_in_lab`：候选排除列表。

写入后回读验证；失败时尝试恢复本次修改前的值，避免部分写入。该失败回滚不等于用户备份/还原功能，不保存历史备份。

在已验证的系统中，shell 直接 `settings put secure` 会被静默拒绝。因此 App 自身负责写入，Shizuku 只负责外屏刷新；授权 Shizuku 并不替代 `WRITE_SECURE_SETTINGS`。

本项目不直接插入、删除 FlipLauncher 受保护的应用数据库。应用放行后，用户仍需在 vivo 原生魔镜管理页点选添加。原生上限为 32 个魔镜应用；Provider 模型数量包含系统卡片，不能用来表示魔镜已添加数量。

## 刷新顺序

1. 通过 Shizuku 执行 `am force-stop com.vivo.fliplauncher`，不清除数据。
2. **先启动 display 1 的 `com.vivo.fliplauncher/.Launcher`**，初始化外屏环境。
3. 初始等待 2.5 秒后轮询 Provider 全模型数量。
4. 连续两次非零且稳定后完成刷新；设有 20 秒初始化超时及单条命令超时。
5. 自动刷新默认不打开管理页。仅点击「魔镜管理」按钮时进入 `SETTING_APP_LIB`，如尚未完成刷新则先刷新再打开。

如果先打开管理页，原生进程可能把未初始化的空候选列表缓存起来。模型数量稳定只是针对当前实测系统的就绪判断，并非厂商公开保证的接口，OTA 后可能需要调整。

## 界面及排序

界面由原生 Android View 和 Canvas 构建，折叠手机插画为原创代码绘制。安装应用图标由 PackageManager 在用户设备本地加载，不包含在仓库资源中。

排序仅使用两项：已放行优先，同状态用 `Locale.CHINA` 的 Collator 比较应用名称。没有特殊包名置顶，搜索和筛选保持相同顺序。

## 验证范围

实测 vivo X Flip V2256A / PD2256、Android 16 / OriginOS 16、FlipLauncher 4.0.0.1。

已确认单项写入、原生候选刷新、Minis 加入原生列表，以及折叠后从原生魔镜入口启动。其他机型、所有第三方 App、设备重启后的长期保持及 OTA 兼容性尚未全面验证。

## 权限与公开内容

- `QUERY_ALL_PACKAGES`：列出本机可启动应用。
- `WRITE_SECURE_SETTINGS`：维护白名单键。
- Shizuku：执行刷新、模型状态检查和必要的原生页面启动。
- Shizuku Provider 的跨用户权限：用于受保护的 Binder 传递，不是普通用户另行授予的一项权限。

App 不申请 `INTERNET`。仓库不包含访问令牌、签名私钥、用户配置、vivo/CoverScreen OS APK 或反编译代码。

第三方依赖版权与许可见 [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md)。
