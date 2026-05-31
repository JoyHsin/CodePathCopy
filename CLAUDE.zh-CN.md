# CLAUDE.md

[English](CLAUDE.md)

此文件为 Claude Code（claude.ai/code）在本仓库中工作提供指导。

## 项目概览

**CodePathCopy** 是一个 IntelliJ IDEA 插件，用于生成代码路径引用并发送到 Codex 工具。用户可以选择代码片段并生成格式化引用，例如 `@/path/to/file.java#L1-L10`，然后复制到剪贴板、发送到 Codex CLI，或粘贴到 macOS Codex 桌面端。

## 构建和开发命令

### 构建插件

```bash
./gradlew buildPlugin
```

构建产物生成在 `build/distributions/`。

### 以开发模式运行插件

```bash
./gradlew runIde
```

这会启动一个已安装该插件的 IntelliJ IDEA 沙箱实例。

### 清理构建产物

```bash
./gradlew clean
```

### 验证插件配置

```bash
./gradlew verifyPlugin
```

## 核心架构

### 引用格式生成器

**CodePathReference**（`CodePathReference.java`）是核心工具类，负责把编辑器选区转换成标准引用：

- 格式：`@{绝对路径}#L{起始行}-L{结束行}`
- 面向用户输出的行号从 1 开始。
- 结束偏移量使用 `Math.max(startOffset, endOffset - 1)` 调整，确保单行选择计算正确。

### 操作模式

插件提供三个编辑器右键菜单操作。它们共享引用构建逻辑，但目标不同：

1. **CopyCodePathReferenceAction** - 复制到剪贴板
   - 使用 `CopyPasteManager.getInstance().setContents()`。
   - 适合手动粘贴到外部工具。

2. **SendCodePathReferenceToCodexAction** - 发送到 Codex CLI
   - 通过 `CodexTerminalService` 管理专用终端会话。
   - 如果尚未启动，则执行配置的 Codex 命令。
   - 首次发送引用前使用可配置的启动延迟。

3. **SendCodePathReferenceToCodexDesktopAction** - 发送到 macOS Codex Desktop
   - 使用 `CodexDesktopService`。
   - 将引用复制到剪贴板，激活配置的 macOS app bundle ID，然后通过 AppleScript 粘贴。
   - 首次使用时需要给 IntelliJ IDEA 授予 macOS 辅助功能权限。

### 设置

**CodePathSettings** 是项目级持久化服务。设置存储在 `codePathCopy.xml` 中，并通过 **CodePathSettingsConfigurable** 暴露给用户。

可配置项：

- Codex CLI 命令，默认 `codex`。
- CLI 启动延迟，默认 `1500ms`。
- Codex Desktop bundle ID，默认 `com.openai.codex`。
- 桌面端粘贴延迟，默认 `500ms`。
- 快捷 Codex 选区提示开关。

### 终端集成

**CodexTerminalService** 是项目级服务，管理与 Codex CLI 的交互：

- 维护名为 `Codex` 的专用终端标签页。
- 跟踪 `codexStarted`，避免在同一个 widget 中重复启动 Codex。
- 发送消息前激活 Terminal 工具窗口。
- 使用 `Alarm.ThreadToUse.SWING_THREAD` 的 `Alarm` 实现延迟发送。

### 桌面端集成

**CodexDesktopService** 负责 macOS Codex Desktop 发送：

- 仅在 macOS 上运行。
- 使用 IntelliJ 剪贴板 API 复制生成的引用。
- 运行 `osascript` 激活配置的 app bundle ID 并执行 `Command+V`。
- 失败时通过通知提示，包括辅助功能权限提醒。

### 操作可见性模式

编辑器动作使用 **CodePathActionUtil**：

- 仅当存在 editor、virtual file 和活动选区时，动作才可见并启用。
- 这样可以在动作不适用时保持右键菜单干净。

### 选区提示

**CodePathSelectionHintService** 监听编辑器选区变化，并在启用时在选区附近显示一个小型 Codex 快捷发送按钮。

### 通知系统

**CodePathNotifier** 提供统一的用户反馈：

- 使用 IntelliJ 平台通知组。
- 通知组 ID：`"CodePathCopy"`，在 `plugin.xml` 中定义。
- 当前消息使用 `NotificationType.INFORMATION`。

## 技术约束

### IntelliJ 平台版本

- 目标版本：2023.2.5（build 232）
- 兼容构建范围：232 至 253.*
- 平台类型：IC（IntelliJ IDEA Community）
- Java 版本：17

### 插件依赖

- `com.intellij.modules.platform` - 核心平台 API
- `org.jetbrains.plugins.terminal` - 终端集成

## 代码规范

### 包结构

- 单包：`hsin.codepathcopy`
- 由于这是一个小型插件，所有类都位于同一包中。

### 命名模式

- Action 类以 `Action` 结尾，例如 `CopyCodePathReferenceAction`。
- Service 类以 `Service` 结尾，例如 `CodexTerminalService`。
- 工具类使用 final 类和私有构造函数，例如 `CodePathReference` 和 `CodePathNotifier`。

### IntelliJ 平台集成

- 项目级服务使用 `@Service(Service.Level.PROJECT)`。
- 服务通过 `project.getService(...)` 获取。
- 编辑器数据通过 `e.getData(CommonDataKeys.EDITOR)` 访问。
- 选区范围通过 `editor.getSelectionModel()` 计算。

### 空值处理

- 始终检查平台提供的对象，例如 `project`、`editor` 和 `file`。
- 前置条件失败时使用早返回。
- 通过通知提供清晰的用户反馈。

## 插件配置（`plugin.xml`）

### 操作注册

- 动作注册在 `EditorPopupMenu` 组。
- 锚点：`anchor="last"`。
- Action ID 使用完全限定命名：`hsin.codepathcopy.{ClassName}`。

### 通知组

- 使用前声明：`<notification-group id="CodePathCopy" displayType="BALLOON" />`。
- 显示类型：`BALLOON`。

### 设置页

- 项目设置页通过 `projectConfigurable` 注册。

## 开发注意事项

- 复用终端 widget 前检查 `!Disposer.isDisposed(terminalWidget)`。
- 使用 `VirtualFile.getPath()` 获取绝对路径。
- 文档行号从 0 开始，面向用户的行号从 1 开始。
- `getSelectionEnd()` 是排他性的，因此结束行计算使用 `endOffset - 1`。
- 不要提交本地 IDE 元数据、Gradle 输出、生成的插件 ZIP 或本地 agent 文件。

## 测试流程

1. 运行 `./gradlew runIde` 启动沙箱 IDE。
2. 打开任意 Java 项目。
3. 选择代码片段。
4. 右键点击选区。
5. 测试操作：
   - `Copy Code Path Reference` - 验证剪贴板内容。
   - `Send Code Path Reference to Codex` - 验证终端集成。需要 Codex CLI。
   - `Send Code Path Reference to Codex Desktop` - 验证 macOS app 激活和粘贴。需要 Codex Desktop 和辅助功能权限。

## 构建产物

- 插件 ZIP：`build/distributions/CodePathCopy-{version}.zip`
- 可以通过 IntelliJ IDEA 的 `Install Plugin from Disk` 选项安装。
