package me.border.trialbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.border.trialbot.module.Ticket;
import me.border.trialbot.module.TicketManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class AddCommand extends Command {

    public AddCommand(){
        this.name = "add";
        this.requiredRole = "Ticket Team";
        this.arguments = "@(user)";
        this.help = "Add a member to a ticket";
    }

    @Override
    public void execute(CommandEvent e) {
        try {
            Guild guild = e.getGuild();
            TextChannel textChannel = e.getTextChannel();
            Member memberToAdd = guild.retrieveMember(e.getMessage().getMentionedUsers().get(0)).complete();
            if (!TicketManager.isTicket(textChannel)){
                e.reply("This channel is not a ticket!");
                return;
            }
            if (memberToAdd == null){
                e.reply("Member does not exist or you have failed to mention a member.");
                return;
            }
            if (textChannel.canTalk(memberToAdd)){
                e.reply("Member is already in the ticket.");
                return;
            }

            Ticket ticket = TicketManager.getTicket(textChannel);
            if (ticket.isClosed()) {
                e.reply("This ticket is closed!");
                return;
            }
            ticket.add(memberToAdd);
            e.reply("Successfully added " + memberToAdd.getEffectiveName() + " to the ticket!");
        } catch (IndexOutOfBoundsException ex){
            e.reply("Member does not exist or you have failed to mention a member.");
        }
    }
}
