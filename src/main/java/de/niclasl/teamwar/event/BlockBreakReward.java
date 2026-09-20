package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.economy.BlockValue;
import de.niclasl.teamwar.economy.MoneyManager;
import de.niclasl.teamwar.game.GameManager;
import de.niclasl.teamwar.game.GameState;
import de.niclasl.teamwar.team.Team;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.DEDICATED_SERVER)
public class BlockBreakReward {

    public static final Set<BlockPos> brokenBlocks = new HashSet<>();
    public static final Set<BlockPos> placedBlocks = new HashSet<>();

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (GameManager.getGameState() == GameState.RUNNING) {
            placedBlocks.add(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        BlockPos loc = event.getPos();
        BlockState state = player.level().getBlockState(loc);

        if (brokenBlocks.contains(loc)) {
            return;
        }

        if (placedBlocks.contains(loc)) {
            placedBlocks.remove(loc);
            return;
        }

        TeamManager teamManager = TeamWar.getTeamManager();
        MoneyManager moneyManager = TeamWar.getMoneyManager();

        for (Team team : teamManager.getTeams().values()) {
            if (team.isInBase(loc) && !team.getMembers().contains(player.getUUID())) {
                player.sendSystemMessage(Component.literal("You cannot break blocks in another team's base!")
                        .withStyle(ChatFormatting.RED));
                event.setCanceled(true);
                return;
            }
        }

        if (!player.level().getServer().getPlayerList().isOp(player.nameAndId())) {
            if (GameManager.getGameState() == GameState.RUNNING) {
                int reward = BlockValue.getValue(state.getBlock());
                if (reward > 0) {
                    String teamName = teamManager.getTeamOfPlayer(player.getUUID());
                    if (teamName != null) {
                        moneyManager.addTeamMoney(teamName, reward);
                        player.sendSystemMessage(Component.literal(
                                "You have received " + reward + "$ for mining " + state.getBlock() + "!")
                                .withStyle(ChatFormatting.GOLD));
                    }
                }

                brokenBlocks.add(loc);
            }
        }
    }

    public static void resetBlocks() {
        brokenBlocks.clear();
        placedBlocks.clear();
    }
}