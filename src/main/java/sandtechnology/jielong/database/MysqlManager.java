package sandtechnology.jielong.database;

import org.bukkit.Bukkit;

import java.sql.DriverManager;
import java.sql.SQLException;

import static sandtechnology.jielong.RedPacketPlugin.config;
import static sandtechnology.jielong.RedPacketPlugin.getInstance;

public class MysqlManager extends AbstractDatabaseManager {

    public MysqlManager(String tableName) {
        setup(tableName);
    }

    @Override
    void setup(String tableName) {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://"
                            + config().getString("Database.IP")
                            + ":"
                            + config().getInt("Database.Port")
                            + "/"
                            + config().getString("Database.DatabaseName")
                            + config().getString("Database.MySQLArgument")
                    , config().getString("Database.UserName")
                    , config().getString("Database.Password")
            );
            executeUpdate(
                    "create table if not exists " + tableName + " (" +
                            "UUID TEXT PRIMARY KEY," +
                            "playerUUID TEXT NOT NULL," +
                            "giveType TEXT NOT NULL," +
                            "RedPacketType TEXT NOT NULL," +
                            "amount INTEGER NOT NULL," +
                            "money DOUBLE NOT NULL," +
                            "moneyMap LongText NOT NULL," +
                            "extraData MEDIUMTEXT NOT NULL," +
                            "givers Text NOT NULL," +
                            "expireTime BIGINT NOT NULL," +
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
