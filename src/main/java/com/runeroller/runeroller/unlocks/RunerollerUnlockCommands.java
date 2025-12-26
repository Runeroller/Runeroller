package com.runeroller.runeroller.unlocks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public final class RunerollerUnlockCommands {

    private RunerollerUnlockCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rr")
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .executes(ctx -> {
                                            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;
                                            String key = StringArgumentType.getString(ctx, "key");

                                            boolean now = !RunerollerUnlocks.isUnlocked(player, key);
                                            RunerollerUnlocks.setUnlocked(player, key, now);

                                            player.displayClientMessage(
                                                    Component.literal("Unlock '" + key + "': ")
                                                            .withStyle(ChatFormatting.GRAY)
                                                            .append(Component.literal(now ? "UNLOCKED" : "LOCKED")
                                                                    .withStyle(now ? ChatFormatting.GREEN : ChatFormatting.RED)),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
        );
    }
}
