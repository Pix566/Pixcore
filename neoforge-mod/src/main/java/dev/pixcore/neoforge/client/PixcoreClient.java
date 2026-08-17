package dev.pixcore.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.pixcore.neoforge.PixcoreMod;
import dev.pixcore.neoforge.network.PixcorePayload;
import dev.pixcore.neoforge.network.PixcoreServerPayload;
import dev.pixcore.protocol.KeyEventPacket;
import dev.pixcore.protocol.PacketCodec;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lwjgl.glfw.GLFW;

/**
 * All Pixcore client-side event wiring. The annotation is client-only and
 * routes mod-bus events to the mod bus and game events to NeoForge.EVENT_BUS
 * automatically.
 */
@EventBusSubscriber(modid = PixcoreMod.MOD_ID, value = Dist.CLIENT)
public final class PixcoreClient {
    private static final KeyMapping OPEN_MENU_KEY = new KeyMapping(
            "key.pixcore.menu", GLFW.GLFW_KEY_G, "key.categories.pixcore");
    private static final KeyMapping OPEN_SETTINGS_KEY = new KeyMapping(
            "key.pixcore.settings", GLFW.GLFW_KEY_P, "key.categories.pixcore");

    private PixcoreClient() {
    }

    // ------------------------------------------------------------------
    // Mod bus events
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PixcoreClientState.INSTANCE.settings.load();
        try {
            java.nio.file.Files.createDirectories(FMLPaths.GAMEDIR.get()
                    .resolve("resourcepacks")
                    .resolve("pixcore"));
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void registerClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(PixcorePayload.TYPE, PixcoreClient::handlePayload);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU_KEY);
        event.register(OPEN_SETTINGS_KEY);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extensions = new PixcoreClientItemExtensions();
        event.registerItem(extensions, BuiltInRegistries.ITEM.stream().toArray(Item[]::new));
    }

    @SubscribeEvent
    public static void registerTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ImageTooltipComponent.class, ClientImageTooltipComponent::new);
    }

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        PixcoreClientState.INSTANCE.imageTooltips.onGatherComponents(event);
    }

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(PixcoreMod.MOD_ID, "dynamic"),
                PixcoreItemModel.Unbaked.MAP_CODEC);
    }

    // ------------------------------------------------------------------
    // Game bus events
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        PixcoreClientState state = PixcoreClientState.INSTANCE;
        state.combatText.tick(mc);
        state.hud.tick();
        state.keybinds.tick(mc);
        state.localPickupHud.tick(mc);

        while (OPEN_MENU_KEY.consumeClick()) {
            sendToServer(new KeyEventPacket("open_menu", 1));
        }
        while (OPEN_SETTINGS_KEY.consumeClick()) {
            mc.setScreen(new PixcoreSettingsScreen());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogIn(ClientPlayerNetworkEvent.LoggingIn event) {
        PixcoreClientState.INSTANCE.onJoin();
    }

    @SubscribeEvent
    public static void onPlayerLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
        PixcoreClientState.INSTANCE.onLeave();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        PixcoreClientState.INSTANCE.hud.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterEntities event) {
        PoseStack poseStack = event.getPoseStack();
        if (poseStack == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
        PixcoreClientState state = PixcoreClientState.INSTANCE;
        state.combatText.render(mc, poseStack, bufferSource, event.getCamera());
        state.monsterHealth.render(mc, poseStack, bufferSource, event.getCamera());
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        PixcoreClientState.INSTANCE.tooltips.onTooltip(event);
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent.CanRender event) {
        Minecraft mc = Minecraft.getInstance();
        if (PixcoreClientState.INSTANCE.settings.monsterHealthEnabled
                && event.getEntity() instanceof Enemy
                && mc.player != null
                && mc.player.distanceToSqr(event.getEntity()) <= 32.0 * 32.0) {
            event.setCanRender(TriState.FALSE);
        }
    }

    // ------------------------------------------------------------------
    // Payload handling
    // ------------------------------------------------------------------

    private static void handlePayload(PixcorePayload payload, IPayloadContext context) {
        try {
            PixcoreClientState.INSTANCE.handlePacket(PacketCodec.decode(payload.data()));
        } catch (Exception e) {
            context.enqueueWork(() -> {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Pixcore 网络数据解析失败: " + e.getMessage()),
                            false);
                }
            });
        }
    }

    private static void sendToServer(dev.pixcore.protocol.Packet packet) {
        try {
            ClientPacketDistributor.sendToServer(new PixcoreServerPayload(PacketCodec.encode(packet)));
        } catch (Exception ignored) {
        }
    }
}
