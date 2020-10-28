package me.border.trialbot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.border.trialbot.command.*;
import me.border.trialbot.config.Config;
import me.border.trialbot.listener.ChatListener;
import me.border.trialbot.module.Logs;
import me.border.trialbot.module.TicketManager;
import me.border.trialbot.storage.DB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class Main {

    private static Config config = new Config("config", new File("./"));

    private static DB db;

    private static CommandClient commandClient;
    private static JDA jda;

    public static void main(String[] args) {
        setupConfig();
        try {
            startDb();
        } catch (Exception e){
            new NullPointerException("Database credentials are null or incorrect").printStackTrace();
            System.exit(0);
        }
        startCommandClient();
        try {
            startJda();
        } catch (NullPointerException | LoginException e){
            e.printStackTrace();
            System.exit(0);
        }

        try {
            sendTicketEmbed();
        } catch (NullPointerException e){
            throw new NullPointerException("SystemChannel not found!");
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        TicketManager.init();
        Logs.init();
    }

    private static void sendTicketEmbed() throws NullPointerException, InterruptedException {
        TextChannel systemChannel = jda.awaitReady().getTextChannelById(config.getString("SystemChannel"));
        if (systemChannel == null){
            throw new NullPointerException("SystemChannel not found!");
        }
        if (systemChannel.getHistoryFromBeginning(2).complete().isEmpty()){
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.setTitle(config.getString("SystemEmbed.Title"));
            builder.setDescription(config.getString("SystemEmbed.Body"));

            Message message = systemChannel.sendMessage(builder.build()).complete();
            message.addReaction("\uD83D\uDC4D").queue();
        }
    }

    private static void startDb() throws NullPointerException {
        String host = config.getString("Host");
        String database = config.getString("Database");
        String username = config.getString("Username");
        String password = config.getString("Password");
        int port = config.getInt("Port");
        db = new DB(host, database, username, password, port);

        db.createTicketsTable();
        db.createXPTable();
    }

    private static void startCommandClient(){
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setPrefix("$");
        builder.setHelpWord("help");
        builder.setOwnerId("456802337030144011");
        builder.addCommands(new NewCommand(), new AddCommand(), new RemoveCommand(), new CloseCommand(), new XPCommand(), new  LevelCommand(), new LeaderboardCommand());

        commandClient = builder.build();
    }

    private static void startJda() throws NullPointerException, LoginException {
        String token = config.getString("Token");
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.addEventListeners(commandClient);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.addEventListeners(new TicketManager.TicketListener(), new ChatListener());
        Collection<GatewayIntent> intents = new HashSet<>(Arrays.asList(GatewayIntent.values()));
        builder.setEnabledIntents(intents);

        jda = builder.build();
    }

    private static void setupConfig(){
        config.setup();
        if (config.values.isEmpty()){
            config.set("Token", "Enter bot token here");
            config.set("Host", "localhost");
            config.set("Database", "trialbot");
            config.set("Username", "root");
            config.set("Password", "password");
            config.set("Port", 3306);
            config.set("LogsChannel", "Logs channel id");
            config.set("TicketCategory", "Ticket category id");
            config.set("SystemChannel", "System channel id");
            config.set("SystemEmbed.Title", "Ticket Creation");
            config.set("SystemEmbed.Body", "React below to create a ticket!");
            config.set("TicketMessage", "Welcome to your ticket %ping%! The support team will be with you shortly!");
            config.set("TicketEmbed.Title", "Ticket-%id%");
            config.set("TicketEmbed.Body", "While you wait, please write down what you need assistance with.");
        }
    }

    public static Config getConfig() {
        return config;
    }

    public static DB getDb() {
        return db;
    }

    public static CommandClient getCommandClient() {
        return commandClient;
    }

    public static JDA getJda() {
        return jda;
    }
}
