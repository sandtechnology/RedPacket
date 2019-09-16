package sandtechnology.jielong.database;

import org.bukkit.entity.Player;
import sandtechnology.jielong.redpacket.RedPacket;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class AbstractDatabaseManager {

    private final Timer timer = new Timer();
    Connection connection;
    String tableName;
    private volatile boolean commiting;
    private volatile boolean running;

    private void sleep() {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException ignored) {
        }
    }

    void executeUpdate(String sql) {
        try {
            while (commiting) {
                //System.out.println("Waiting commit");
                sleep();
            }
            connection.createStatement().executeUpdate(sql);
        } catch (SQLException ex) {
            throw new RuntimeException("SQL语句执行错误！语句：" + sql, ex);
        }
    }

    abstract void setup(String tableName);

    private ResultSet executeQuery(String sql) {
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException ex) {
            throw new RuntimeException("SQL语句执行错误！语句：" + sql, ex);
        }
    }


    void startCommitTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                if (running) {
                    commit();
                   // System.out.println("commit! time cost: " + (System.currentTimeMillis() - time) + " ms");
                } else {
                    timer.cancel();
                }
            }
        }, 0L, 10000L);
    }

    synchronized public void setRunning(boolean running) {
        this.running = running;
        long time = System.currentTimeMillis();
        commit();
        if (!running) {
            close();
        }
        //System.out.println("First/Final commit! time cost: " + (System.currentTimeMillis() - time) + " ms");
    }

    private synchronized void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new RuntimeException("数据库连接关闭失败！", ex);
        }
    }

    private synchronized void commit() {
        try {
            commiting = true;
            connection.commit();
            commiting = false;
        } catch (SQLException ex) {
            throw new RuntimeException("数据库提交更改失败！", ex);
        }
    }

    public void store(RedPacket redPacket) {
        executeUpdate(redPacket.toInsertSQL(tableName));
    }

    public void delete(RedPacket redPacket) {
        executeUpdate("DELETE FROM " + tableName + " Where UUID==" + redPacket.getUUID().toString());
    }


    public void update(RedPacket redPacket) {
        executeUpdate(redPacket.toUpdateSQL(tableName));
    }

    public List<RedPacket> getValid() {
        long time = System.currentTimeMillis();
        ResultSet resultSet = executeQuery("Select * from " + tableName + " where expired==0 and amount!=0");
        //System.out.println("Init Query Time:" + (System.currentTimeMillis() - time) + " ms");
        return RedPacket.fromSQL(resultSet);
    }

    public RedPacket get(Player player) {
        return getAll(player, 1).get(0);
    }

    public List<RedPacket> getAll(Player player, int amount) {
        return getNext(player, amount, 0);
    }

    private List<RedPacket> getNext(Player player, int amount, int offset) {
        long time = System.currentTimeMillis();
        ResultSet resultSet = executeQuery("Select * from " + tableName + " where playerUUID=='" + player.getUniqueId().toString() + "' order by expireTime desc LIMIT " + amount + " OFFSET " + offset);
       // System.out.println("Query Time:" + (System.currentTimeMillis() - time) + " ms");
        return RedPacket.fromSQL(resultSet);
    }


}
