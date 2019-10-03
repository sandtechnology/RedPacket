package sandtechnology.redpacket.util;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import sandtechnology.redpacket.redpacket.RedPacket;

import java.util.logging.Level;

import static org.bukkit.Bukkit.getServer;
import static sandtechnology.redpacket.RedPacketPlugin.log;
import static sandtechnology.redpacket.util.CommonHelper.checkAndDoSomething;
import static sandtechnology.redpacket.util.CommonHelper.emptyFunction;
import static sandtechnology.redpacket.util.MessageHelper.sendSimpleMsg;

/**
 * 权限+经济的工具类
 */
public class EcoAndPermissionHelper {
    static private Economy eco;
    static private Permission per;

    private EcoAndPermissionHelper() {
    }

    /**
     * 获取目标服务
     *
     * @param service 要获取的服务
     * @param <T>     服务类型
     * @return 服务提供者
     */
    private static <T> T getRegisteredProvider(Class<T> service) {
        RegisteredServiceProvider<T> serviceProvider = getServer().getServicesManager().getRegistration(service);
        //增强版null检查 修复楼层#27的问题
        if (serviceProvider != null && serviceProvider.getProvider() != null) {
            return serviceProvider.getProvider();
        }
        return null;
    }

    public static void setup() {
        log(Level.INFO, "初始化经济与权限支持....");
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            per = getRegisteredProvider(Permission.class);
            if (per != null) {
                log(Level.INFO, "找到权限插件：" + per.getName());
            } else {
                log(Level.SEVERE, "未找到支持Vault的权限插件！将使用原版API！");
            }
            eco = getRegisteredProvider(Economy.class);
            if (eco != null) {
                log(Level.INFO, "找到经济插件：" + eco.getName());
            } else {
                log(Level.SEVERE, "未找到支持Vault的经济插件！");
            }
        } else {
            log(Level.SEVERE, "未找到Vault！此插件将被禁用！");
        }

        if (getServer().getPluginManager().getPlugin("Vault") == null && eco == null) {
            throw new RuntimeException("当前插件运行环境不符合！将禁用本插件！");
        }
    }

    public static Economy getEco() {
        return eco;
    }

    public static boolean hasPermission(Player sender, String perNode) {
        boolean hasPerNode = per == null ? sender.hasPermission(perNode) : per.playerHas(sender, perNode);
        return checkAndDoSomething(hasPerNode, emptyFunction, () -> sendSimpleMsg(sender, ChatColor.RED, "你没有进行此操作的权限！"));
    }

    public static boolean canSet(Player sender, RedPacket.RedPacketType redPacket) {
        return hasPermission(sender, "redpacket.set." + redPacket.name().toLowerCase());
    }

    public static boolean canGet(Player sender, RedPacket.RedPacketType redPacket) {
        return hasPermission(sender, "redpacket.get." + redPacket.name().toLowerCase());
    }
}
