# Changelog

## [Unreleased]

### Added
- 资源包自动同步：服务端 `resourcepacks/pixcore` 分块同步到客户端，按文件哈希只发送变化文件，并按 tick 限流发送。
- Tooltip 规则增强：支持 `combine: all|first`、颜色与粗体/斜体等样式、`translate`、`component-json` 组件。
- 配置校验错误使用 SnakeYAML Mark 精确定位 YAML 行号。
- 物品模型细化：支持 `foil` 强制附魔光泽，`handheld` 在第一人称视角放大显示。
- 协议版本协商：Handshake 携带 min/max 版本，服务端按区间接受，HandshakeAck 返回最终协商版本。
- 服务端规则差异下发：使用 SHA-256 内容哈希，只向玩家发送发生变化的模块。
- ImageCache LRU 缓存与自动淘汰。
- 物品模型 3D 化：动态模型从平面升级为六面盒，支持 depth / x-scale / y-scale / z-scale。
- 盔甲外观：通过 `IClientItemExtensions.getArmorTexture` 替换原版盔甲贴图，支持 inner-texture / outer-texture。
- 配置逐条校验：非 Map 条目或缺少必填字段时跳过并输出定位日志。
- HUD 性能优化：使用 ID 索引 Map 快速移除/更新条目。
- 拾取 HUD 设置界面实时位置预览。
- 新增 `zh_cn.json` 本地化。
- 协议层支持大字符串（UTF-8 长度前缀）与 gzip 压缩。
- 配置超限时输出警告日志。
- `ImageCache` 支持释放动态贴图，离开服务器/规则更新时清理。
- 服务端按键冲突检测与默认键更新。
- 拾取 HUD 拖动模式。
- `/pixcore` Tab 补全。
- `protocol` 模块单元测试。
- 物品模型改为 Mixin 注入 `ItemModelResolver`，不再修改本地物品 `ITEM_MODEL`。
- MkDocs Material 本地 Wiki。
- GitHub Wiki 自动同步工作流。
- FAQ 页面。

### Changed
- 协议版本从 `1` 升至 `3`。
- `/pixcore` 命令注册 TabCompleter。
- `config.yml` 增加 `pickup-hud` 模块开关。
- 移除旧物品装饰层叠加，统一由 Mixin 模型替换渲染，避免双重绘制。
