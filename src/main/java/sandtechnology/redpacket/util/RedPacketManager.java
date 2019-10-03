package sandtechnology.redpacket.util;

import org.bukkit.Bukkit;
import sandtechnology.redpacket.redpacket.RedPacket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static sandtechnology.redpacket.RedPacketPlugin.getDatabaseManager;
import static sandtechnology.redpacket.RedPacketPlugin.getInstance;

/**
 * 红包管理
 */
public class RedPacketManager {

    private static final RedPacketManager redPacketManager = new RedPacketManager();
    private final List<RedPacket> redPackets = new CopyOnWriteArrayList<>();

    public static RedPacketManager getRedPacketManager() {
        return redPacketManager;
    }

    public void setup() {
        redPackets.addAll(getDatabaseManager().getValid());
        redPackets.forEach(RedPacket::refundIfExpired);
        Bukkit.getScheduler().runTaskTimerAsynchronously(getInstance(),()->redPackets.forEach(RedPacket::refundIfExpired),200,20000);
    }

    public void add(RedPacket redPacket) {
        getDatabaseManager().store(redPacket);
        redPackets.add(redPacket);
    }

    public void remove(RedPacket redPacket) {
        redPackets.remove(redPacket);
    }

    public List<RedPacket> getRedPackets() {
        return redPackets;
    }

}
