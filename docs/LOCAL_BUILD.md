# 本机轻量构建

该流程不用 Gradle，适合已有 Android 工具链的开发环境。不要把工具、依赖缓存和签名密钥提交到源码仓库。

## 准备

需要 JDK 17 或 21、`zip`、`aapt`、`zipalign`，并配置：

| 环境变量 | 内容 |
|---|---|
| `ANDROID_JAR` | Android API 35 的 android.jar 路径 |
| `R8_JAR` | 官方 R8/D8 jar 路径，原项目使用 9.4.26 |
| `AAPT` | 当前架构可运行的 aapt 路径 |
| `ZIPALIGN` | 当前架构可运行的 zipalign 路径 |
| `SHIZUKU_DEPS_DIR` | api.jar / provider.jar / aidl.jar / shared.jar 所在目录 |

Shizuku 依赖坐标均为 `dev.rikka.shizuku:<模块>:13.1.5`，从 Maven Central 获取相应 `.aar`，解压其中的 `classes.jar` 并重命名为模块名。不要使用未知来源修改版 SDK。

Android 官方 Build Tools 用于其支持的主机架构。aarch64 Alpine 环境可另行取得兼容的本机二进制工具；这些二进制不包含于仓库。

```sh
export ANDROID_JAR=/path/to/android.jar
export R8_JAR=/path/to/r8.jar
export AAPT=/path/to/aapt
export ZIPALIGN=/path/to/zipalign
export SHIZUKU_DEPS_DIR=/path/to/shizuku-jars
sh scripts/build-local.sh
```

输出为 `out/MirrorWhitelist-unsigned.apk`，没有签名，不能直接安装。使用 Android 官方 `apksigner` 签名，或使用 `scripts/SignApk.java` 与官方 apksig 8.10.1：

```sh
javac -cp /path/to/apksig.jar -d out scripts/SignApk.java
java -cp out:/path/to/apksig.jar SignApk \
  /path/to/private-keystore.p12 signing-alias \
  out/MirrorWhitelist-unsigned.apk out/MirrorWhitelist.apk
```

Java 签名工具从 `SIGNING_STORE_PASSWORD` 和 `SIGNING_KEY_PASSWORD` 环境变量读取密码（后者未设置时使用前者），不将密码写进命令行或源码。请在安全的终端中设置它们，不要提交到 `.env` 文件。

维护者原签名密钥仅保留在本机，不随源码公开。独立生成的密钥不能覆盖其他签名的已安装 App。
