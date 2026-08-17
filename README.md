# Pixcore.Minecraft

Pixcore.Minecraft 是客户端与 Paper 服务端的一体化增强项目：

- **NeoForge 客户端 Mod**（`neoforge-mod`）：战斗反馈、怪物血条、自定义物品图片/模型、盔甲外观、HUD/粒子等服务端效果。
- **Paper 服务端插件**（`paper-plugin`）：配置下发、规则同步、命令与权限。
- **双端共享协议**（`protocol`）：零依赖的二进制/JSON 协议，NeoForge 与 Paper 共用。

参考 [SoulCore Docs](https://mysoulcutting.github.io/SoulCore-Docs/)，Pixcore 把同样的“客户端渲染 + 服务端规则下发”思路用 **NeoForge + Paper** 实现。

## Wiki

详细文档见 [wiki/](wiki/README.md)：安装、配置、命令、客户端/服务端功能、协议、开发构建与路线图。

仓库已包含 `.github/workflows/wiki-sync.yml`：推送到 `main`/`master` 且修改 `wiki/` 时，会自动同步到 GitHub Wiki；也可手动 `workflow_dispatch` 触发。

同时支持 **MkDocs Material** 本地/静态 Wiki：

```bash
pip install -r requirements-docs.txt
mkdocs serve    # 本地预览 http://127.0.0.1:8000
mkdocs build    # 生成 site/ 静态站点
```

也可直接运行 `serve-docs.bat` / `build-docs.bat`。

## 版本

| 组件 | 版本 |
|---|---|
| Minecraft | 1.21.8 |
| Java | 21+ |
| NeoForge | 21.8.54 |
| Paper API | 1.21.8-R0.1-SNAPSHOT |

## 模块结构

```text
Pixcore/
├── protocol/          # 共享协议（纯 Java，零依赖）
├── paper-plugin/      # Paper 服务端插件
└── neoforge-mod/      # NeoForge 客户端 Mod
```

## 构建

需要 JDK 21 和 Gradle 9.2.1+（仓库已附带 Gradle Wrapper）。

```bash
# 完整构建
./gradlew clean build

# 单独构建
./gradlew :protocol:build
./gradlew :paper-plugin:build
./gradlew :neoforge-mod:build
```

产物：

```text
paper-plugin/build/libs/Pixcore-Plugin-0.1.0.jar
neoforge-mod/build/libs/Pixcore-NeoForge-0.1.0.jar
protocol/build/libs/protocol-0.1.0.jar
```

GitHub Actions 会在 push/PR 时自动构建并上传三个模块的 jar；打 `v*` tag 会自动创建 Release。

## 安装

### 服务端

1. 将 `Pixcore-Plugin-0.1.0.jar` 放入 `plugins/`。
2. 启动 Paper 1.21.8，首次启动会生成：
   ```text
   plugins/Pixcore/config.yml
   plugins/Pixcore/modules/icons.yml
   plugins/Pixcore/modules/hud.yml
   plugins/Pixcore/modules/tooltip-text.yml
   plugins/Pixcore/modules/particles.yml
   plugins/Pixcore/modules/keybinds.yml
   plugins/Pixcore/modules/armor.yml
   ```
3. 修改配置后执行 `/pixcore reload`。

### 客户端

1. 将 `Pixcore-NeoForge-0.1.0.jar` 放入 NeoForge 1.21.8 客户端的 `mods/`。
2. 启动游戏，Mod 会自动创建图片资源目录：
   ```text
   .minecraft/resourcepacks/pixcore/
   ```
3. 将自定义图片放入该目录，并在服务端 `modules/icons.yml` 中配置 `texture` 路径。

## 已实现功能

| 功能 | 位置 | 说明 |
|---|---|---|
| 战斗反馈 | 客户端 | 采样实体生命变化，在实体颈部显示浮动伤害/治疗数字 |
| 怪物血条 | 客户端 | 敌对生物头顶显示名称、像素条与生命数值，隐藏原版名牌 |
| 自定义物品图片 / 模型替换 | 服务端规则 + 客户端渲染 | `icons.yml` 下发匹配规则；客户端通过 Mixin 注入 `ItemModelResolver`，动态选择 `pixcore:dynamic` 3D 模型，不修改物品数据；匹配字段含 material/name/name-regex/lore/lore-regex/nbt，支持 scale/depth/x-scale/y-scale/z-scale、handheld、foil、texture-gui/texture-hand/texture-ground 多材质 |
| 盔甲外观 | 服务端规则 + 客户端渲染 | `armor.yml` 匹配盔甲，通过 `IClientItemExtensions` 替换贴图与模型，支持 inner/outer、按槽位贴图、color 染色、pulse-color 动态脉冲与 model-anim 模型动画，保留原版盔甲模型 |
| HUD 文本/图片 | 服务端下发 | `hud.yml` 支持 text/texture，锚点、透明度、缩放、时长 |
| 粒子效果 | 服务端下发 | `particles.yml` 进服自动触发，支持注册表内任意简单粒子 ID |
| Tooltip 规则 | 服务端下发 + 客户端渲染 | `tooltip-text.yml` 支持 append/prepend/replace、combine、颜色/样式、translate、keybind、component-json、image 图片组件，匹配字段与图标一致，支持 `&` 颜色代码 |
| 服务端按键 | 服务端下发 + 客户端动态注册 | `keybinds.yml` 定义，客户端动态创建 `KeyMapping` 并上报按下/释放 |
| 资源包自动同步 | 服务端下发 + 客户端写入 | 服务端 `resourcepacks/pixcore` 文件夹按分块同步到客户端对应目录；客户端上报本地哈希，服务端只发送缺失/变化文件，并按 tick 限流发送 |
| 拾取 HUD 通知 | 客户端本地检测 | 客户端通过 Mixin 监听 `ClientboundTakeItemEntityPacket`，显示物品/箭/经验拾取，物品图标/名称/数量，短时间同物品自动合并，并带淡出动画，不依赖 Pixcore 服务端下发 |
| HUD 设置界面 | 客户端 | 按 `P` 打开设置，可开关战斗/治疗/怪物血条/拾取 HUD、调整边距、查看/禁用/编辑已加载规则 |
| 能力协商 | 双端 | 客户端握手声明 capabilities，服务端按能力下发 |

## 命令与权限

| 命令 | 权限 | 说明 |
|---|---|---|
| `/pixcore reload` | `pixcore.reload` | 重载服务端配置 |
| `/pixcore hud <玩家> <id>` | `pixcore.hud` | 手动发送 HUD 条目 |
| `/pixcore capabilities <玩家>` | `pixcore.capabilities` | 查看能力协商结果 |
| `/pixcore effect clear <玩家> <id>` | `pixcore.effect.clear` | 清除客户端效果 |

客户端内置默认按键：`G`（发送 `open_menu`）、`P`（打开 Pixcore HUD 设置界面）；服务端 `keybinds.yml` 下发的按键会动态注册到游戏按键设置。

## 协议

通道：
- 服务端 -> 客户端：`pixcore:main`
- 客户端 -> 服务端：`pixcore:main_c2s`

数据包：`Handshake`、`HandshakeAck`、`IconRules`、`Hud`、`TooltipRules`、`Particle`、`KeybindDefinitions`、`ArmorRules`、`KeyEvent`、`EffectClear`、`ResourcePackChunk`、`ResourcePackStatus`。

格式：`[compressionFlag:byte][packetId:byte][body]`；字符串使用 4 字节长度前缀的 UTF-8，JSON 承载复杂规则，大包自动 gzip 压缩。

协议版本：`4`；`Handshake` 携带 `minProtocolVersion` / `maxProtocolVersion` 与模块版本表，`HandshakeAck` 返回服务端模块版本表与特性声明。

## 后续方向

### 近期
- 盔甲外观细化：当前已支持内层/外层贴图、染色、按槽位贴图、pulse-color 动态脉冲与 model-anim 模型动画。
- 物品模型细化：当前已支持 3D 六面盒、轴缩放、foil、手持放大与 GUI/手持/掉落多材质，下一步支持动画与更复杂模型。

### 中期
- Tooltip 规则增强：已完成颜色/样式/combine/translate/keybind/component-json/image 图片组件，下一步支持更复杂的冲突策略。
- 资源包自动同步：已完成分块同步、文件哈希增量、tick 限流与客户端哈希上报断点续传。
- 配置热更新与校验：已完成逐条校验、Tab 补全与 SnakeYAML 精确行号，下一步提供更细字段校验。
- 协议能力协商扩展：已完成 min/max 版本协商、模块版本表与服务端特性声明。

### 远期
- GeckoLib 3D 物品模型：为自定义物品接入动画/3D 模型。
- 跨 Minecraft 版本支持：抽象协议层与版本适配，降低升级成本。
- 客户端内置可视化配置器：已提供规则查看器、单条规则禁用/启用、一键禁用/启用与 texture/scale/depth/xyz-scale/handheld/foil 字段级编辑。
