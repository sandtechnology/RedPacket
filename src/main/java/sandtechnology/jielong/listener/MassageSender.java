package sandtechnology.jielong.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import sandtechnology.jielong.util.MessageHelper;

import java.util.List;

import static sandtechnology.jielong.util.MessageHelper.sendServiceMsg;

/**
 * 自动在上线时发送离线时发送的消息
 */
public class MassageSender implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        List<String> msg = MessageHelper.getMassageMap().remove(event.getPlayer().getUniqueId());
        if (msg != null) {
            msg.forEach(m -> sendServiceMsg(event.getPlayer(), ChatColor.GREEN, m));
        }
    }
}
