package com.Discord.DiscordBot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.Color;
import java.util.HashMap;
import java.util.Random;

public class RouletteCommand {

    // Maps userId to chamber array (6 boolean chambers)
    private static final HashMap<String, boolean[]> chambers = new HashMap<>();

    // Maps userId to survive count
    private static final HashMap<String, Integer> surviveCount = new HashMap<>();

    private static final Random random = new Random();

    public static CommandData getCommandData() {
        return Commands.slash("sigma_roulette", "Play Sigma Roulette!");
    }

    // Initialize chambers array with one bullet randomly placed
    private static boolean[] newChamber() {
        boolean[] chamber = new boolean[6];
        int bulletPos = random.nextInt(6);
        chamber[bulletPos] = true;
        return chamber;
    }

    private static boolean[] initialChamber() {
        boolean[] chamber = new boolean[6];
        int bulletPos = random.nextInt(5)+1;
        chamber[bulletPos] = true;
        return chamber;
    }


    // Spin resets chambers with bullet in random spot
    private static void spinChamber(String userId, User user) {
        boolean[] chamber = newChamber();
        chambers.put(userId, chamber);

        // Call the logger here if you want
        //printChamber(user, chamber);
    }

    // Shift chambers: remove chamber[0], shift left, append removed chamber to end
    private static void rotateChamber(String userId) {
        boolean[] chamber = chambers.get(userId);
        if (chamber == null) return;

        boolean first = chamber[0];
        System.arraycopy(chamber, 1, chamber, 0, chamber.length - 1);
        chamber[chamber.length - 1] = first;
    }

    public static void execute(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();

        chambers.put(userId, initialChamber());
        surviveCount.put(userId, 0);  // reset survive count when new game starts

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Sigma Roulette")
                .setDescription(event.getUser().getAsMention() + ", you've loaded the toilet.\n\n**Rules:**\n" +
                        "- If you get a **Chicken Jockey** <:chickenjockey:1391619109308862625> when you flush, you're safe!\n" +
                        "- If you get a **Skibidi Toilet** <:SKIBIDITOILET:1391619088970944522> you lose.\n" +
                        "- You can choose to spin or flush the toilet.\n" +
                        "Good luck!")
                .setColor(Color.ORANGE);

        event.replyEmbeds(embed.build())
                .addActionRow(
                        Button.primary("roulette_flush_" + userId, "Flush"),
                        Button.secondary("roulette_spin_" + userId, "Spin")
                )
                .queue();
    }

    public static void handleButton(ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split("_");

        if (parts.length != 3 || !parts[0].equals("roulette")) return;

        String action = parts[1];
        String userId = parts[2];
        User user = event.getJDA().getUserById(userId);

        if (user == null) {
            event.reply("User not found!").setEphemeral(true).queue();
            return;
        }

        if (!event.getUser().getId().equals(userId)) {
            event.reply("This is not your sigma roulette game nerd!").setEphemeral(true).queue();
            return;
        }

        int currentSurvive = surviveCount.getOrDefault(userId, 0);

        switch (action) {
            case "spin" -> {
                spinChamber(userId, user);//also prints the chamber to me

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("<:chickenjockey:1391619109308862625> Sigma Roulette <:SKIBIDITOILET:1391619088970944522> - Toilet Spun")
                        .setDescription(user.getAsMention() + " spun the toilet. Good luck!")
                        .addField("Times survived", String.valueOf(currentSurvive), false)
                        .setColor(Color.CYAN);

                event.editMessageEmbeds(embed.build())
                        .setActionRow(
                                Button.primary("roulette_flush_" + userId, "Flush"),
                                Button.secondary("roulette_spin_" + userId, "Spin")
                        )
                        .queue();
            }
            case "flush" -> {

                boolean[] chamber = chambers.get(userId);

                if (chamber == null) {
                    event.reply("Game not found! Start a new game with /sigma_roulette.").setEphemeral(true).queue();
                    return;
                }

                boolean bulletAtZero = chamber[0];



                if (bulletAtZero) {
                    // 💥 Bullet fired — you lose
                    chambers.remove(userId);
                    surviveCount.remove(userId);

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("<:SKIBIDITOILET:1391619088970944522> SKIBIDI SIGMA TOILET! You lost!")
                            .setDescription(user.getAsMention() + " flushed the toilet and got flushed down! Game over.")
                            .addField("Total rounds survived", String.valueOf(currentSurvive), false)
                            .setColor(Color.RED);

                    event.editMessageEmbeds(embed.build())
                            .setComponents() //removes buttons after done I hope
                            .queue();
                } else {
                    // ✅ Safe — you didnt get flushed
                    currentSurvive++;
                    surviveCount.put(userId, currentSurvive);
                    rotateChamber(userId);

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("<:chickenjockey:1391619109308862625> CHICKEN JOCKEY!!! You're safe!")
                            .setDescription(user.getAsMention() + " flushed the toilet and and out came a chicken jockey <:chickenjockey:1389470392128639007>. Safe!")
                            .addField("Times survived", String.valueOf(currentSurvive), false)
                            .setColor(Color.GREEN);

                    event.editMessageEmbeds(embed.build())
                            .setActionRow(
                                    Button.primary("roulette_flush_" + userId, "Flush"),
                                    Button.secondary("roulette_spin_" + userId, "Spin")
                            )
                            .queue();
                }
                printChamber(user, chamber);
            }
        }
    }
    //tells position at the end of the round
    public static void printChamber(User user, boolean[] chamber) {
        System.out.print("Current chamber for " + user.getName() + ": [");
        for (int i = 0; i < chamber.length; i++) {
            System.out.print((chamber[i] ? "B" : "E") + (i < chamber.length - 1 ? ", " : ""));
        }
        System.out.println("]");
    }

}
