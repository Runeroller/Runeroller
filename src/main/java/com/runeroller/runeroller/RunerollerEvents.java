package com.runeroller.runeroller;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Runeroller.MODID)
public final class RunerollerEvents {

    private static final String WELCOME_SHOWN_KEY = "runeroller_welcome_shown";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return; // server-side only
        }

        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(WELCOME_SHOWN_KEY)) {
            return; // only once per player per world
        }

        // ---- Colored welcome message ----
        Component msg = Component.empty()
                .append(Component.literal("Welcome to ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("Runeroller Modpack").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal(", press ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("K").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(Component.literal(" to start.").withStyle(ChatFormatting.GRAY));

        player.sendSystemMessage(msg);

        // Open Readme popup for first time
        player.connection.send(new com.runeroller.runeroller.network.OpenReadmeS2CPayload(true));

        // ---- Small particle burst around the player ----
        playCompletionEffect(player);

        data.putBoolean(WELCOME_SHOWN_KEY, true);
    }

    public static void playCompletionEffect(ServerPlayer player) {
        if (player.level() instanceof ServerLevel level) {
            double x = player.getX();
            double y = player.getY() + 1.2;
            double z = player.getZ();

            // Colorful burst
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 30, 0.5, 0.5, 0.5, 0.05);
            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 20, 0.4, 0.4, 0.4, 0.02);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 15, 0.3, 0.3, 0.3, 0.1);
            level.sendParticles(ParticleTypes.FIREWORK, x, y, z, 10, 0.2, 0.2, 0.2, 0.05);

            // Sound
            level.playSound(null, x, y, z, net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.75f, 1.0f);
            level.playSound(null, x, y, z, net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.2f);
        }
    }
}
