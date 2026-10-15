package de.niclasl.teamwar.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class TeamColorsCommand {

    private static final ChatFormatting[] COLORS = {
            ChatFormatting.RED,
            ChatFormatting.BLUE,
            ChatFormatting.GREEN,
            ChatFormatting.YELLOW,
            ChatFormatting.GRAY,
            ChatFormatting.AQUA,
            ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.BLACK,
            ChatFormatting.WHITE,
            ChatFormatting.DARK_GRAY,
            ChatFormatting.DARK_BLUE,
            ChatFormatting.DARK_GREEN,
            ChatFormatting.DARK_AQUA,
            ChatFormatting.DARK_RED,
            ChatFormatting.DARK_PURPLE,
            ChatFormatting.GOLD
    };

    private static final String[] COLOR_NAMES = {
            "Red",
            "Blue",
            "Green",
            "Yellow",
            "Gray",
            "Aqua",
            "Light Purple",
            "Black",
            "White",
            "Dark Gray",
            "Dark Blue",
            "Dark Green",
            "Dark Aqua",
            "Dark Red",
            "Dark Purple",
            "Gold"
    };

    private static final int[] PAGE_SIZES = {
            5,
            5,
            6
    };

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("team-colors")
                        .requires(source ->
                                source.getEntity() instanceof ServerPlayer
                        )
                        .executes(context -> {

                            ServerPlayer player =
                                    context.getSource().getPlayerOrException();

                            showPage(player, 1);

                            return 1;
                        })
                        .then(Commands.argument("page", IntegerArgumentType.integer(1, 3))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();

                                            int page = IntegerArgumentType.getInteger(context, "page");

                                            showPage(player, page);

                                            return 1;
                                        })
                        )
        );
    }

    private static void showPage(ServerPlayer player, int page) {
        if (page < 1 || page > 3) {
            player.sendSystemMessage(Component.translatable("message.command.team_colors.invalid_page")
                    .withStyle(ChatFormatting.RED));

            return;
        }

        int startIndex = 0;

        for (int i = 0; i < page - 1; i++) {
            startIndex += PAGE_SIZES[i];
        }

        int endIndex = Math.min(startIndex + PAGE_SIZES[page - 1], COLORS.length);

        player.sendSystemMessage(Component.translatable(
                "message.command.team_colors.page_from_page", page)
                .withStyle(ChatFormatting.BOLD));

        for (int i = startIndex; i < endIndex; i++) {

            ChatFormatting color = COLORS[i];

            player.sendSystemMessage(Component.literal(COLOR_NAMES[i])
                    .withStyle(style -> style.withColor(color))
            );
        }

        Component navigation = Component.empty();

        if (page > 1) {

            Component previous = Component.translatable("message.command.team_colors.previous_page")
                    .withStyle(style ->
                            style
                                    .withColor(ChatFormatting.GOLD)
                                    .withClickEvent(new ClickEvent.RunCommand(
                                            "/team-colors " + (page - 1)
                                    ))
            );

            navigation = navigation.copy().append(previous);
        }

        if (page < 3) {

            Component next = Component.translatable("message.command.team_colors.next_page")
                    .withStyle(style ->
                            style
                                    .withColor(ChatFormatting.GOLD)
                                    .withClickEvent(new ClickEvent.RunCommand(
                                            "/team-colors " + (page + 1)
                                    ))
            );

            navigation = navigation.copy().append(next);
        }

        player.sendSystemMessage(navigation);
    }
}