# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

**CodePathCopy** 是一个 IntelliJ IDEA 插件，用于生成代码路径引用并与 Codex CLI 集成。该插件允许用户选择代码片段并生成格式化的引用（`@/path/to/file.java#L1-L10`），可以复制到剪贴板或直接发送到 Codex 终端。

## 构建和开发命令

### 构建插件
```bash
./gradlew buildPlugin
```
构建产物位于 `build/distributions/`

### 运行插件（开发模式）
```bash
./gradlew runIde
```
启动带有插件的 IntelliJ IDEA 沙箱实例

### 清理构建
```bash
./gradlew clean
```

### 验证插件配置
```bash
./gradlew verifyPlugin
```

## 核心架构

### 引用格式生成器
**CodePathReference** (`CodePathReference.java`) 是核心工具类，负责将编辑器选择转换为标准化引用格式：
- 格式：`@{绝对路径}#L{起始行}-L{结束行}`
- 行号从 1 开始（用户可见的行号）
- 结束偏移量调整：`Math.max(startOffset, endOffset - 1)` 确保单行选择的正确性

### 双操作模式
插件提供两个上下文菜单操作（右键菜单），两者共享相同的引用构建逻辑但有不同的目标：

1. **CopyCodePathReferenceAction** - 复制到剪贴板
   - 使用 `CopyPasteManager.getInstance().setContents()`
   - 适用于手动粘贴到外部工具

2. **SendCodePathReferenceToCodexAction** - 发送到 Codex 终端
   - 通过 `CodexTerminalService` 管理专用终端会话
   - 自动启动 `codex` CLI（如果尚未运行）
   - 使用 800ms 启动延迟确保 CLI 准备就绪

### 终端集成架构
**CodexTerminalService** 是项目级服务，管理与 Codex CLI 的交互：
- **会话管理**：维护名为 "Codex" 的专用终端标签
- **生命周期**：跟踪 `codexStarted` 状态以避免重复启动
- **自动激活**：发送消息时显示终端工具窗口
- **延迟机制**：首次启动时使用 `Alarm` 实现 800ms 延迟，确保 CLI 初始化完成

### 操作可见性模式
两个操作都使用相同的 `update()` 模式：
- 仅当编辑器有活动选择时可见和启用
- 需要三个条件：`editor != null && file != null && hasSelection()`
- 避免在没有适用上下文时污染菜单

### 通知系统
**CodePathNotifier** 提供统一的用户反馈：
- 使用 IntelliJ 平台通知组（气球提示）
- 通知组 ID：`"CodePathCopy"`（在 `plugin.xml` 中定义）
- 所有消息都是信息级别（`NotificationType.INFORMATION`）

## 技术约束

### IntelliJ 平台版本
- **目标版本**：2023.2 (build 232)
- **兼容范围**：232 至 241.*
- **平台类型**：IC (IntelliJ IDEA Community)
- **Java 版本**：17

### 插件依赖
- `com.intellij.modules.platform` - 核心平台 API
- `org.jetbrains.plugins.terminal` - 终端集成必需

## 代码规范

### 包结构
- 单包设计：`hsin.codepathcopy`
- 所有类都在同一包中（小型插件架构）

### 类命名模式
- **Action 类**：以 `Action` 结尾（如 `CopyCodePathReferenceAction`）
- **Service 类**：以 `Service` 结尾（如 `CodexTerminalService`）
- **工具类**：描述性名称，final 类 + 私有构造函数（如 `CodePathReference`、`CodePathNotifier`）

### IntelliJ 平台集成模式
- **项目级服务**：使用 `@Service(Service.Level.PROJECT)` 注解
- **服务获取**：`project.getService(CodexTerminalService.class)`
- **编辑器数据访问**：`e.getData(CommonDataKeys.EDITOR)`
- **选择模型**：`editor.getSelectionModel()` 用于范围计算

### 空值处理
- 始终检查平台提供的对象（`project`、`editor`、`file`）
- 在继续之前使用早期返回模式验证先决条件
- 通过通知向用户提供清晰的错误消息

## 插件配置（plugin.xml）

### 操作注册
- 两个操作都注册在 `EditorPopupMenu` 组
- 锚点：`anchor="last"`（在上下文菜单末尾）
- 操作 ID 遵循完全限定命名：`hsin.codepathcopy.{ClassName}`

### 通知组
- 必须在使用前声明：`<notification-group id="CodePathCopy" displayType="BALLOON" />`
- 显示类型：BALLOON（非模态、非侵入式）

## 开发注意事项

- **终端小部件生命周期**：在使用前始终检查 `!Disposer.isDisposed(terminalWidget)`
- **线程模型**：`CodexTerminalService` 中的 Alarm 使用 `Alarm.ThreadToUse.SWING_THREAD`
- **路径处理**：使用 `VirtualFile.getPath()` 获取绝对路径
- **行号转换**：文档行号从 0 开始，用户可见行号从 1 开始（需要 `+1`）
- **选择范围**：`getSelectionEnd()` 是排他性的，使用 `endOffset - 1` 进行行计算

## 测试工作流

1. 运行 `./gradlew runIde` 启动沙箱 IDE
2. 打开任何 Java 项目
3. 选择代码片段
4. 右键单击以访问上下文菜单
5. 测试两个操作：
   - "Copy Code Path Reference" - 验证剪贴板内容
   - "Send Code Path Reference to Codex" - 验证终端集成（需要安装 `codex` CLI）

## 构建产物

- 插件 ZIP：`build/distributions/CodePathCopy-{version}.zip`
- 可以通过 IntelliJ IDEA 的"从磁盘安装插件"选项安装
