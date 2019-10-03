package sandtechnology.redpacket.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import sandtechnology.redpacket.util.MessageHelper;

import java.util.List;

import static sandtechnology.redpacket.util.MessageHelper.sendServiceMsg;

/**
 * 自动在上线时发送离线时发送的消息
 * @see MessageHelper
 */
public class MessageSender implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        List<String> msg = MessageHelper.getMassageMap().remove(event.getPlayer().getUniqueId());
        if (msg != null) {
            msg.forEach(m -> sendServiceMsg(event.getPlayer(), ChatColor.GREEN, m));
        }
    }
}
