# Changelog

## [Unreleased]

### Added
- 资源包自动同步：客户端上报本地哈希，服务端只发送缺失/变化文件，支持跨会话断点续传，并按 tick 限流发送。
- Tooltip 规则增强：支持 `combine: all|first`、颜色与粗体/斜体等样式、`translate`、`keybind`、`component-json`、`image` 图片组件。
- 配置校验错误使用 SnakeYAML Mark 精确定位 YAML 行号。
- 物品模型细化：支持 `foil` 强制附魔光泽，`handheld` 在第一人称视角放大显示，新增 `texture-gui` / `texture-hand` / `texture-ground` 多材质。
- 配置校验增强：对 icons/armor/hud/tooltip/particles 的数值字段做类型校验。
- 协议版本协商：Handshake 携带 min/max 版本与模块版本表，HandshakeAck 返回最终协商版本、服务端模块版本表与特性声明。
- 服务端规则差异下发：使用 SHA-256 内容哈希，只向玩家发送发生变化的模块。
- ImageCache LRU 缓存与自动淘汰。
- 物品模型 3D 化：动态模型从平面升级为六面盒，支持 depth / x-scale / y-scale / z-scale。
- 盔甲外观：通过 `IClientItemExtensions` 替换原版盔甲贴图与模型，支持 inner/outer、按槽位贴图、color 染色、pulse-color 动态脉冲与 model-anim 模型动画。
- 配置逐条校验：非 Map 条目或缺少必填字段时跳过并输出定位日志。
- HUD 性能优化：使用 ID 索引 Map 快速移除/更新条目。
- 拾取 HUD 设置界面实时位置预览。
- 客户端规则查看器：在设置界面查看已加载图标/盔甲/Tooltip 规则，支持单条规则禁用/启用、一键禁用/启用与 texture/scale/depth/xyz-scale/handheld/foil 字段级编辑。
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
- 修复规则查看器中 Tooltip 规则点击“编辑”会打开空编辑页的问题，Tooltip 规则不再显示“编辑”按钮。
