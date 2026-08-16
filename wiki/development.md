# 开发与构建

## 环境

- JDK 21
- Gradle 9.2.1+（仓库已附带 Wrapper）

## 构建

```bash
# 完整构建
./gradlew clean build

# 单独构建
./gradlew :protocol:build
./gradlew :paper-plugin:build
./gradlew :neoforge-mod:build
```

## 产物

```text
paper-plugin/build/libs/Pixcore-Plugin-0.1.0.jar
neoforge-mod/build/libs/Pixcore-NeoForge-0.1.0.jar
protocol/build/libs/protocol-0.1.0.jar
```

## 模块结构

```text
Pixcore/
├── protocol/          # 共享协议（纯 Java，零依赖）
├── paper-plugin/      # Paper 服务端插件
└── neoforge-mod/      # NeoForge 客户端 Mod
```

## CI

GitHub Actions：

- `.github/workflows/build.yml`：push/PR 自动构建并上传产物。
- `.github/workflows/release.yml`：打 `v*` tag 自动发布 Release。

## Mixin

- Mixin 配置：`pixcore.mixins.json`
- Mixin 类位于 `dev.pixcore.neoforge.mixin`
- 当前注入：
  - `ItemModelResolverMixin`：动态物品模型选择
- Mixin 注入使用 `remap = false`，适配 NeoForge 官方映射；MixinGradle 插件与 ModDevGradle 不兼容，因此未生成传统 refmap。
- 盔甲贴图替换使用 `IClientItemExtensions.getArmorTexture`，不依赖 Mixin。

## 代码约定

- Java 21。
- 协议模块保持零依赖。
- 客户端渲染逻辑放在 `neoforge-mod` 的 `client` 包。
- 服务端逻辑放在 `paper-plugin` 的 `plugin` 包。
