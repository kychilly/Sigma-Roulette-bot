package com.Discord.DiscordBot.listeners;

import com.Discord.DiscordBot.commands.RouletteCommand;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class RouletteButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        RouletteCommand.handleButton(event);
    }
}