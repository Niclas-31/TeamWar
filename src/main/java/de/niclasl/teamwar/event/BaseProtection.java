package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.team.Team;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import java.util.UUID;

@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.DEDICATED_SERVER)
public class BaseProtection {

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        TeamManager teamManager = TeamWar.getTeamManager();
        Player player = event.getPlayer();
        UUID uuid = player.getUUID();
        Team playerTeam = teamManager.getTeam(uuid);

        if (playerTeam == null) return;

        for (Team team : teamManager.getAllTeams()) {
            if (team.isInBase(event.getPos())) {
                if (team.equals(playerTeam)) return;

                if (event.getState().getBlock() instanceof BedBlock) {
                    return;
                }

                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("You are not allowed to mine anything here!")
                        .withStyle(ChatFormatting.RED));
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        TeamManager teamManager = TeamWar.getTeamManager();

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        UUID uuid = player.getUUID();
        Team playerTeam = teamManager.getTeam(uuid);

        if (playerTeam == null) return;

        for (Team team : teamManager.getAllTeams()) {
            if (team.isInBase(event.getPos())) {
                if (team.equals(playerTeam)) return;
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("You are not allowed to build anything here!")
                        .withStyle(ChatFormatting.RED));
                return;
            }
        }
    }
}