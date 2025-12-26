package com.runeroller.runeroller;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.runeroller.runeroller.network.OpenMenuS2CPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.neoforged.neoforge.network.PacketDistributor;

public final class RunerollerCommands {

    private RunerollerCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rr")
                        .then(Commands.literal("menu").executes(RunerollerCommands::openMenuIfInVillage))
                        // keep your other subcommands if you want later:
                        .then(Commands.literal("slay").executes(ctx -> simpleMsg(ctx, "Slay clicked!")))
                        .then(Commands.literal("quest").executes(ctx -> simpleMsg(ctx, "Quest clicked!")))
                        .then(Commands.literal("unlocks").executes(ctx -> simpleMsg(ctx, "Unlocks clicked!")))
                        .then(Commands.literal("bank").executes(ctx -> simpleMsg(ctx, "Bank clicked!")))
                        .then(Commands.literal("shop").executes(ctx -> simpleMsg(ctx, "Shop clicked!")))
                        .then(Commands.literal("skill").executes(ctx -> simpleMsg(ctx, "Skill clicked!")))
        );
    }

    private static int openMenuIfInVillage(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();

        // ---- Village structure check ----
        // We locate nearest village structure start within a small radius and require it to be close.
        // radius is in CHUNKS. 8 chunks = 128 blocks-ish searching radius.
        int radiusChunks = 8;

        BlockPos nearest = level.findNearestMapStructure(StructureTags.VILLAGE, pos, radiusChunks, false);

        // If none found nearby => not in/near a village
        if (nearest == null) {
            player.displayClientMessage(
                    Component.literal("You can only open the Runeroller Menu inside a Village!")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return 0;
        }

        // require "inside-ish": distance threshold
        double maxDistBlocks = 120.0; // tweak if you want stricter/looser
        double dist = nearest.distToCenterSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (dist > (maxDistBlocks * maxDistBlocks)) {
            player.displayClientMessage(
                    Component.literal("You must be inside the Village to open the menu.")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return 0;
        }

        // Allowed -> tell client to open GUI
        PacketDistributor.sendToPlayer(player, new OpenMenuS2CPayload());
        return 1;
    }

    private static int simpleMsg(CommandContext<CommandSourceStack> ctx, String msg) {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
            player.displayClientMessage(Component.literal(msg).withStyle(ChatFormatting.GRAY), true);
        }
        return 1;
    }
}
