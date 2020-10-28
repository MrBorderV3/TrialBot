package me.border.trialbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.border.trialbot.Main;
import me.border.utilities.utils.AsyncScheduler;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LeaderboardCommand extends Command {

    public LeaderboardCommand(){
        this.name = "leaderboard";
        this.help = "Get the xp leaderboard";
    }

    @Override
    public void execute(CommandEvent e) {
        AsyncScheduler.runTaskAsyncDaemon(() -> {
            try {
                ResultSet rs = Main.getDb().executeQuery("SELECT * FROM xptable ORDER BY xp DESC LIMIT 10;");
                EmbedBuilder builder = new EmbedBuilder();
                StringBuilder description = new StringBuilder();
                int position = 1;
                while (rs.next()) {
                    String name = e.getGuild().retrieveMemberById(rs.getString("id")).complete().getEffectiveName();
                    long xp = rs.getLong("xp");
                    description.append(position).append(". ").append(name).append(" - ").append(xp / 20).append(" - ").append(xp).append("\n");
                    position++;
                }

                builder.setTitle("Leaderboard");
                builder.setColor(Color.YELLOW);
                builder.setDescription(description);

                e.reply(builder.build());
            } catch (SQLException ex){
                ex.printStackTrace();
            }
        });
    }
}
