package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.team.Team;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PermissionsChangedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class TabPrefixManager {

    private static final Map<UUID, Boolean> adminStatus = new HashMap<>();

    @SubscribeEvent
    public static void onPermissionChange(PermissionsChangedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        boolean isAdmin = event.getNewLevel().level()
                .isEqualOrHigherThan(PermissionLevel.ADMINS);

        adminStatus.put(player.getUUID(), isAdmin);

        player.refreshTabListName();
    }

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        event.setDisplayName(createTabName(player));
    }

    private static Component createTabName(ServerPlayer player) {

        if (isAdmin(player)) {
            return Component.empty()
                    .append(Component.literal("[ADMIN] ")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                    .append(player.getName());
        }

        TeamManager teamManager = TeamWar.getTeamManager();
        Team team = teamManager.getTeam(player.getUUID());

        if (team == null) {
            return player.getName();
        }

        ChatFormatting teamColor = getTeamColor(team.getName());

        return Component.empty()
                .append(Component.literal("[" + team.getName().toUpperCase(Locale.ROOT) + "] ")
                        .withStyle(teamColor))
                .append(player.getName());
    }

    private static boolean isAdmin(ServerPlayer player) {
        return adminStatus.computeIfAbsent(
                player.getUUID(),
                _ -> player.level().getServer().getPlayerList().isOp(player.nameAndId())
        );
    }

    private static ChatFormatting getTeamColor(String teamName) {
        return switch (teamName.toUpperCase(Locale.ROOT)) {
            case "BLACK" -> ChatFormatting.BLACK;
            case "DARK_BLUE" -> ChatFormatting.DARK_BLUE;
            case "DARK_GREEN" -> ChatFormatting.DARK_GREEN;
            case "DARK_AQUA" -> ChatFormatting.DARK_AQUA;
            case "DARK_RED" -> ChatFormatting.DARK_RED;
            case "DARK_PURPLE" -> ChatFormatting.DARK_PURPLE;
            case "GOLD" -> ChatFormatting.GOLD;
            case "GRAY" -> ChatFormatting.GRAY;
            case "DARK_GRAY" -> ChatFormatting.DARK_GRAY;
            case "BLUE" -> ChatFormatting.BLUE;
            case "GREEN" -> ChatFormatting.GREEN;
            case "AQUA" -> ChatFormatting.AQUA;
            case "RED" -> ChatFormatting.RED;
            case "LIGHT_PURPLE" -> ChatFormatting.LIGHT_PURPLE;
            case "YELLOW" -> ChatFormatting.YELLOW;
            default -> ChatFormatting.WHITE;
        };
    }

    public static void refresh(ServerPlayer player) {
        player.refreshTabListName();
    }
}