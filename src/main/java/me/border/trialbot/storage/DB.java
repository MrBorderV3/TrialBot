package me.border.trialbot.storage;

import me.border.utilities.storage.IMySQLDB;

public class DB extends IMySQLDB {
    public DB(String host, String database, String username, String password, int port) {
        super(host, database, username, password, port);
        System.out.println("Successfully connected to MySQL Database at " + host + "|" + username);
    }

    public void createXPTable(){
        execute("CREATE TABLE IF NOT EXISTS xptable(id VARCHAR(32), xp BIGINT NOT NULL, PRIMARY KEY(id));");
    }

    public void createTicketsTable(){
        execute("CREATE TABLE IF NOT EXISTS tickets(id VARCHAR(8), creator VARCHAR(32), time VARCHAR(32), closed BOOLEAN, closer VARCHAR(32), PRIMARY KEY(id));");
    }
}
