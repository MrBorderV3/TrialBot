package me.border.trialbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.border.trialbot.module.Ticket;
import me.border.trialbot.module.TicketManager;
import me.border.trialbot.util.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class RemoveCommand extends Command {

    public RemoveCommand(){
        this.name = "remove";
        this.requiredRole = "Ticket Team";
        this.arguments = "@(user)";
        this.help = "Remove a member from a ticket";
    }

    @Override
    public void execute(CommandEvent e) {
        try {
            Guild guild = e.getGuild();
            TextChannel textChannel = e.getTextChannel();
            Member memberToRemove = guild.retrieveMember(e.getMessage().getMentionedUsers().get(0)).complete();
            if (!TicketManager.isTicket(textChannel)){
                e.reply("This channel is not a ticket!");
                return;
            }
            if (memberToRemove == null){
                e.reply("Member does not exist or you have failed to mention a member.");
                return;
            }
            if (Utils.hasRole(memberToRemove, "Support Team")){
                e.reply("Member is not apart of the ticket!");
                return;
            }
            if (!textChannel.canTalk(memberToRemove)){
                e.reply("Member is not in this ticket or does not exist!");
                return;
            }
            Ticket ticket = TicketManager.getTicket(textChannel);
            if (ticket.isClosed()) {
                e.reply("This ticket is closed!");
                return;
            }
            ticket.remove(memberToRemove);
            e.reply("Successfully removed " + memberToRemove.getEffectiveName() + " from the ticket!");
        } catch (ArrayIndexOutOfBoundsException ex){
            e.reply("Member does not exist or you have failed to mention a member.");
        }
    }
}
