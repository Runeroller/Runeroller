package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import com.runeroller.runeroller.bank.BankMenu;
import com.runeroller.runeroller.bank.BankSavedData;
import com.runeroller.runeroller.bank.PlayerBank;
import com.runeroller.runeroller.quests.Quest;
import com.runeroller.runeroller.quests.QuestDefinitions;
import com.runeroller.runeroller.quests.QuestSavedData;
import com.runeroller.runeroller.unlocks.RunerollerCoinHelper;
import com.runeroller.runeroller.unlocks.RunerollerUnlockData;
import com.runeroller.runeroller.unlocks.RunerollerUnlockDefinitions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class RunerollerNetwork {

    private RunerollerNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(Runeroller.MODID);

        // ---- MENU OPEN ----
        registrar.playToServer(OpenMenuC2SPayload.TYPE, OpenMenuC2SPayload.STREAM_CODEC, RunerollerNetwork::handleOpenMenuRequest);
        registrar.playToClient(OpenMenuS2CPayload.TYPE, OpenMenuS2CPayload.STREAM_CODEC,
                com.runeroller.runeroller.client.RunerollerClientNetHandlers::handleOpenMenuClient);
        registrar.playToClient(OpenReadmeS2CPayload.TYPE, OpenReadmeS2CPayload.STREAM_CODEC,
                com.runeroller.runeroller.client.RunerollerClientNetHandlers::handleOpenReadmeClient);

        // ---- UNLOCKS ----
        registrar.playToServer(RequestUnlocksC2SPayload.TYPE, RequestUnlocksC2SPayload.STREAM_CODEC, RunerollerNetwork::handleRequestUnlocks);
        registrar.playToServer(ToggleUnlockPayload.TYPE, ToggleUnlockPayload.STREAM_CODEC, RunerollerNetwork::handleToggleUnlock);
        registrar.playToClient(UnlocksSyncPayload.TYPE, UnlocksSyncPayload.STREAM_CODEC,
                com.runeroller.runeroller.client.RunerollerClientNetHandlers::handleUnlocksSyncClient);

        // ---- BANK ----
        registrar.playToServer(OpenBankC2SPayload.TYPE, OpenBankC2SPayload.STREAM_CODEC, RunerollerNetwork::handleOpenBank);
        registrar.playToServer(RequestBankStateC2SPayload.TYPE, RequestBankStateC2SPayload.STREAM_CODEC, RunerollerNetwork::handleRequestBankState);
        registrar.playToClient(BankStateS2CPayload.TYPE, BankStateS2CPayload.STREAM_CODEC,
                com.runeroller.runeroller.client.RunerollerClientNetHandlers::handleBankStateClient);

        registrar.playToServer(BankScrollC2SPayload.TYPE, BankScrollC2SPayload.STREAM_CODEC, RunerollerNetwork::handleBankScroll);
        registrar.playToServer(BankSearchC2SPayload.TYPE, BankSearchC2SPayload.STREAM_CODEC, RunerollerNetwork::handleBankSearch);
        registrar.playToServer(SortBankC2SPayload.TYPE, SortBankC2SPayload.STREAM_CODEC, RunerollerNetwork::handleBankSort);
        registrar.playToServer(BuyBankUpgradeC2SPayload.TYPE, BuyBankUpgradeC2SPayload.STREAM_CODEC, RunerollerNetwork::handleBuyBankUpgrade);

        // ---- QUESTS ----
        registrar.playToServer(RequestQuestsC2SPayload.TYPE, RequestQuestsC2SPayload.STREAM_CODEC, RunerollerNetwork::handleRequestQuests);
        registrar.playToServer(ClaimQuestRewardC2SPayload.TYPE, ClaimQuestRewardC2SPayload.STREAM_CODEC, RunerollerNetwork::handleClaimQuestReward);
        registrar.playToClient(QuestsSyncPayload.TYPE, QuestsSyncPayload.STREAM_CODEC,
                com.runeroller.runeroller.client.RunerollerClientNetHandlers::handleQuestsSyncClient);

        // ---- SLAYER ----
        registrar.playToServer(RequestSlayerDataC2SPayload.TYPE, RequestSlayerDataC2SPayload.STREAM_CODEC, RunerollerNetwork::handleRequestSlayerData);
        registrar.playToServer(SelectSlayTaskC2SPayload.TYPE, SelectSlayTaskC2SPayload.STREAM_CODEC, RunerollerNetwork::handleSelectSlayTask);
        registrar.playToServer(RerollSlayTasksC2SPayload.TYPE, RerollSlayTasksC2SPayload.STREAM_CODEC, RunerollerNetwork::handleRerollSlayTasks);
        registrar.playToServer(BuySlayerPerkC2SPayload.TYPE, BuySlayerPerkC2SPayload.STREAM_CODEC, RunerollerNetwork::handleBuySlayerPerk);
        registrar.playToClient(SlayerDataSyncS2CPayload.TYPE, SlayerDataSyncS2CPayload.STREAM_CODEC,
                com.runeroller.runeroller.client.RunerollerClientNetHandlers::handleSlayerDataSyncClient);

        // ---- SKILLS ----
        registrar.playToServer(RequestSkillsC2SPayload.TYPE, RequestSkillsC2SPayload.STREAM_CODEC, RunerollerNetwork::handleRequestSkills);
        registrar.playToServer(BuySkillPerkC2SPayload.TYPE, BuySkillPerkC2SPayload.STREAM_CODEC, RunerollerNetwork::handleBuySkillPerk);
        registrar.playToClient(SkillSyncS2CPayload.TYPE, SkillSyncS2CPayload.STREAM_CODEC,
                com.runeroller.runeroller.client.RunerollerClientNetHandlers::handleSkillSyncClient);
    }

    // --------------------------
    // Menu
    // --------------------------
    private static void handleOpenMenuRequest(OpenMenuC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            if (!com.runeroller.runeroller.RunerollerLocationHelper.isInsideAllowedArea(sp)) {
                sp.displayClientMessage(Component.literal("You must be in a Village or Minecolony to open the menu!")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }

            sp.connection.send(new OpenMenuS2CPayload());
        });
    }

    // --------------------------
    // Unlocks
    // --------------------------
    private static void handleRequestUnlocks(RequestUnlocksC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            var data = RunerollerUnlockData.get(sp.serverLevel());
            sp.connection.send(new UnlocksSyncPayload(new HashMap<>(data.getPlayerStates(sp.getUUID()))));
        });
    }

    private static void handleToggleUnlock(ToggleUnlockPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            var data = RunerollerUnlockData.get(sp.serverLevel());
            String key = payload.key();

            if (data.isUnlocked(sp.getUUID(), key)) {
                sp.displayClientMessage(Component.literal("Already unlocked."), true);
                sp.connection.send(new UnlocksSyncPayload(new HashMap<>(data.getPlayerStates(sp.getUUID()))));
                return;
            }

            int cost = RunerollerUnlockDefinitions.getCost(key);
            if (cost <= 0) {
                sp.displayClientMessage(Component.literal("Invalid unlock key: " + key), true);
                sp.connection.send(new UnlocksSyncPayload(new HashMap<>(data.getPlayerStates(sp.getUUID()))));
                return;
            }

            if (!RunerollerCoinHelper.removeCoins(sp, cost)) {
                sp.displayClientMessage(Component.literal("Not enough coins! Need " + cost + "."), true);
                sp.connection.send(new UnlocksSyncPayload(new HashMap<>(data.getPlayerStates(sp.getUUID()))));
                return;
            }

            data.setUnlocked(sp.getUUID(), key, true);
            sp.displayClientMessage(Component.literal("Unlocked " + key + " (-" + cost + " coins)"), true);
            sp.connection.send(new UnlocksSyncPayload(new HashMap<>(data.getPlayerStates(sp.getUUID()))));
        });
    }

    // --------------------------
    // Bank
    // --------------------------
    private static void handleOpenBank(OpenBankC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            if (!com.runeroller.runeroller.RunerollerLocationHelper.isInsideAllowedArea(sp)) {
                sp.displayClientMessage(Component.literal("You must be in a Village or Minecolony to use the Bank!")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }

            sp.openMenu(new SimpleMenuProvider(
                    (id, inv, player) -> new BankMenu(id, inv, player),
                    Component.literal("Bank")
            ));
        });
    }

    private static void handleRequestBankState(RequestBankStateC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            BankSavedData data = BankSavedData.get(sp.serverLevel());
            PlayerBank bank = data.getBank(sp.getUUID());

            sp.connection.send(new BankStateS2CPayload(bank.getSlots(), bank.getStackLimit()));
        });
    }

    private static void handleBankScroll(BankScrollC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            if (sp.containerMenu instanceof BankMenu menu) {
                menu.setRowOffsetServer(payload.rowOffset());
            }
        });
    }

    private static void handleBankSearch(BankSearchC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            if (sp.containerMenu instanceof BankMenu menu) {
                menu.setSearchQuery(payload.query());
            }
        });
    }

    private static void handleBankSort(SortBankC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            if (sp.containerMenu instanceof BankMenu menu) {
                menu.sortServer();
            }
        });
    }

    private static void handleBuyBankUpgrade(BuyBankUpgradeC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            BankSavedData data = BankSavedData.get(sp.serverLevel());
            PlayerBank bank = data.getBank(sp.getUUID());

            var up = payload.upgrade();

            if (up == BuyBankUpgradeC2SPayload.Upgrade.SLOTS) {
                int cur = bank.getSlots();
                if (cur >= PlayerBank.MAX_SLOTS) {
                    sp.displayClientMessage(Component.literal("Bank slots already maxed."), true);
                    sp.connection.send(new BankStateS2CPayload(bank.getSlots(), bank.getStackLimit()));
                    return;
                }

                // cost curve: grows with number of upgrades bought
                int slotUpgrades = (cur - 54) / 9;
                int cost = 200 + (slotUpgrades * 150) + (slotUpgrades * slotUpgrades * 25);

                if (!RunerollerCoinHelper.removeCoins(sp, cost)) {
                    sp.displayClientMessage(Component.literal("Not enough coins! Need " + cost + "."), true);
                    sp.connection.send(new BankStateS2CPayload(bank.getSlots(), bank.getStackLimit()));
                    return;
                }

                // +9 slots per upgrade (1 row)
                bank.setSlots(Math.min(PlayerBank.MAX_SLOTS, cur + 9));
                data.markDirtyNow();

                sp.displayClientMessage(Component.literal("Upgraded bank slots! (-" + cost + " coins)"), true);
                sp.connection.send(new BankStateS2CPayload(bank.getSlots(), bank.getStackLimit()));
                return;
            }

            if (up == BuyBankUpgradeC2SPayload.Upgrade.STACK) {
                int cur = bank.getStackLimit();
                int next = cur + 64;

                int stackUpgrades = (cur - 64) / 64;
                int cost = 200 + (stackUpgrades * 150) + (stackUpgrades * stackUpgrades * 25);

                if (!RunerollerCoinHelper.removeCoins(sp, cost)) {
                    sp.displayClientMessage(Component.literal("Not enough coins! Need " + cost + "."), true);
                    sp.connection.send(new BankStateS2CPayload(bank.getSlots(), bank.getStackLimit()));
                    return;
                }

                bank.setStackLimit(next);
                data.markDirtyNow();

                sp.displayClientMessage(Component.literal("Upgraded stack limit! (-" + cost + " coins)"), true);
                sp.connection.send(new BankStateS2CPayload(bank.getSlots(), bank.getStackLimit()));
            }
        });
    }

    // --------------------------
    // Quests
    // --------------------------
    private static void handleRequestQuests(RequestQuestsC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            syncQuests(sp);
        });
    }

    private static void handleClaimQuestReward(ClaimQuestRewardC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            Quest q = QuestDefinitions.get(payload.questId());
            if (q == null) return;

            QuestSavedData data = QuestSavedData.get(sp.serverLevel());
            if (data.isCompleted(sp.getUUID(), q.id())) return;

            // Check if player has items (if it's a collection quest)
            int count = 0;
            for (ItemStack s : sp.getInventory().items) {
                if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, q.target())) {
                    count += s.getCount();
                }
            }
            // Check offhand too
            ItemStack off = sp.getOffhandItem();
            if (!off.isEmpty() && ItemStack.isSameItemSameComponents(off, q.target())) {
                count += off.getCount();
            }

            if (count < q.amount()) {
                sp.displayClientMessage(Component.literal("You don't have enough " + q.target().getHoverName().getString() + "!"), true);
                return;
            }

            // Consume items
            int toRemove = q.amount();
            for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
                ItemStack s = sp.getInventory().getItem(i);
                if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, q.target())) {
                    int take = Math.min(toRemove, s.getCount());
                    s.shrink(take);
                    toRemove -= take;
                    if (toRemove <= 0) break;
                }
            }

            // Give reward
            RunerollerCoinHelper.addCoins(sp, q.rewardCoins());
            data.setCompleted(sp.getUUID(), q.id());

            com.runeroller.runeroller.RunerollerEvents.playCompletionEffect(sp);

            sp.displayClientMessage(Component.literal("Quest Completed: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(q.title()).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" (+" + q.rewardCoins() + " coins)").withStyle(ChatFormatting.AQUA)), false);

            // Also show prominent action bar message
            sp.displayClientMessage(Component.literal("Quest Completed! ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("+" + q.rewardCoins() + " Coins").withStyle(ChatFormatting.GOLD)), true);

            syncQuests(sp);
        });
    }

    private static void syncQuests(ServerPlayer sp) {
        QuestSavedData data = QuestSavedData.get(sp.serverLevel());
        Map<String, Boolean> completed = new HashMap<>();
        Map<String, Integer> progress = new HashMap<>();

        for (Quest q : QuestDefinitions.getAll()) {
            boolean comp = data.isCompleted(sp.getUUID(), q.id());
            completed.put(q.id(), comp);
            // Dynamic progress: how many items they currently have
            int count = 0;
            for (ItemStack s : sp.getInventory().items) {
                if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, q.target())) {
                    count += s.getCount();
                }
            }
            ItemStack off = sp.getOffhandItem();
            if (!off.isEmpty() && ItemStack.isSameItemSameComponents(off, q.target())) {
                count += off.getCount();
            }
            progress.put(q.id(), count);
        }

        sp.connection.send(new QuestsSyncPayload(progress, completed));
    }

    // --------------------------
    // Slayer
    // --------------------------
    private static void handleRequestSlayerData(RequestSlayerDataC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            var saved = com.runeroller.runeroller.slay.SlayerSavedData.get(sp.serverLevel());
            var data = saved.get(sp.getUUID());

            // Generate initial options if empty
            boolean empty = true;
            for (String s : data.taskOptions) { if (s != null) empty = false; }
            if (empty && data.activeTaskId == null) {
                com.runeroller.runeroller.slay.RunerollerSlayEvents.refreshTaskOptions(data);
                saved.setDirty();
            }

            com.runeroller.runeroller.slay.RunerollerSlayEvents.sync(sp, data);
        });
    }

    private static void handleSelectSlayTask(SelectSlayTaskC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            var saved = com.runeroller.runeroller.slay.SlayerSavedData.get(sp.serverLevel());
            var data = saved.get(sp.getUUID());

            if (data.activeTaskId != null) {
                sp.displayClientMessage(Component.literal("You already have an active task!").withStyle(ChatFormatting.RED), true);
                return;
            }

            String taskId = payload.taskId();
            boolean valid = false;
            for (int i = 0; i < 3; i++) {
                if (taskId.equals(data.taskOptions[i])) {
                    valid = true;
                    data.taskOptions[i] = null;
                    break;
                }
            }

            if (valid) {
                var taskDef = com.runeroller.runeroller.slay.SlayTaskDefinitions.get(taskId);
                if (taskDef != null) {
                    data.activeTaskId = taskId;
                    data.activeTaskProgress = 0;
                    data.activeTaskTarget = new java.util.Random().nextInt(taskDef.maxAmount() - taskDef.minAmount() + 1) + taskDef.minAmount();
                    saved.setDirty();
                    sp.displayClientMessage(Component.literal("Task Selected: " + taskDef.name()).withStyle(ChatFormatting.GREEN), true);
                }
            }

            com.runeroller.runeroller.slay.RunerollerSlayEvents.sync(sp, data);
        });
    }

    private static void handleRerollSlayTasks(RerollSlayTasksC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            int cost = 120;
            if (!RunerollerCoinHelper.removeCoins(sp, cost)) {
                sp.displayClientMessage(Component.literal("Not enough coins to reroll! Need " + cost).withStyle(ChatFormatting.RED), true);
                return;
            }

            var saved = com.runeroller.runeroller.slay.SlayerSavedData.get(sp.serverLevel());
            var data = saved.get(sp.getUUID());

            com.runeroller.runeroller.slay.RunerollerSlayEvents.refreshTaskOptions(data);
            saved.setDirty();
            sp.displayClientMessage(Component.literal("Tasks rerolled! (-" + cost + " coins)").withStyle(ChatFormatting.AQUA), true);

            com.runeroller.runeroller.slay.RunerollerSlayEvents.sync(sp, data);
        });
    }

    private static void handleBuySlayerPerk(BuySlayerPerkC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            com.runeroller.runeroller.slay.SlayerPerk perk;
            try {
                perk = com.runeroller.runeroller.slay.SlayerPerk.valueOf(payload.perkName());
            } catch (Exception e) {
                return;
            }

            var saved = com.runeroller.runeroller.slay.SlayerSavedData.get(sp.serverLevel());
            var data = saved.get(sp.getUUID());

            int currentLevel = data.perks.getOrDefault(perk, 0);
            if (currentLevel >= 10) {
                sp.displayClientMessage(Component.literal("Perk already at maximum level!").withStyle(ChatFormatting.RED), true);
                return;
            }

            int nextLevel = currentLevel + 1;
            int reqSlayerLevel = perk.getRequiredSlayerLevel(nextLevel);
            if (data.level < reqSlayerLevel) {
                sp.displayClientMessage(Component.literal("Required Slay Level " + reqSlayerLevel + "!").withStyle(ChatFormatting.RED), true);
                return;
            }

            int cost = perk.getPrice(currentLevel);
            if (!RunerollerCoinHelper.removeCoins(sp, cost)) {
                sp.displayClientMessage(Component.literal("Not enough coins! Need " + cost).withStyle(ChatFormatting.RED), true);
                return;
            }

            data.perks.put(perk, nextLevel);
            saved.setDirty();
            sp.displayClientMessage(Component.literal("Upgraded " + perk.getName() + " to level " + perk.getDisplayLevel(nextLevel) + "!").withStyle(ChatFormatting.GREEN), true);

            com.runeroller.runeroller.slay.RunerollerSlayEvents.sync(sp, data);
        });
    }

    // --------------------------
    // Skills
    // --------------------------
    private static void handleRequestSkills(RequestSkillsC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            com.runeroller.runeroller.skills.SkillEvents.sync(sp);
        });
    }

    private static void handleBuySkillPerk(BuySkillPerkC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            com.runeroller.runeroller.skills.SkillDefinitions.Perk perk;
            try {
                perk = com.runeroller.runeroller.skills.SkillDefinitions.Perk.valueOf(payload.perkName());
            } catch (Exception e) {
                return;
            }

            var saved = com.runeroller.runeroller.skills.SkillSavedData.get(sp.serverLevel());
            var data = saved.get(sp.getUUID());

            int currentTier = data.perks.getOrDefault(perk.name(), 0);
            if (currentTier >= 10) {
                sp.displayClientMessage(Component.literal("Perk already at maximum level!").withStyle(ChatFormatting.RED), true);
                return;
            }

            int nextTier = currentTier + 1;
            int reqLevel = perk.getRequiredSkillLevel(nextTier);
            if (data.levels.getOrDefault(perk.skill, 1) < reqLevel) {
                sp.displayClientMessage(Component.literal("Required " + perk.skill.getDisplayName() + " Level " + reqLevel + "!").withStyle(ChatFormatting.RED), true);
                return;
            }

            int cost = perk.getPrice(currentTier);
            if (!RunerollerCoinHelper.removeCoins(sp, cost)) {
                sp.displayClientMessage(Component.literal("Not enough coins! Need " + cost).withStyle(ChatFormatting.RED), true);
                return;
            }

            data.perks.put(perk.name(), nextTier);
            saved.setDirty();
            sp.displayClientMessage(Component.literal("Upgraded " + perk.name + " to tier " + nextTier + "!").withStyle(ChatFormatting.GREEN), true);

            com.runeroller.runeroller.skills.SkillEvents.sync(sp);
        });
    }
}
