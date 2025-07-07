package com.Discord.DiscordBot.commands;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommandManager extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equalsIgnoreCase("sigma_roulette")) {
            RouletteCommand.execute(event);
        }

    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        //roulette command
        commandData.add(RouletteCommand.getCommandData());

        //updates all commands in guilds(only 1 lol)
        event.getGuild().updateCommands()
                .addCommands(commandData)
                .queue(
                        success -> System.out.println("✅ Commands registered in " + event.getGuild().getName()),
                        error -> System.err.println("❌ Failed in " + event.getGuild().getName() + ": " + error.getMessage())
                );

    }

    //if want to have these commands on other guilds, do onGuildJoin, copy paste everything from onGuildReady here
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        // Add all your slash commands here
        commandData.add(RouletteCommand.getCommandData());

        event.getGuild().updateCommands()
                .addCommands(commandData)
                .queue(
                        success -> System.out.println("✅ Commands registered in new guild: " + event.getGuild().getName()),
                        error -> System.err.println("❌ Failed to register in new guild: " + event.getGuild().getName() + ": " + error.getMessage())
                );
    }
}