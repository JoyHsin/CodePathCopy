# Code Path Copy

Code Path Copy is an IntelliJ IDEA plugin that turns the code you selected in the editor into a Codex-friendly file reference:

```text
@/absolute/path/File.java#L1-L10
```

You can copy the reference to your clipboard or send it directly to a dedicated Codex CLI terminal tab.

## Features

- Copy selected code ranges as `@path#Lstart-Lend` references.
- Send references to Codex CLI from the editor context menu.
- Send references to the macOS Codex desktop app by opening the app and pasting from the clipboard.
- Show a small quick-send button after selecting code.
- Configure the Codex command, terminal startup delay, and selection hint behavior per project.
- Hide editor actions unless a file range is selected.

## Requirements

- IntelliJ IDEA 2023.2 or newer.
- Java 17 for development.
- The bundled Terminal plugin enabled.
- Optional: Codex CLI installed and available on your PATH.

## Install From Source

Build the plugin ZIP:

```bash
./gradlew buildPlugin
```

Then install the generated ZIP from:

```text
build/distributions/
```

In IntelliJ IDEA, open `Settings | Plugins | Install Plugin from Disk...` and select the ZIP file.

## Development

Run a sandbox IDE with the plugin installed:

```bash
./gradlew runIde
```

Verify the plugin configuration:

```bash
./gradlew verifyPlugin
```

Build everything:

```bash
./gradlew buildPlugin
```

## Configuration

Open IntelliJ IDEA settings and search for `Code Path Copy`.

- `Codex command`: command sent to the terminal when the Codex tab starts. Default: `codex`.
- `Startup delay`: time to wait before sending the first reference after starting Codex. Default: `1500ms`.
- `Codex Desktop bundle ID`: macOS app bundle ID used for desktop sending. Default: `com.openai.codex`.
- `Desktop paste delay`: time to wait after activating Codex Desktop before pasting. Default: `500ms`.
- `Show quick Codex button`: controls the small button that appears after selecting code.

Sending to Codex Desktop uses macOS AppleScript paste automation. The first time you use it, macOS may ask you to grant IntelliJ IDEA Accessibility permission.

## Usage

1. Select code in an editor.
2. Right-click the selection.
3. Choose `Copy Code Path Reference`, `Send Code Path Reference to Codex`, or `Send Code Path Reference to Codex Desktop`.

The copied or sent value will look like:

```text
@/Users/me/project/src/main/java/App.java#L12-L20
```

## Contributing

Issues and pull requests are welcome. Please run `./gradlew verifyPlugin` before opening a pull request.

## License

This project is licensed under the MIT License.
