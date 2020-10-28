package me.border.trialbot.listener;

import me.border.trialbot.module.XPManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class ChatListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent e) {
        Member member = e.getMember();
        if (member == null)
            return;
        if (e.getMember().getUser().isBot())
            return;
        String message = e.getMessage().getContentRaw();
        if (message.startsWith("$"))
            return;
        XPManager.giveXP(e.getChannel(), member);
    }
}
