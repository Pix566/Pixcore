# 服务端功能

## 配置下发

- 服务端将 `icons.yml`、`hud.yml`、`tooltip-text.yml`、`particles.yml`、`keybinds.yml`、`armor.yml` 下发给兼容客户端。
- 客户端握手后，服务端按能力位同步对应模块。

## 能力协商

- 客户端发送 `HandshakePacket`，声明协议版本与 capabilities。
- 服务端返回 `HandshakeAckPacket`。
- `/pixcore capabilities <玩家>` 可查看协商结果。

## 命令

- `/pixcore reload`
- `/pixcore hud <玩家> <id>`
- `/pixcore capabilities <玩家>`
- `/pixcore effect clear <玩家> <id>`

## 资源包自动同步

- 服务端 `plugins/Pixcore/resourcepacks/pixcore/` 下的文件会自动分块同步给兼容客户端。
- 客户端写入 `.minecraft/resourcepacks/pixcore/`，无需手动放置图片。
- 通过 `modules.resource-pack-sync` 开关控制。

## 拾取 HUD

- 服务端监听 `EntityPickupItemEvent`。
- 玩家拾取物品时自动发送 HUD 文本。
- 可通过 `modules.pickup-hud` 开关。

## 限流与安全

- 限制规则数量，防止配置过大。
- 限制单个玩家每秒消息数。
- 限制按键事件频率。
