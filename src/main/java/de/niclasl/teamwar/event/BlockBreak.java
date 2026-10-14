package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.bed.BedManager;
import de.niclasl.teamwar.bed.TeamBed;
import de.niclasl.teamwar.game.GameManager;
import de.niclasl.teamwar.game.GameState;
import de.niclasl.teamwar.team.Team;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.DEDICATED_SERVER)
public class BlockBreak {

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        TeamManager teamManager = TeamWar.getTeamManager();
        BedManager bedManager = TeamWar.getBedManager();

        BlockState state = event.getState();

        if (!(state.getBlock() instanceof BedBlock)) {
            return;
        }

        if (GameManager.getGameState() != GameState.RUNNING) {
            event.setCanceled(true);
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getPlayer();

        if (player.level().getServer().getPlayerList().isOp(player.nameAndId())) {
            event.setCanceled(true);
            return;
        }

        String playerTeam = teamManager.getTeamOfPlayer(player.getUUID());

        BlockPos brokenPos = event.getPos();

        for (Team team : teamManager.getTeams().values()) {

            String teamName = team.getName();

            TeamBed bed = bedManager.getTeamBeds().get(
                    teamName.toLowerCase()
            );

            if (bed == null) {
                continue;
            }

            boolean isFoot =
                    bed.foot.equals(brokenPos)
                            && bed.world.equals(
                            player.level()
                                    .dimension()
                                    .identifier()
                                    .toString()
                    );

            boolean isHead =
                    bed.head.equals(brokenPos)
                            && bed.world.equals(
                            player.level()
                                    .dimension()
                                    .identifier()
                                    .toString()
                    );

            if (!isFoot && !isHead) {
                continue;
            }

            if (playerTeam != null
                    && playerTeam.equalsIgnoreCase(teamName)) {

                event.setCanceled(true);

                player.sendSystemMessage(
                        Component.translatable(
                                "message.team.cant_destroy_bed"
                        ).withStyle(ChatFormatting.RED)
                );

            } else {

                event.setCanceled(false);

                player.level().getServer().getPlayerList().broadcastSystemMessage(
                        Component.translatable("message.team.destroyed_bed", teamName)
                                .withStyle(ChatFormatting.RED),
                        false
                );

                team.destroyBed();

                bedManager.removeRespawnForTeam(teamName);
                bedManager.removeBed(teamName);
            }

            return;
        }
    }
}