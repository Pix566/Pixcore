# 常见问题 FAQ

## 客户端进服后没有效果？

1. 确认服务端已安装 Pixcore Paper 插件。
2. 确认客户端 `mods/` 里有 Pixcore NeoForge Mod。
3. 在服务端执行：

```text
/pixcore capabilities <玩家>
```

如果显示“未安装 Pixcore 客户端或尚未完成握手”，说明握手失败。

## 自定义图片不显示？

1. 确认图片放在：

```text
.minecraft/resourcepacks/pixcore/
```

2. 确认 `icons.yml` 中 `texture` 路径正确。
3. 确认物品匹配规则能命中该物品。
4. 修改配置后执行 `/pixcore reload`。

## 为什么本地打开 Wiki 后点侧边栏会显示目录/索引？

MkDocs 默认生成目录式 URL。本项目已设置：

```yaml
use_directory_urls: false
```

重新运行 `build-docs.bat` 后，侧边栏链接会直接指向 `xxx.html`，本地双击即可正常浏览。

## 协议版本不匹配？

当前协议版本为 `3`。如果服务端和客户端版本区间不兼容，握手会失败。请确保两边使用兼容版本的 Pixcore。

## 服务端按键没反应？

1. 确认 `keybinds.yml` 已配置。
2. 确认客户端已收到按键定义（可在游戏按键设置中查看）。
3. 如果多个按键使用同一个默认键，客户端会输出冲突警告。

## 配置太多被截断了？

服务端会在日志中输出警告，例如：

```text
Pixcore icons config has 600 entries, but limit is 512; keeping the first 512.
```

可以调大 `config.yml` 中对应的 `limits.*`。
