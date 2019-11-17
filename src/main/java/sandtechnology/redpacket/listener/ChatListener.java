package sandtechnology.redpacket.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import sandtechnology.redpacket.redpacket.RedPacket;
import sandtechnology.redpacket.session.CreateSession;

import java.util.Arrays;

import static sandtechnology.redpacket.RedPacketPlugin.getInstance;
import static sandtechnology.redpacket.session.SessionManager.getSessionManager;
import static sandtechnology.redpacket.util.RedPacketManager.getRedPacketManager;


public class ChatListener implements Listener {

    private static final CreateSession.State[] inputNeededState = {CreateSession.State.WaitAmount, CreateSession.State.WaitExtra, CreateSession.State.WaitGiver, CreateSession.State.WaitMoney};
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player=event.getPlayer();
        //判断是否在输入创建红包的数据
        if (getSessionManager().hasSession(player) && Arrays.stream(inputNeededState).anyMatch(state -> state == getSessionManager().getSession(player).getState())) {
            getSessionManager().getSession(player).parse(event.getPlayer(),event.getMessage());
            event.setCancelled(true);
        }
        //确保异步执行
        if(event.isAsynchronous()){
           checkRedPacket(event);
        }else {
            Bukkit.getScheduler().runTaskAsynchronously(getInstance(),()->checkRedPacket(event));
        }
    }

    private void checkRedPacket(AsyncPlayerChatEvent event){
        getRedPacketManager().getRedPackets().stream().filter(redPacket -> redPacket.getType().equals(RedPacket.RedPacketType.JieLongRedPacket) || redPacket.getType().equals(RedPacket.RedPacketType.PasswordRedPacket)).forEach(redPacket -> redPacket.giveIfValid(event.getPlayer(), event.getMessage()));
    }

}
