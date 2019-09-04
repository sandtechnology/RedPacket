package sandtechnology.jielong.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import sandtechnology.jielong.redpacket.RedPacket;
import sandtechnology.jielong.session.CreateSession;

import static sandtechnology.jielong.RedPacketPlugin.*;

import static sandtechnology.jielong.session.SessionManager.getSessionManager;
import static sandtechnology.jielong.util.RedPacketManager.getRedPacketManager;


public class ChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Player player=event.getPlayer();
        //判断是否在输入创建红包的数据
        if(getSessionManager().hasSession(player)&&
                getSessionManager().getSession(player).getState()!= CreateSession.State.WaitType&&
        getSessionManager().getSession(player).getState()!= CreateSession.State.WaitGiveType&&
                getSessionManager().getSession(player).getState()!= CreateSession.State.Cancel&&
        getSessionManager().getSession(player).getState()!=CreateSession.State.Init){
            getSessionManager().getSession(player).parse(event.getPlayer(),event.getMessage());
            event.setCancelled(true);
        }

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
