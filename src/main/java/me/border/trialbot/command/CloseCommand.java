package me.border.trialbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.border.trialbot.module.Ticket;
import me.border.trialbot.module.TicketManager;
import net.dv8tion.jda.api.entities.TextChannel;

public class CloseCommand extends Command {

    public CloseCommand() {
        this.name = "close";
        this.requiredRole = "Ticket Team";
        this.help = "Close a ticket";
    }

    @Override
    public void execute(CommandEvent e) {
        TextChannel textChannel = e.getTextChannel();
        if (!TicketManager.isTicket(textChannel)) {
            e.reply("This channel is not a ticket!");
            return;
        }
        Ticket ticket = TicketManager.getTicket(textChannel);
        if (ticket.isClosed()) {
            e.reply("This ticket is already closed!");
            return;
        }
        ticket.close(e.getMember());
        e.reply("Successfully closed the ticket!");
    }
}
