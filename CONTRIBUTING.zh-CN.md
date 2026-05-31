# 贡献指南

[English](CONTRIBUTING.md)

感谢你关注 Code Path Copy。

## 本地设置

```bash
./gradlew runIde
```

这会启动一个已安装该插件的 IntelliJ IDEA 沙箱实例。

## 检查

提交 pull request 前，请运行：

```bash
./gradlew verifyPlugin
./gradlew buildPlugin
```

## Pull Request

- 保持改动聚焦，方便审查。
- UI 改动请附上截图或简短录屏。
- 说明手动测试使用的 IntelliJ IDEA 版本。
- 不要提交 IDE 元数据、构建产物或本地插件 ZIP。
