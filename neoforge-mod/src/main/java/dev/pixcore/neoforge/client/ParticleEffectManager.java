package dev.pixcore.neoforge.client;

import dev.pixcore.protocol.Json;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/** Server-pushed particle effects. */
public final class ParticleEffectManager {
    public void spawn(String json) {
        Object parsed = Json.parse(json);
        if (!(parsed instanceof Map<?, ?> map)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        String particleId = str(map.get("particle-id"), "minecraft:end_rod");
        double x = num(map.get("x"), 0.5D);
        double y = num(map.get("y"), 65.0D);
        double z = num(map.get("z"), 0.5D);
        double ox = num(map.get("offset-x"), 0.3D);
        double oy = num(map.get("offset-y"), 0.3D);
        double oz = num(map.get("offset-z"), 0.3D);
        double speed = num(map.get("speed"), 0.05D);
        int count = (int) num(map.get("count"), 40);

        ParticleOptions particle = resolveParticle(particleId);
        for (int i = 0; i < Math.min(count, 128); i++) {
            double vx = (mc.level.getRandom().nextDouble() - 0.5D) * 2.0D * ox;
            double vy = (mc.level.getRandom().nextDouble() - 0.5D) * 2.0D * oy;
            double vz = (mc.level.getRandom().nextDouble() - 0.5D) * 2.0D * oz;
            level.addParticle(particle, x, y, z, vx * speed, vy * speed, vz * speed);
        }
    }

    private ParticleOptions resolveParticle(String id) {
        return switch (id) {
            case "minecraft:flame", "flame" -> ParticleTypes.FLAME;
            case "minecraft:heart", "heart" -> ParticleTypes.HEART;
            case "minecraft:campfire_cosy_smoke", "campfire_cosy_smoke" -> ParticleTypes.CAMPFIRE_COSY_SMOKE;
            case "minecraft:cloud", "cloud" -> ParticleTypes.CLOUD;
            case "minecraft:crit", "crit" -> ParticleTypes.CRIT;
            default -> {
                try {
                    ResourceLocation location = ResourceLocation.parse(id);
                    ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(location);
                    if (type instanceof ParticleOptions options) {
                        yield options;
                    }
                } catch (Exception ignored) {
                    // Fall through to the safe default.
                }
                yield ParticleTypes.END_ROD;
            }
        };
    }

    private static String str(Object o, String def) {
        return o == null ? def : String.valueOf(o);
    }

    private static double num(Object o, double def) {
        if (o instanceof Number n) {
            return n.doubleValue();
        }
        if (o instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }
}
