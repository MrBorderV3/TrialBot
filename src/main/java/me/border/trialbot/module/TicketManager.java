package me.border.trialbot.module;

import me.border.trialbot.Main;
import me.border.utilities.utils.AsyncScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TicketManager {

    private static Map<TextChannel, Ticket> ticketMap = new HashMap<>();

    private static Set<String> idSet = new HashSet<>();

    private static Category ticketCategory;

    public static void init() {
        try {
            ticketCategory = Main.getJda().awaitReady().getCategoryById(Main.getConfig().getString("TicketCategory"));
            loadTickets();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ticketCategory == null) {
            throw new NullPointerException("Ticket category not found!");
        }

    }

    public static void createTicket(Member creator){
        Ticket ticket = new Ticket(creator);
        ticketMap.put(ticket.getChannel(), ticket);
        idSet.add(ticket.getId());

        TextChannel channel = ticket.getChannel();
        channel.sendMessage(Main.getConfig().getString("TicketMessage").replaceAll("%ping%", "<@" + creator.getId() + ">")).queue();
        channel.sendMessage(getWelcomeEmbed(ticket.getId())).queue();
    }

    // Run this async
    private static Ticket getTicketFromDB(TextChannel textChannel, String id) {
        try {
            ResultSet rs = Main.getDb().executeQuery("SELECT * FROM tickets WHERE id='" + id + "';");
            if (rs.next()) {
                String creatorId = rs.getString("creator");
                String timeCreated = rs.getString("time");
                boolean closed = rs.getBoolean("closed");
                String closer = rs.getString("closer");

                idSet.add(id);
                return new Ticket(textChannel, id, creatorId, timeCreated, closed, closer);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return null;
    }

    private static void loadTickets(){
        AsyncScheduler.runTaskAsyncDaemon(() -> {
            for (TextChannel channel : ticketCategory.getTextChannels()){
                if (channel.getId().equals(Main.getConfig().getString("SystemChannel")))
                    continue;
                String id = channel.getName().replace("ticket-", "");
                if (id.length() != 6)
                    continue;
                Ticket ticket = getTicketFromDB(channel, id);
                ticketMap.put(channel, ticket);
            }
        });
    }

    private static MessageEmbed getWelcomeEmbed(String id){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setTitle(Main.getConfig().getString("TicketEmbed.Title").replaceAll("%id%", id));
        builder.setDescription(Main.getConfig().getString("TicketEmbed.Body"));

        return builder.build();
    }

    public static boolean isTicket(TextChannel channel){
        return ticketMap.containsKey(channel);
    }

    protected static boolean ticketExists(String id){
        return idSet.contains(id);
    }

    public static Ticket getTicket(TextChannel channel){
        return ticketMap.get(channel);
    }

    public static Category getTicketCategory(){
        return ticketCategory;
    }

    public static class TicketListener extends ListenerAdapter {

        @Override
        public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent e) {
            TextChannel channel = e.getChannel();
            if (channel.getId().equals(Main.getConfig().getString("SystemChannel"))) {
                User user = e.getUser();
                if (user.isBot())
                    return;
                Member member = e.getMember();
                TicketManager.createTicket(member);
                e.getReaction().removeReaction(user).queue();
            }
        }
    }
}
