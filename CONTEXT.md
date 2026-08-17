# Pixcore 项目上下文摘要

> 本文件用于快速恢复会话上下文。每次重要变更后请更新。

## 项目定位

Pixcore 是 **NeoForge 客户端 + Paper 服务端** 的 Minecraft 1.21.8 增强项目，类似 SoulCore / DragonCore 思路：服务端下发规则，客户端渲染。

## 模块

- `protocol`：纯 Java 共享协议，零依赖，gzip 压缩，大字符串支持，协议版本 4
- `paper-plugin`：Paper 服务端插件
- `neoforge-mod`：NeoForge 客户端 Mod

## 网络通道

- 服务端 -> 客户端：`pixcore:main`
- 客户端 -> 服务端：`pixcore:main_c2s`
- NeoForge 使用两个独立 payload 类型避免注册冲突

## 已实现功能

- 战斗反馈（本地）
- 怪物血条（本地）
- 物品图片 / 3D 模型替换（Mixin + `ItemModelResolver`）
- 物品模型参数：scale / depth / x-y-z-scale / handheld / foil / 多材质
- 盔甲贴图替换：inner/outer、按槽位、color、pulse、model-anim
- Tooltip：颜色、样式、combine、translate、keybind、component-json、image
- 服务端按键动态注册
- HUD 文本/贴图
- 粒子
- 拾取 HUD（本地，Mixin 监听 `ClientboundTakeItemEntityPacket`，支持物品/箭/经验、合并、淡出）
- 资源包自动同步：分块、哈希、断点续传、tick 限流
- 配置逐条校验 + SnakeYAML 行号 + 数值校验
- 协议版本协商 + 模块版本表 + 服务端特性声明
- 客户端规则查看器/编辑器：禁用、编辑 texture/scale/depth/xyz-scale/handheld/foil
- 怪物外观规则（DragonCore 简化版）：`monsters.yml` + `MonsterRulesPacket` + 半透明外观框

## 当前配置模块

- `modules/icons.yml`
- `modules/hud.yml`
- `modules/tooltip-text.yml`
- `modules/particles.yml`
- `modules/keybinds.yml`
- `modules/armor.yml`
- `modules/monsters.yml`

## 协议数据包 ID

1 Handshake
2 HandshakeAck
3 IconRules
4 Hud
5 TooltipRules
6 Particle
7 KeybindDefinitions
8 ArmorRules
9 KeyEvent
10 EffectClear
11 ResourcePackChunk
12 ResourcePackStatus
13 MonsterRules

## 最近提交

- `7f22273` Add DragonCore-inspired monster appearance rules
- `f35928c` Local pickup HUD: support arrows and experience
- `bd3fc8a` Pickup HUD: item icons, merge counts, fade-out
- `80cd2cc` Replace black fade box with scale fade in pickup HUD（本地已提交，可能未推送）
- `d69543d` Fix client-to-server payload encoding with separate serverbound channel（本地已提交，可能未推送）

## 未推送风险

部分提交可能因 GitHub 网络连接失败而未推送到远程。推送前请检查：

```bash
git status
git log origin/main..HEAD
```

## 构建

```bash
./gradlew clean build -x test
```

> 当前沙箱无法运行 Gradle Test Executor，测试只编译不执行。

## 路线图剩余

- 自定义背包 GUI 界面（DragonCore 风格）
- 怪物模型真正替换（目前是外观框）
- GUI 表达式 / 变量系统
- 实体模型组件显示在 GUI 中
- GeckoLib 3D 动画模型
- 跨版本支持
- 客户端可视化配置器更多字段

## 注意事项

- NeoForge payload 同一 id 不能同时注册 clientbound 和 serverbound，必须分通道
- Mixin 使用 `remap = false` 适配官方映射
- 服务端和客户端 jar 必须同步更新，否则握手失败
