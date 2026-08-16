# 配置说明

## 主配置 `config.yml`

```yaml
modules:
  item-images: true
  hud: true
  tooltip: true
  particles: true
  keybinds: true
  armor: true
  pickup-hud: true

limits:
  max-icon-rules: 512
  max-hud-entries: 64
  max-tooltip-rules: 64
  max-particle-entries: 64
  max-particle-count: 128
  max-armor-rules: 64
  max-events-per-player-per-second: 40
  max-keybind-definitions: 64
  max-key-events-per-second: 20
  max-resource-pack-chunks-per-tick: 20
```

- `modules.*` 控制是否向客户端下发对应规则。
- `limits.*` 控制规则数量与事件频率上限。

## 物品图片 / 模型 `modules/icons.yml`

```yaml
icons:
  soul_blade:
    priority: 100
    match:
      material: diamond_sword
      name: "Soul Blade"
      lore:
        - "Legendary"
      nbt:
        variant: blue
    texture: weapons/soul_blade.png
    texture-gui: weapons/soul_blade_gui.png
    texture-hand: weapons/soul_blade_hand.png
    texture-ground: weapons/soul_blade_ground.png
    scale: 1.0
    depth: 1.0
    x-scale: 1.0
    y-scale: 1.0
    z-scale: 1.0
    handheld: true
    foil: false
```

匹配字段：

| 字段 | 说明 |
|---|---|
| `material` | 物品 ID，如 `diamond_sword` 或 `minecraft:diamond_sword` |
| `name` | 精确显示名 |
| `name-regex` | 显示名正则匹配 |
| `lore` | Lore 行精确匹配，可填字符串或列表 |
| `lore-regex` | Lore 正则匹配 |
| `nbt` | 自定义数据子标签匹配 |

## 盔甲外观 `modules/armor.yml`

```yaml
魔法盔甲:
  priority: 100
  match:
    name: "魔法盔甲"
    lore:
      - "远古附魔"
    nbt:
      pixcore:
        armor: legendary
  texture: equipment/legendary.png
  inner-texture: equipment/legendary_inner.png
  outer-texture: equipment/legendary_outer.png
  color: "#FFD700"
  scale: 1.0
```

客户端会用 `texture` / `inner-texture` / `outer-texture` 替换原版盔甲贴图，保留原版盔甲模型。

## HUD `modules/hud.yml`

支持 `text` 和 `texture` 两种类型：

```yaml
welcome_message:
  enable: true
  type: text
  text:
    - "欢迎来到 Pixcore 服务器"
  anchor: top-center
  x: 0
  y: 10
  argb: -256
  scale: 1.5
  shadow: true
  duration-ticks: 120
```

```yaml
spawn_banner:
  enable: true
  type: texture
  texture: hud/spawn_banner.png
  anchor: top-right
  x: -10
  y: 10
  width: 64
  height: 32
  alpha: 1.0
  scale: 1.0
  duration-ticks: 200
```

## Tooltip `modules/tooltip-text.yml`

支持 `append` / `prepend` / `replace`，支持 `combine: all|first`，支持颜色与样式：

```yaml
legendary_hint:
  priority: 100
  operation: prepend
  combine: first
  match:
    material: diamond_sword
    lore:
      - "Legendary"
  color: "#FFD700"
  bold: true
  translate: "item.minecraft.diamond_sword"
  component-json:
    - '{"text":" ⚔","color":"red"}'
  lines:
    - '✦ 传说级物品 ✦'
```

`&` 颜色代码会被客户端转换为实际颜色；`color` / `bold` 等样式会应用到未显式指定格式的行；`translate` 会追加翻译组件，`component-json` 会解析为完整 Minecraft 文本组件。

## 粒子 `modules/particles.yml`

```yaml
spawn_sparkle:
  particle-id: minecraft:end_rod
  x: 0.5
  y: 65.0
  z: 0.5
  offset-x: 0.3
  offset-y: 0.3
  offset-z: 0.3
  speed: 0.05
  count: 40
```

`particle-id` 支持注册表内任意简单粒子 ID。

## 按键 `modules/keybinds.yml`

```yaml
open_menu:
  display-name: 打开服务器菜单
  default-key: key.keyboard.g
  category: pixcore:default
```

客户端收到后会动态注册到游戏按键设置。

## 客户端设置

客户端配置文件位于：

```text
config/pixcore-client.properties
```

也可以在游戏内按 `P` 打开设置界面进行修改。
