package dev.pixcore.neoforge.client;

import dev.pixcore.protocol.ArmorRulesPacket;
import dev.pixcore.protocol.EffectClearPacket;
import dev.pixcore.protocol.HandshakeAckPacket;
import dev.pixcore.protocol.HandshakePacket;
import dev.pixcore.protocol.HudPacket;
import dev.pixcore.protocol.IconRulesPacket;
import dev.pixcore.protocol.KeybindDefinitionsPacket;
import dev.pixcore.protocol.Packet;
import dev.pixcore.protocol.PacketCodec;
import dev.pixcore.protocol.ParticlePacket;
import dev.pixcore.protocol.PixcoreProtocol;
import dev.pixcore.protocol.ResourcePackChunkPacket;
import dev.pixcore.protocol.ResourcePackStatusPacket;
import dev.pixcore.protocol.TooltipRulesPacket;
import dev.pixcore.neoforge.network.PixcoreServerPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/** Client-side singleton holding negotiated state and runtime data. */
public final class PixcoreClientState {
    public static final PixcoreClientState INSTANCE = new PixcoreClientState();

    public final ClientSettings settings = new ClientSettings();
    public final CombatTextManager combatText = new CombatTextManager();
    public final MonsterHealthManager monsterHealth = new MonsterHealthManager();
    public final HudEffectManager hud = new HudEffectManager();
    public final ParticleEffectManager particles = new ParticleEffectManager();
    public final TooltipManager tooltips = new TooltipManager();
    public final ImageTooltipManager imageTooltips = new ImageTooltipManager();
    public final ServerKeybindManager keybinds = new ServerKeybindManager();
    public final ResourcePackManager resourcePack = new ResourcePackManager();
    public final LocalPickupHudManager localPickupHud = new LocalPickupHudManager();

    public int capabilities = PixcoreProtocol.CAP_ALL;
    public int negotiatedProtocolVersion = PixcoreProtocol.VERSION;
    public String serverFeaturesJson = "{}";
    public boolean connected = false;

    private List<IconRule> iconRules = new ArrayList<>();
    private List<IconRule> armorRules = new ArrayList<>();
    private String tooltipRulesJson = "{}";
    private String keybindDefinitionsJson = "{}";

    private PixcoreClientState() {
    }

    public void onJoin() {
        settings.load();
        clearAll();
        capabilities = PixcoreProtocol.CAP_ALL;
        connected = false;
        try {
            ClientPacketDistributor.sendToServer(new PixcoreServerPayload(
                    PacketCodec.encode(new HandshakePacket(PixcoreProtocol.VERSION, capabilities, "NeoForge"))
            ));
        } catch (Exception e) {
            // Network not ready yet; the next login will retry.
        }
    }

    public void onLeave() {
        connected = false;
        clearAll();
    }

    private void clearAll() {
        combatText.clear();
        hud.clearAll();
        tooltips.clearAll();
        keybinds.clearAll();
        resourcePack.clear();
        localPickupHud.clear();
        ImageCache.INSTANCE.clear();
        iconRules = new ArrayList<>();
        armorRules = new ArrayList<>();
        tooltipRulesJson = "{}";
        keybindDefinitionsJson = "{}";
    }

    public void handlePacket(Packet packet) {
        if (packet instanceof HandshakeAckPacket ack) {
            connected = ack.accepted();
            if (ack.accepted()) {
                negotiatedProtocolVersion = ack.protocolVersion();
                serverFeaturesJson = ack.featuresJson();
                sendResourcePackStatus();
            }
        } else if (packet instanceof IconRulesPacket iconRulesPacket) {
            iconRules = IconRule.parseAll(iconRulesPacket.rulesJson());
            ImageCache.INSTANCE.clear();
        } else if (packet instanceof ArmorRulesPacket armorRulesPacket) {
            armorRules = IconRule.parseAll(armorRulesPacket.rulesJson());
        } else if (packet instanceof HudPacket hudPacket) {
            hud.onPacket(hudPacket.id(), hudPacket.json());
        } else if (packet instanceof ParticlePacket particlePacket) {
            particles.spawn(particlePacket.json());
        } else if (packet instanceof TooltipRulesPacket tooltip) {
            tooltipRulesJson = tooltip.rulesJson();
            tooltips.onPacket(tooltip.rulesJson());
        } else if (packet instanceof KeybindDefinitionsPacket keybinds) {
            keybindDefinitionsJson = keybinds.definitionsJson();
            this.keybinds.onPacket(keybinds.definitionsJson());
        } else if (packet instanceof EffectClearPacket clear) {
            hud.clear(clear.effectId());
        } else if (packet instanceof ResourcePackChunkPacket chunk) {
            resourcePack.handle(chunk);
        }
    }

    private void sendResourcePackStatus() {
        try {
            ClientPacketDistributor.sendToServer(new PixcoreServerPayload(PacketCodec.encode(
                    new ResourcePackStatusPacket(resourcePack.manifestJson()))));
        } catch (Exception ignored) {
        }
    }

    public IconRule findIconRule(ItemStack stack) {
        for (IconRule rule : iconRules) {
            if (!isRuleDisabled(rule.id()) && rule.matches(stack)) {
                return rule;
            }
        }
        return null;
    }

    public boolean isRuleDisabled(String id) {
        return settings.disabledRules.contains(id);
    }

    public double scaleFor(IconRule rule) {
        Double override = settings.ruleScaleOverrides.get(rule.id());
        return override != null ? override : rule.scale();
    }

    public String textureFor(IconRule rule, net.minecraft.world.item.ItemDisplayContext context) {
        String override = settings.ruleTextureOverrides.get(rule.id());
        return override != null ? override : rule.textureFor(context);
    }

    public double depthFor(IconRule rule) {
        Double override = settings.ruleDepthOverrides.get(rule.id());
        return override != null ? override : rule.depth();
    }

    public double xScaleFor(IconRule rule) {
        Double override = settings.ruleXScaleOverrides.get(rule.id());
        return override != null ? override : rule.xScale();
    }

    public double yScaleFor(IconRule rule) {
        Double override = settings.ruleYScaleOverrides.get(rule.id());
        return override != null ? override : rule.yScale();
    }

    public double zScaleFor(IconRule rule) {
        Double override = settings.ruleZScaleOverrides.get(rule.id());
        return override != null ? override : rule.zScale();
    }

    public boolean handheldFor(IconRule rule) {
        Boolean override = settings.ruleHandheldOverrides.get(rule.id());
        return override != null ? override : rule.handheld();
    }

    public boolean foilFor(IconRule rule) {
        Boolean override = settings.ruleFoilOverrides.get(rule.id());
        return override != null ? override : rule.foil();
    }

    public List<IconRule> iconRules() {
        return iconRules;
    }

    public List<IconRule> armorRules() {
        return armorRules;
    }

    public IconRule findArmorRule(ItemStack stack) {
        for (IconRule rule : armorRules) {
            if (!isRuleDisabled(rule.id()) && rule.matches(stack)) {
                return rule;
            }
        }
        return null;
    }

    public String tooltipRulesJson() {
        return tooltipRulesJson;
    }

    public String keybindDefinitionsJson() {
        return keybindDefinitionsJson;
    }
}
