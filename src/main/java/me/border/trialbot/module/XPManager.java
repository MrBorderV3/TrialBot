package me.border.trialbot.module;

import me.border.trialbot.Main;
import me.border.trialbot.storage.DB;
import me.border.utilities.cache.CachedObject;
import me.border.utilities.utils.AsyncScheduler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class XPManager {

    private static XPCache xpCache = new XPCache();

    private static Set<String> onCooldown = new HashSet<>();

    /**
     * Give xp to a member, if the member has received XP in the last 30 seconds don't do anything
     *
     * @param channel The textchannel the member sent the message in
     * @param member The member to give XP too
     */
    public static void giveXP(TextChannel channel, Member member) {
        String id = member.getId();
        if (onCooldown.contains(id)) {
            return;
        }

        getXP(member).whenComplete((l, t) -> {
            long currentXp = l + 5L;
            if (currentXp % 20 == 0) {
                int level = (int) (currentXp / 20);
                channel.sendMessage("You have successfully leveled up to level " + level).queue();
            }

            onCooldown.add(id);

            saveXP(id, currentXp);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    onCooldown.remove(id);
                }
            }, 30000);
        });
    }

    public static CompletableFuture<Long> getXP(Member member){
        String id = member.getId();
        CompletableFuture<Long> future = new CompletableFuture<>();

        Long cachedXp = (Long) xpCache.getParsedCache(id);
        if (cachedXp != null) {
            future.complete(cachedXp);
            return future;
        } else {
            AsyncScheduler.runTaskAsyncDaemon(() -> {
                try {
                    PreparedStatement ps = Main.getDb().createPreparedStatement("SELECT * FROM xptable WHERE id=?;");
                    ps.setString(1, id);
                    ResultSet rs = Main.getDb().executeQuery(ps);
                    long xp;
                    if (rs.next()) {
                        xp = rs.getLong("xp");
                        future.complete(xp);
                    } else {
                        xp = 0L;
                    }

                    future.complete(xp);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
        }

        return future;
    }

    public static CompletableFuture<Integer> getLevel(Member member){
        CompletableFuture<Integer> future = new CompletableFuture<>();

        CompletableFuture<Long> xp = getXP(member);
        xp.whenComplete((l, t) -> {
            long currentXp = l;
            future.complete((int) (currentXp / 20));
        });

        return future;
    }

    private static void saveXP(String id, Long xp){
        try {
            PreparedStatement ps = Main.getDb().createPreparedStatement("INSERT INTO xptable VALUES (?, ?) ON DUPLICATE KEY UPDATE xp=?;");
            ps.setString(1, id);
            ps.setLong(2, xp);
            ps.setLong(3, xp);
            Main.getDb().executeUpdate(ps);
            xpCache.cache(id, new CachedObject(xp, 60));
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

}
