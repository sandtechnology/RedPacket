package sandtechnology.redpacket.database;

import java.sql.DriverManager;
import java.sql.SQLException;

import static sandtechnology.redpacket.RedPacketPlugin.config;
import static sandtechnology.redpacket.RedPacketPlugin.getInstance;

public class SqliteManager extends AbstractDatabaseManager {


    public SqliteManager(String tableName) {
        setup(tableName);
    }

    @Override
    void setup(String tableName) {
        try{
            this.tableName = tableName;
            connection = DriverManager.getConnection("jdbc:sqlite:" + getInstance().getDataFolder().toPath().resolve(config().getString("Database.FileName")).toString());
            executeUpdate(
                    "create table if not exists " + tableName + " (" +
                            "UUID Text PRIMARY KEY," +
                            "playerUUID Text NOT NULL," +
                            "giveType Text NOT NULL," +
                            "RedPacketType Text NOT NULL," +
                            "amount INTEGER NOT NULL," +
                            "money real NOT NULL," +
                            "moneyMap Text NOT NULL," +
                            "extraData Text NOT NULL," +
                            "givers Text NOT NULL," +
                            "expireTime INTEGER NOT NULL," +
                            "timeZone TEXT NOT NULL,"+
                            "expired INTEGER NOT NULL)"
            );
            executeUpdate("CREATE INDEX if not exists searchIndex ON " + tableName + " (playerUUID, expireTime)");
            connection.setAutoCommit(false);
            setRunning(true);
            startCommitTimer();
        } catch (SQLException ex) {
            throw new RuntimeException("数据库初始化出现错误，将关闭本插件！", ex);
        }
    }
}
