package sandtechnology.jielong.util;

import org.bukkit.Bukkit;
import sandtechnology.jielong.redpacket.RedPacket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static sandtechnology.jielong.RedPacketPlugin.getDatabaseManager;
import static sandtechnology.jielong.RedPacketPlugin.getInstance;

public class RedPacketManager {

    private static RedPacketManager redPacketManager = new RedPacketManager();
    private List<RedPacket> redPackets = new CopyOnWriteArrayList<>();

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
