# 安装指南

## 环境要求

| 组件 | 版本 |
|---|---|
| Minecraft | 1.21.8 |
| Java | 21+ |
| NeoForge | 21.8.54 |
| Paper API | 1.21.8-R0.1-SNAPSHOT |

## 服务端安装

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

## 客户端安装

1. 将 `Pixcore-NeoForge-0.1.0.jar` 放入 NeoForge 1.21.8 客户端的 `mods/`。
2. 启动游戏，Mod 会自动创建图片资源目录：

```text
.minecraft/resourcepacks/pixcore/
```

3. 将自定义图片放入该目录，并在服务端 `modules/icons.yml` 中配置 `texture` 路径。

## 验证安装

- 服务端日志出现 `Pixcore enabled. Channel: pixcore:main`。
- 客户端进入服务器后，服务端执行 `/pixcore capabilities <玩家>` 能看到握手成功。
- 客户端按 `P` 能打开 Pixcore HUD 设置界面。
