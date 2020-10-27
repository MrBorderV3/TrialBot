package me.border.trialbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.border.trialbot.module.TicketManager;

public class NewCommand extends Command {

    public NewCommand(){
        this.name = "new";
        this.help = "Create a new ticket";
    }

    @Override
    public void execute(CommandEvent e) {
        TicketManager.createTicket(e.getMember());
        e.reply("Successfully created a ticket!");
    }
}
