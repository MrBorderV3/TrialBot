package me.border.trialbot.module;

import me.border.trialbot.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class Logs {

    private static TextChannel logChannel;

    public static void init() {
        try {
            logChannel = Main.getJda().awaitReady().getTextChannelById(Main.getConfig().getString("LogsChannel"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (logChannel == null){
            throw new NullPointerException("Log channel not found!");
        }
    }


    public static void log(String title, String log){
        if (logChannel == null){
            throw new NullPointerException("Log channel not found!");
        }

        logChannel.sendMessage(getLogEmbed(title, log)).queue();
    }

    private static MessageEmbed getLogEmbed(String title, String body){
        EmbedBuilder builder = new EmbedBuilder();
        // Example: "Log - Ticket Closed - %ticketid%"
        builder.setTitle("Log - " + title);
        builder.setColor(Color.YELLOW);
        builder.setDescription(body);

        return builder.build();
    }
}
