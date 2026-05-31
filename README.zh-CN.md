# Code Path Copy

[English](README.md) | 简体中文

Code Path Copy 是一个 IntelliJ IDEA 插件，可以把编辑器中选中的代码片段转换成适合 Codex 使用的文件引用：

```text
@/absolute/path/File.java#L1-L10
```

你可以把引用复制到剪贴板，也可以直接发送到专用的 Codex CLI 终端标签页。

## 功能

- 将选中的代码范围复制为 `@path#Lstart-Lend` 引用。
- 从编辑器右键菜单把引用发送到 Codex CLI。
- 通过打开 macOS Codex 桌面端并从剪贴板粘贴，把引用发送到桌面端。
- 选中代码后显示一个小型快捷发送按钮。
- 按项目配置 Codex 命令、终端启动延迟和选区提示行为。
- 仅在存在文件选区时显示编辑器动作，避免污染右键菜单。

## 要求

- IntelliJ IDEA 2023.2 或更新版本。
- 开发环境需要 Java 17。
- 启用内置 Terminal 插件。
- 可选：安装 Codex CLI，并确保它在 `PATH` 中可用。

## 从源码安装

构建插件 ZIP：

```bash
./gradlew buildPlugin
```

生成的 ZIP 位于：

```text
build/distributions/
```

在 IntelliJ IDEA 中打开 `Settings | Plugins | Install Plugin from Disk...`，选择生成的 ZIP 文件安装。

## 开发

启动带插件的沙箱 IDE：

```bash
./gradlew runIde
```

验证插件配置：

```bash
./gradlew verifyPlugin
```

完整构建：

```bash
./gradlew buildPlugin
```

## 配置

打开 IntelliJ IDEA 设置并搜索 `Code Path Copy`。

- `Codex command`：Codex 终端标签页启动时执行的命令。默认值：`codex`。
- `Startup delay`：启动 Codex 后首次发送引用前等待的时间。默认值：`1500ms`。
- `Codex Desktop bundle ID`：用于桌面端发送的 macOS 应用 bundle ID。默认值：`com.openai.codex`。
- `Desktop paste delay`：激活 Codex Desktop 后等待粘贴的时间。默认值：`500ms`。
- `Show quick Codex button`：控制选中代码后是否显示快捷按钮。

发送到 Codex Desktop 使用 macOS AppleScript 粘贴自动化。首次使用时，macOS 可能会要求你给 IntelliJ IDEA 授予辅助功能权限。

## 使用

1. 在编辑器中选中代码。
2. 右键点击选区。
3. 选择 `Copy Code Path Reference`、`Send Code Path Reference to Codex` 或 `Send Code Path Reference to Codex Desktop`。

复制或发送的内容类似：

```text
@/Users/me/project/src/main/java/App.java#L12-L20
```

## 贡献

欢迎提交 issue 和 pull request。提交 pull request 前，请先运行 `./gradlew verifyPlugin`。

## 许可证

本项目使用 MIT License。中文参考译本见 [LICENSE.zh-CN.md](LICENSE.zh-CN.md)，正式法律文本以 [LICENSE](LICENSE) 为准。
