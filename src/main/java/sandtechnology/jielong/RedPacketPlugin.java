package sandtechnology.jielong;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import sandtechnology.jielong.command.CommandHandler;
import sandtechnology.jielong.database.AbstractDatabaseManager;
import sandtechnology.jielong.database.MysqlManager;
import sandtechnology.jielong.database.SqliteManager;
import sandtechnology.jielong.listener.ChatListener;
import sandtechnology.jielong.util.EcoPerHelper;
import sandtechnology.jielong.util.IdiomManager;
import sandtechnology.jielong.util.MessageHelper;
import sandtechnology.jielong.util.RedPacketManager;

import java.util.logging.Level;

public class RedPacketPlugin extends JavaPlugin {

    private static RedPacketPlugin instance;
    private static AbstractDatabaseManager databaseManager;
    private static boolean startup;

    public static RedPacketPlugin getInstance() {
        return instance;
    }

    public static AbstractDatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static FileConfiguration config() {
        return instance.getConfig();
    }

    public static void log(Level level, String msg, Object... format) {
        getInstance().getLogger().log(level, String.format(msg, format));
    }

    @Override
    public void onEnable() {
        try {
            instance = this;
            saveDefaultConfig();
            getConfig();
            getLogger().info("初始化插件...");
            EcoPerHelper.setup();
            IdiomManager.setup();
            if (config().getString("Database.Type").equalsIgnoreCase("sqlite")) {
                databaseManager = new SqliteManager(config().getString("Database.TableName"));
            } else {
                databaseManager = new MysqlManager(config().getString("Database.TableName"));
            }
            getLogger().info("注册监听器...");
            getServer().getPluginManager().registerEvents(new ChatListener(), this);
            getLogger().info("注册命令...");
            getCommand("RedPacket").setExecutor(CommandHandler.getCommandHandler());
            getCommand("RedPacket").setTabCompleter(CommandHandler.getCommandHandler());
            getLogger().info("注册完成！");
            getLogger().info("正在载入红包信息，请稍等...");
            RedPacketManager.getRedPacketManager().setup();
            MessageHelper.setStatus(true);
            getLogger().info("初始化插件完成！");
            startup = true;
        }catch (RuntimeException e){
            getServer().getPluginManager().disablePlugin(this);
            throw e;
        }
    }

    @Override
    public void onDisable() {
        if(startup) {
            getLogger().info("正在保存红包信息，请稍等...");
            databaseManager.setRunning(false);
            MessageHelper.setStatus(false);
            getLogger().info("完成！继续服务器关闭程序...");
        }
        // Plugin shutdown logic
    }
}
