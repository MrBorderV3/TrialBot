package me.border.trialbot.module;

import me.border.trialbot.Main;
import me.border.utilities.utils.AsyncScheduler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.RandomStringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Collections;

public class Ticket {

    private TextChannel ticketChannel;

    private String id;
    private String creatorId;
    private String timeCreated;
    private boolean closed;
    private String closer = "";

    // Creates a new ticket
    protected Ticket(Member creator){
        this.id = RandomStringUtils.random(6, true, true).toLowerCase();
        while (TicketManager.ticketExists(id)){
            this.id = RandomStringUtils.random(6, true, true).toLowerCase();
        }
        this.creatorId = creator.getId();
        this.timeCreated = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
        this.closed = false;

        this.ticketChannel = TicketManager.getTicketCategory().createTextChannel("ticket-" + id)
                .addPermissionOverride(creator.getGuild().getPublicRole(), null, Arrays.asList(Permission.MESSAGE_READ, Permission.VIEW_CHANNEL))
                .addPermissionOverride(creator, Arrays.asList(Permission.MESSAGE_READ, Permission.VIEW_CHANNEL), null).complete();
        Logs.log("Ticket Created - " + id, "Ticket " + id + " has been created by " + creator.getEffectiveName());
        saveToDB();
    }

    // Creates a ticket object for an existing ticket (ran upon start)
    protected Ticket(TextChannel ticketChannel, String id, String creatorId, String timeCreated, boolean closed, String closer){
        this.ticketChannel = ticketChannel;
        this.id = id;
        this.creatorId = creatorId;
        this.timeCreated = timeCreated;
        this.closed = closed;
        this.closer = closer;
    }

    public void add(Member member){
        if (closed){
            return;
        }
        Logs.log("Member Added To Ticket - " + id, "Member " + member.getEffectiveName() + " has been added from ticket " + id);
        this.ticketChannel.getManager().putPermissionOverride(member, Collections.singletonList(Permission.VIEW_CHANNEL), null).queue();
    }

    public void remove(Member member){
        if (closed){
            return;
        }
        Logs.log("Member Removed From Ticket - " + id, "Member " + member.getEffectiveName() + " has been removed from ticket " + id);
        this.ticketChannel.getManager().putPermissionOverride(member, null, Collections.singletonList(Permission.VIEW_CHANNEL)).queue();
    }

    public void close(Member closer){
        if (closed){
            return;
        }
        Logs.log("Ticket Closed - " + id, "Ticket " + id + " has been closed by " + closer.getEffectiveName());
        this.closer = closer.getId();
        this.closed = true;
        ticketChannel.putPermissionOverride(closer.getGuild().getPublicRole()).setDeny(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ).queue();
        saveToDB();
    }

    private void saveToDB(){
        AsyncScheduler.runTaskAsyncDaemon(() -> {
            PreparedStatement ps = Main.getDb().createPreparedStatement("INSERT INTO tickets VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE closed=?, closer=?;");
            try {
                ps.setString(1, id);
                ps.setString(2, creatorId);
                ps.setString(3, timeCreated);
                ps.setBoolean(4, closed);
                ps.setString(5, closer);
                ps.setBoolean(6, closed);
                ps.setString(7, closer);
            } catch (SQLException e){
                e.printStackTrace();
            }

            Main.getDb().executeUpdate(ps);
        });
    }

    public TextChannel getChannel(){
        return ticketChannel;
    }

    public String getId() {
        return id;
    }

    public boolean isClosed() {
        return closed;
    }
}
