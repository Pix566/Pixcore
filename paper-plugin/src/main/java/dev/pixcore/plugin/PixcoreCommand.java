package dev.pixcore.plugin;

import dev.pixcore.protocol.EffectClearPacket;
import dev.pixcore.protocol.HudPacket;
import dev.pixcore.protocol.Json;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class PixcoreCommand implements CommandExecutor, TabCompleter {
    private final PixcorePlugin plugin;

    public PixcoreCommand(PixcorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("用法: /pixcore <reload|hud|capabilities|effect>");
            return true;
        }
        return switch (args[0].toLowerCase()) {
            case "reload" -> reload(sender);
            case "hud" -> hud(sender, args);
            case "capabilities" -> capabilities(sender, args);
            case "effect" -> effect(sender, args);
            default -> {
                sender.sendMessage("未知子命令: " + args[0]);
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("reload", "hud", "capabilities", "effect"), args[0]);
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "hud", "capabilities", "effect" -> {
                    List<String> players = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        players.add(player.getName());
                    }
                    yield filter(players, args[1]);
                }
                default -> List.of();
            };
        }
        if (args.length == 3 && "hud".equalsIgnoreCase(args[0])) {
            return filter(new ArrayList<>(plugin.getConfigManager().getHudEntries().keySet()), args[2]);
        }
        if (args.length == 3 && "effect".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("clear"), args[2]);
        }
        return List.of();
    }

    private static List<String> filter(List<String> values, String prefix) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(value);
            }
        }
        return result;
    }

    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission("pixcore.reload")) {
            sender.sendMessage("你没有权限执行此命令");
            return true;
        }
        plugin.getConfigManager().reload();
        for (Player online : Bukkit.getOnlinePlayers()) {
            PlayerSession session = plugin.session(online);
            if (session != null) {
                plugin.syncAll(online, session);
                plugin.syncResourcePack(online);
            }
        }
        sender.sendMessage("Pixcore 配置已重载，并已向在线兼容客户端重新下发");
        return true;
    }

    private boolean hud(CommandSender sender, String[] args) {
        if (!sender.hasPermission("pixcore.hud")) {
            sender.sendMessage("你没有权限执行此命令");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("用法: /pixcore hud <玩家> <条目ID>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("找不到玩家: " + args[1]);
            return true;
        }
        Object entry = plugin.getConfigManager().findHudEntry(args[2]);
        if (!(entry instanceof Map<?, ?> map)) {
            sender.sendMessage("找不到 HUD 条目: " + args[2]);
            return true;
        }
        plugin.sendPacket(target, new HudPacket(args[2], Json.write(map)));
        sender.sendMessage("已发送 HUD 条目 " + args[2] + " 给 " + target.getName());
        return true;
    }

    private boolean capabilities(CommandSender sender, String[] args) {
        if (!sender.hasPermission("pixcore.capabilities")) {
            sender.sendMessage("你没有权限执行此命令");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("用法: /pixcore capabilities <玩家>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("找不到玩家: " + args[1]);
            return true;
        }
        PlayerSession session = plugin.session(target);
        sender.sendMessage(session == null
                ? target.getName() + " 未安装 Pixcore 客户端或尚未完成握手"
                : target.getName() + " -> " + session);
        return true;
    }

    private boolean effect(CommandSender sender, String[] args) {
        if (!sender.hasPermission("pixcore.effect.clear")) {
            sender.sendMessage("你没有权限执行此命令");
            return true;
        }
        if (args.length < 4 || !"clear".equalsIgnoreCase(args[1])) {
            sender.sendMessage("用法: /pixcore effect clear <玩家> <effectId>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage("找不到玩家: " + args[2]);
            return true;
        }
        plugin.sendPacket(target, new EffectClearPacket(args[3]));
        sender.sendMessage("已清除 " + target.getName() + " 的 Pixcore 效果 " + args[3]);
        return true;
    }
}
