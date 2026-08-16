# 网络协议

## 通道

```text
pixcore:main
```

## 格式

```text
[compressionFlag:byte][packetId:byte][body]
```

- `compressionFlag`：`0` 表示未压缩，`1` 表示 body 经过 gzip 压缩。
- 字符串使用 4 字节长度前缀的 UTF-8，不再受 `writeUTF` 的 64 KiB 限制。
- JSON 承载复杂规则，大包自动压缩。

## 数据包

| ID | 数据包 | 方向 | 说明 |
|---|---|---|---|
| 1 | `Handshake` | C -> S | 客户端握手 |
| 2 | `HandshakeAck` | S -> C | 服务端应答 |
| 3 | `IconRules` | S -> C | 物品图片/模型规则 |
| 4 | `Hud` | S -> C | 单条 HUD |
| 5 | `TooltipRules` | S -> C | Tooltip 规则 |
| 6 | `Particle` | S -> C | 粒子效果 |
| 7 | `KeybindDefinitions` | S -> C | 动态按键定义 |
| 8 | `ArmorRules` | S -> C | 盔甲外观规则 |
| 9 | `KeyEvent` | C -> S | 按键按下/释放 |
| 10 | `EffectClear` | S -> C | 清除效果 |
| 11 | `ResourcePackChunk` | S -> C | 资源包文件分块 |
| 12 | `ResourcePackStatus` | C -> S | 客户端本地资源包哈希清单 |

## 能力位

| 位 | 能力 |
|---|---|
| 1 | 战斗反馈 |
| 2 | 怪物血条 |
| 4 | 物品图片 |
| 8 | HUD/粒子/Tooltip 等效果 |
| 16 | 服务端按键 |

协议版本：`4`。

## 版本协商

`HandshakePacket` 携带 `minProtocolVersion`、`maxProtocolVersion` 与模块版本表 JSON。服务端会检查自身协议版本是否落在客户端支持区间内；区间兼容时握手成功，否则拒绝。`HandshakeAckPacket` 会返回最终协议版本、服务端模块版本表和服务端特性声明。
