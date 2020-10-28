package me.border.trialbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.border.trialbot.module.XPManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;

public class XPCommand extends Command {

    public XPCommand(){
        this.name = "xp";
        this.arguments = "@(user)";
        this.help = "Get the XP of a member";
    }

    @Override
    public void execute(CommandEvent e) {
        Guild guild = e.getGuild();
        try {
            Member pingedMember = guild.retrieveMember(e.getMessage().getMentionedUsers().get(0)).complete();
            if (pingedMember == null) {
                e.reply("Member does not exist or you have failed to mention a member.");
                return;
            }
            CompletableFuture<Long> future = XPManager.getXP(e.getMember());
            future.whenComplete((l, t) -> e.reply(pingedMember.getEffectiveName() + " Has " + l + " XP!"));
        } catch (IndexOutOfBoundsException ex){
            e.reply("Member does not exist or you have failed to mention a member.");
        }
    }
}
