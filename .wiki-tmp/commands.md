# 命令与权限

## 命令

| 命令 | 权限 | 说明 |
|---|---|---|
| `/pixcore reload` | `pixcore.reload` | 重载服务端配置并重新下发 |
| `/pixcore hud <玩家> <id>` | `pixcore.hud` | 手动发送 HUD 条目 |
| `/pixcore capabilities <玩家>` | `pixcore.capabilities` | 查看能力协商结果 |
| `/pixcore effect clear <玩家> <id>` | `pixcore.effect.clear` | 清除客户端效果 |

## 权限

| 权限 | 默认 | 说明 |
|---|---|---|
| `pixcore.reload` | op | 重载配置 |
| `pixcore.hud` | op | 手动发送 HUD |
| `pixcore.capabilities` | op | 查看客户端握手信息 |
| `pixcore.effect.clear` | op | 清除效果 |

## 客户端默认按键

| 按键 | 功能 |
|---|---|
| `G` | 发送 `open_menu` 按键事件 |
| `P` | 打开 Pixcore HUD 设置界面 |

服务端下发的动态按键会出现在游戏“按键设置”中。
