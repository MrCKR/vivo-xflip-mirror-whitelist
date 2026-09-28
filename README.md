# 魔镜白名单 · vivo X Flip

用于扩展 **vivo X Flip 原生「魔镜应用」候选白名单**的本机 Android 管理器。不需要 Root，不依赖 CoverScreen OS，不修改 vivo 系统 APK。

> 本项目非 vivo 官方产品。厂商私有设置和组件可能随系统更新变化。请按需放行，勿把「加入白名单」理解为应用已完成外屏适配。

## 功能

- 列出当前用户可启动的应用，展示真实应用图标。
- 单项加入 / 移出原生外屏候选白名单。
- 应用排序仅使用 **放行状态 + 名称**：已放行优先，同状态按中文区域名称顺序，无特殊应用置顶。
- 搜索应用名或包名，筛选全部 / 已放行 / 未放行。
- 写入后回读校验；写入失败时尝试回滚本次修改，避免两个设置部分写入。
- 默认修改后自动刷新原生外屏缓存，**不自动打开魔镜管理页**。
- 独立「魔镜管理」按钮；必要时先完成刷新再打开原生管理页。
- 浅色青绿原生界面，Canvas 矢量折叠手机插画。
- 已删除测试用备份 / 还原功能，不保存白名单历史备份。

## 实测范围

| 项目 | 已验证环境 |
|---|---|
| 设备 | vivo X Flip，V2256A / PD2256 |
| 系统 | Android 16 / OriginOS 16 |
| 外屏组件 | `com.vivo.fliplauncher` 4.0.0.1 |
| Shizuku SDK | 13.1.5 |
| 应用版本 | 1.3.1，versionCode 5 |

已实测 Minis 出现在原生「可添加」区，加入后通过折叠状态下的「外屏桌面 → 魔镜应用 → Minis」成功启动。其他设备、所有第三方应用、重启后的长期保持和 OTA 更新兼容性未做全面验证。

应用最低 SDK 为 33，但这并不代表所有 Android 13+ 设备可用：需要上述 vivo 私有设置和 FlipLauncher 组件。

## 使用

1. 安装应用，启动 Shizuku，并在应用内为本管理器授予 Shizuku 权限。
2. 为本应用授予安全设置写权限（只需一次；卸载后需重新授权）：

   ```sh
   adb shell pm grant com.openminis.mirrorwhitelist android.permission.WRITE_SECURE_SETTINGS
   ```

   使用 Shizuku shell 时去掉前面的 `adb shell`。Shizuku 授权和 `WRITE_SECURE_SETTINGS` 是两项不同权限。
3. 开启所需应用的白名单开关，等待自动刷新成功。
4. 点击 **魔镜管理**，在 vivo 原生「可添加」区点选应用。
5. 折叠、点亮并解锁外屏，从原生 **魔镜应用**入口打开它。

### 白名单不是「已添加」列表

本管理器维护的是原生候选范围。应用出现于候选区后，仍由用户在 vivo 原生页面完成添加，不直接写受保护的外屏数据库。原生管理界面已添加上限为 **32 个应用**，本项目不移除这一上限。Provider 输出的模型数量包含系统卡片，不等于魔镜已添加应用数。

「全部加入」位于更多菜单，默认保留厂商排除列表；不建议把批量放行作为默认操作。已在外屏添加的应用移出候选白名单后，也不保证同步从外屏删除，请在原生管理页移除。

## 刷新原理

1. App 自身使用 `WRITE_SECURE_SETTINGS` 写入并回读：
   - `Settings.Secure/fold_adaptive_screen_app_list`
   - `Settings.Secure/fold_adaptive_app_not_in_lab`
2. Shizuku 停止 `com.vivo.fliplauncher`（**不清除应用数据**）。
3. **先启动 display 1 的 `.Launcher`**，让原生外屏环境和应用模型完成初始化。
4. 初始等待 2.5 秒，轮询 Provider 模型数量，连续两次非零且稳定后完成刷新；设有初始化超时和命令超时。
5. 自动刷新不打开管理页；只有用户点击按钮才进入 `SETTING_APP_LIB`。

初始化顺序很重要：先打开原生管理页可能提前缓存空候选列表。当前实测系统还会静默拒绝 shell 直接写 Secure 设置，所以采用 App 自身写入 + Shizuku 刷新，而不是 `settings put` 一条命令。

初始化状态以模型数量稳定作为判断依据，是针对实测系统的兼容方案，并非厂商公开保证的接口。刷新会短暂中断外屏桌面或当前外屏任务；建议在内屏管理。

## 构建

### Android Studio / 标准 Android SDK

- JDK 17
- Gradle **8.11.1**
- Android Gradle Plugin **8.10.1**
- Android SDK Platform 35，Build Tools 35.0.0

导入项目后使用 Android Studio 配置以上工具，或安装 Gradle 后运行：

```sh
gradle assembleDebug
```

Debug APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。仓库目前未包含 Gradle Wrapper，需要本机安装固定版本 Gradle。

### 本机轻量构建

仓库也保留手动资源打包、javac、D8 和 apksig 的构建流程，说明见 [docs/LOCAL_BUILD.md](docs/LOCAL_BUILD.md)。整理后的 `scripts/build-local.sh` 已在 aarch64 Alpine / PRoot 中实测，成功生成 1.3.1 未签名 APK；可选签名工具也已通过编译。标准 Gradle 构建配置尚未在当前环境中完整运行验证。

### 签名

仓库**不包含**维护者用于已安装 APK 的签名密钥。自己构建的 Debug APK 无法直接覆盖不同签名的已安装版本；请勿为覆盖安装把私钥提交到 Git。

## 权限与隐私

- `QUERY_ALL_PACKAGES`：本机列出可启动应用及图标。
- `WRITE_SECURE_SETTINGS`：维护外屏候选白名单。
- Shizuku：执行外屏启动器刷新与状态检查。
- Shizuku Provider 的跨用户权限用于受保护的 Binder 传递，并非普通用户需单独授予的权限。

App 不申请 `INTERNET`，没有广告、统计上传、在线图片或账号系统。应用名称、包名和权限状态在本机处理；项目不会上传设备白名单或个人数据。

## 故障排查

- **白名单已开却没有候选**：点击重新刷新，等待完成后再进入魔镜管理；不要先手动反复重启并打开原生设置页。
- **Shizuku 未连接**：先启动 Shizuku，再授权本管理器。手机重启后通常要重新启动 Shizuku。
- **写入未授权**：授予 `WRITE_SECURE_SETTINGS`，不要只开启 Shizuku 授权。
- **刷新超时**：确认外屏可解锁、系统组件未被禁用，重试；不建议清除 FlipLauncher 数据。
- **候选应用在外屏显示不完整**：不适配的页面、输入法、横竖屏、安全窗口等仍受应用和厂商限制。

## 发布说明

当前源码为 1.3.1。发布内容不含 vivo / CoverScreen OS APK、反编译源码、手机私有配置、访问令牌或签名密钥。依赖通过官方 Maven 坐标取得，相关声明见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

开源许可证尚待项目所有者确认；在确认前，本仓库不授予额外的软件使用、修改或再分发许可。公开可见不等于自动获得 MIT 等开源授权。
