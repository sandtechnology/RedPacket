package sandtechnology.jielong.util;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import sandtechnology.jielong.redpacket.RedPacket;

import java.util.logging.Level;

import static org.bukkit.Bukkit.getServer;
import static sandtechnology.jielong.RedPacketPlugin.log;

public class EcoAndPermissionHelper {
    static private Economy eco;
    static private Permission per;

    private EcoAndPermissionHelper() {
    }

    public static void setup() {
        log(Level.INFO, "初始化经济与权限支持....");
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            per = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
            if (per != null) {
                log(Level.INFO, "找到权限插件：" + per.getName());
            } else {
                log(Level.SEVERE, "未找到支持Vault的权限插件！将使用原版API！");
            }

            eco = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
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
        return per == null ? sender.hasPermission(perNode) : per.playerHas(sender, perNode);
    }

    public static boolean canSet(Player sender, RedPacket.RedPacketType redPacket) {
        return hasPermission(sender, "redpacket.set." + redPacket.name().toLowerCase());
    }

    public static boolean canGet(Player sender, RedPacket.RedPacketType redPacket) {
        return hasPermission(sender, "redpacket.get." + redPacket.name().toLowerCase());
    }
}
