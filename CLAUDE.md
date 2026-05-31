# CLAUDE.md

[简体中文](CLAUDE.zh-CN.md)

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**CodePathCopy** is an IntelliJ IDEA plugin for generating code path references and sending them to Codex tools. It lets users select code and generate formatted references such as `@/path/to/file.java#L1-L10`, then copy them to the clipboard, send them to Codex CLI, or paste them into the macOS Codex desktop app.

## Build And Development Commands

### Build The Plugin

```bash
./gradlew buildPlugin
```

Build artifacts are generated in `build/distributions/`.

### Run The Plugin In Development Mode

```bash
./gradlew runIde
```

This launches an IntelliJ IDEA sandbox instance with the plugin installed.

### Clean Build Outputs

```bash
./gradlew clean
```

### Verify Plugin Configuration

```bash
./gradlew verifyPlugin
```

## Core Architecture

### Reference Format Builder

**CodePathReference** (`CodePathReference.java`) is the core utility that converts editor selections into standardized references:

- Format: `@{absolute path}#L{start line}-L{end line}`
- Line numbers are 1-based for user-facing output.
- End offset adjustment uses `Math.max(startOffset, endOffset - 1)` so single-line selections resolve correctly.

### Action Modes

The plugin exposes three editor context menu actions. They share the same reference-building logic but target different destinations:

1. **CopyCodePathReferenceAction** - copy to clipboard
   - Uses `CopyPasteManager.getInstance().setContents()`.
   - Works well for manually pasting into external tools.

2. **SendCodePathReferenceToCodexAction** - send to Codex CLI
   - Uses `CodexTerminalService` to manage a dedicated terminal session.
   - Starts the configured Codex command if it has not been started yet.
   - Uses a configurable startup delay before sending the first reference.

3. **SendCodePathReferenceToCodexDesktopAction** - send to macOS Codex Desktop
   - Uses `CodexDesktopService`.
   - Copies the reference to the clipboard, activates the configured macOS app bundle ID, then pastes via AppleScript.
   - Requires macOS Accessibility permission for IntelliJ IDEA on first use.

### Settings

**CodePathSettings** is a project-level persistent service. Settings are stored in `codePathCopy.xml` and exposed through **CodePathSettingsConfigurable**.

Configurable values:

- Codex CLI command, default `codex`.
- CLI startup delay, default `1500ms`.
- Codex Desktop bundle ID, default `com.openai.codex`.
- Desktop paste delay, default `500ms`.
- Quick Codex selection hint toggle.

### Terminal Integration

**CodexTerminalService** is a project-level service that manages Codex CLI interaction:

- Maintains a dedicated terminal tab named `Codex`.
- Tracks `codexStarted` to avoid starting Codex repeatedly in the same widget.
- Activates the Terminal tool window before sending a message.
- Uses `Alarm` on `Alarm.ThreadToUse.SWING_THREAD` for delayed sending.

### Desktop Integration

**CodexDesktopService** handles macOS Codex Desktop sending:

- It only runs on macOS.
- It uses the IntelliJ clipboard API to copy the generated reference.
- It runs `osascript` to activate the configured app bundle ID and issue `Command+V`.
- It reports failures with a notification, including Accessibility permission hints.

### Action Visibility Pattern

Editor actions use **CodePathActionUtil**:

- Actions are visible and enabled only when an editor, virtual file, and active selection are present.
- This keeps the editor context menu clean when the action is not applicable.

### Selection Hint

**CodePathSelectionHintService** listens for editor selection changes and shows a small Codex quick-send button near the selection when enabled.

### Notifications

**CodePathNotifier** provides consistent user feedback:

- Uses the IntelliJ platform notification group.
- Notification group ID: `"CodePathCopy"` defined in `plugin.xml`.
- Messages currently use `NotificationType.INFORMATION`.

## Technical Constraints

### IntelliJ Platform Version

- Target version: 2023.2.5 (build 232)
- Compatible build range: 232 to 253.*
- Platform type: IC (IntelliJ IDEA Community)
- Java version: 17

### Plugin Dependencies

- `com.intellij.modules.platform` - core platform API
- `org.jetbrains.plugins.terminal` - terminal integration

## Code Conventions

### Package Structure

- Single package: `hsin.codepathcopy`
- All classes live in the same package because this is a small plugin.

### Naming Pattern

- Action classes end with `Action`, such as `CopyCodePathReferenceAction`.
- Service classes end with `Service`, such as `CodexTerminalService`.
- Utility classes are final with private constructors, such as `CodePathReference` and `CodePathNotifier`.

### IntelliJ Platform Integration

- Project-level services use `@Service(Service.Level.PROJECT)`.
- Services are retrieved through `project.getService(...)`.
- Editor data is accessed with `e.getData(CommonDataKeys.EDITOR)`.
- Selection ranges are calculated through `editor.getSelectionModel()`.

### Null Handling

- Always check platform-provided objects such as `project`, `editor`, and `file`.
- Use early returns before continuing after failed preconditions.
- Provide clear user-facing messages through notifications.

## Plugin Configuration (`plugin.xml`)

### Action Registration

- Actions are registered in the `EditorPopupMenu` group.
- Anchor: `anchor="last"`.
- Action IDs follow fully qualified naming: `hsin.codepathcopy.{ClassName}`.

### Notification Group

- Declare before use: `<notification-group id="CodePathCopy" displayType="BALLOON" />`.
- Display type: `BALLOON`.

### Settings Page

- The project settings page is registered with `projectConfigurable`.

## Development Notes

- Check `!Disposer.isDisposed(terminalWidget)` before reusing a terminal widget.
- Use `VirtualFile.getPath()` for absolute paths.
- Document line numbers are 0-based, while user-facing line numbers are 1-based.
- `getSelectionEnd()` is exclusive, so use `endOffset - 1` for end-line calculation.
- Do not commit local IDE metadata, Gradle outputs, generated plugin ZIPs, or local agent files.

## Test Workflow

1. Run `./gradlew runIde` to start the sandbox IDE.
2. Open any Java project.
3. Select a code snippet.
4. Right-click the selection.
5. Test the actions:
   - `Copy Code Path Reference` - verify clipboard content.
   - `Send Code Path Reference to Codex` - verify terminal integration. Requires Codex CLI.
   - `Send Code Path Reference to Codex Desktop` - verify macOS app activation and paste. Requires Codex Desktop and Accessibility permission.

## Build Artifact

- Plugin ZIP: `build/distributions/CodePathCopy-{version}.zip`
- It can be installed through IntelliJ IDEA's `Install Plugin from Disk` option.
