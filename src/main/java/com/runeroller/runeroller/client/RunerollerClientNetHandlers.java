package com.runeroller.runeroller.client;

import com.runeroller.runeroller.network.BankStateS2CPayload;
import com.runeroller.runeroller.network.OpenMenuS2CPayload;
import com.runeroller.runeroller.network.OpenReadmeS2CPayload;
import com.runeroller.runeroller.network.QuestsSyncPayload;
import com.runeroller.runeroller.network.SkillSyncS2CPayload;
import com.runeroller.runeroller.network.SlayerDataSyncS2CPayload;
import com.runeroller.runeroller.network.UnlocksSyncPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class RunerollerClientNetHandlers {

    private RunerollerClientNetHandlers() {}

    public static void handleOpenMenuClient(OpenMenuS2CPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            mc.setScreen(new RunerollerMenuScreen());
        });
    }

    public static void handleOpenReadmeClient(OpenReadmeS2CPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            mc.setScreen(new RunerollerReadmeScreen(null));
        });
    }

    public static void handleUnlocksSyncClient(UnlocksSyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            RunerollerClientUnlocks.setAll(payload.states());

            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof RunerollerUnlocksScreen s) {
                s.refreshFromClientCache();
            }
        });
    }

    public static void handleBankStateClient(BankStateS2CPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            RunerollerClientBankState.set(payload.slots(), payload.stackLimit());

            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof ShopScreen shop) {
                shop.refreshText();
            }
        });
    }

    public static void handleQuestsSyncClient(QuestsSyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            RunerollerClientQuests.update(payload.progress(), payload.completed());
            
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof RunerollerQuestsScreen s) {
                s.refreshFromClientCache();
            }
        });
    }

    public static void handleSlayerDataSyncClient(SlayerDataSyncS2CPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            RunerollerClientSlayer.update(payload.level(), payload.xp(), payload.activeTaskId(), payload.activeTaskProgress(), payload.activeTaskTarget(), payload.taskOptions(), payload.perks());
            
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof RunerollerSlayScreen s) {
                s.refreshFromClientCache();
            }
        });
    }

    public static void handleSkillSyncClient(SkillSyncS2CPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            RunerollerClientSkills.update(payload.levels(), payload.xp(), payload.perks());

            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof RunerollerSkillScreen s) {
                s.refreshFromClientCache();
            }
        });
    }
}
