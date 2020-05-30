package sandtechnology.redpacket.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static sandtechnology.redpacket.RedPacketPlugin.config;

public class MysqlManager extends AbstractDatabaseManager {

    public MysqlManager(String tableName) {
        setup(tableName);
    }

    @Override
    public Connection getConnection() {
        String argument = config().getString("Database.MySQLArgument");
        try {
            if (connection == null || !connection.isValid(800)) {
                connection = DriverManager.getConnection(
                        "jdbc:mysql://"
                                + config().getString("Database.IP")
                                + ":"
                                + config().getInt("Database.Port")
                                + "/"
                                + config().getString("Database.DatabaseName")
                                + (argument.equals("null") ? "" : argument)
                        , config().getString("Database.UserName")
                        , config().getString("Database.Password")
                );
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库连接错误", e);
        }
        return connection;
    }

    @Override
    void setup(String tableName) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.tableName = tableName;
            //https://techjourney.net/mysql-error-1170-42000-blobtext-column-used-in-key-specification-without-a-key-length/
            executeUpdate(
                    "create table if not exists " + tableName + " (" +
                            "UUID CHAR(128) PRIMARY KEY," +
                            "playerUUID CHAR(128) NOT NULL," +
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
            setRunning(true);
            startCommitTimer();
        } catch (Exception ex) {
            throw new RuntimeException("数据库初始化出现错误，将关闭本插件！", ex);

        }
    }

}
