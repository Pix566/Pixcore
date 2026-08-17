# 客户端功能

## 战斗反馈

- 客户端采样实体生命变化。
- 在实体颈部显示浮动伤害/治疗数字。
- 可通过 `P` 设置界面开关。

## 怪物血条

- 敌对生物头顶显示名称、像素条和生命数值。
- 隐藏原版名牌。
- 可通过 `P` 设置界面开关。

## 自定义物品图片 / 模型替换

- 服务端下发 `icons.yml` 规则。
- 客户端通过 Mixin 注入 `ItemModelResolver`，动态选择 `pixcore:dynamic` 3D 模型替换物品外观。
- 支持 `material`、`name`、`name-regex`、`lore`、`lore-regex`、`nbt` 匹配。
- 支持 `scale` 与 `handheld`。

## 盔甲外观

- 服务端下发 `armor.yml`。
- 客户端通过 `IClientItemExtensions.getArmorTexture` 替换匹配盔甲的原版贴图。
- 保留原版盔甲模型，只替换纹理。

## 怪物外观

- 服务端下发 `monsters.yml`。
- 客户端按实体类型绘制半透明外观框，模拟 DragonCore 怪物模型功能。

## HUD

- 支持文本和贴图。
- 支持锚点、透明度、缩放、时长。
- 客户端通过 Mixin 监听 `ClientboundTakeItemEntityPacket`，支持物品/箭/经验拾取，显示图标/名称/数量，短时间同物品自动合并，并带淡出动画，不依赖 Pixcore 服务端下发。

## Tooltip

- 服务端下发 `tooltip-text.yml`。
- 支持 `append` / `prepend` / `replace`。
- 支持 `&` 颜色代码。

## 服务端按键

- 服务端下发按键定义后，客户端动态注册 `KeyMapping`。
- 按下/释放会上报服务端。

## 粒子

- 进服后自动触发服务端下发的粒子效果。
- 支持注册表内任意简单粒子 ID。

## 设置界面

按 `P` 打开：

- 战斗伤害数字开关
- 治疗数字开关
- 怪物血条开关
- 拾取 HUD 开关
- 拾取 HUD 右间距 / 下间距
- 拖动模式：按住左键实时调整拾取 HUD 位置，并显示预览框
- 查看已加载规则：打开规则查看器，显示图标/盔甲/Tooltip 规则，支持单条/一键禁用启用与 texture/scale/depth/xyz-scale/handheld/foil 字段编辑
