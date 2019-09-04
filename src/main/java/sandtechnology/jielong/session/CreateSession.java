package sandtechnology.jielong.session;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import sandtechnology.jielong.RedPacketPlugin;
import sandtechnology.jielong.redpacket.RedPacket;
import sandtechnology.jielong.util.EcoPerHelper;
import sandtechnology.jielong.util.OperatorHelper;
import sandtechnology.jielong.util.RedPacketManager;

import static sandtechnology.jielong.RedPacketPlugin.*;
import static sandtechnology.jielong.session.SessionManager.*;
import static sandtechnology.jielong.util.MessageHelper.sendSimpleMsg;

import java.util.UUID;

public class CreateSession {
    public enum State {Init,WaitAmount,WaitMoney,WaitGiveType,WaitType,WaitExtra,WaitGiver,Cancel};
    private RedPacket.Builder builder;
    private UUID playerUUID;
    private volatile long expiredTime;
    private State state;

    CreateSession(Player player){
        playerUUID=player.getUniqueId();
        builder=new RedPacket.Builder().extraData("恭喜发财").player(player);
        expiredTime=System.currentTimeMillis()+ getInstance().getConfig().getLong("RedPacket.SessionExpiredTime");
        state=State.Init;
    }

    public State getState() {
        return state;
    }
    public void cancel(){
        state=State.Cancel;
        getSessionManager().remove(this);
    }
    public RedPacket create(){
        SessionManager.getSessionManager().remove(this);
        RedPacket redPacket=builder.build();
        EcoPerHelper.getEco().withdrawPlayer(redPacket.getPlayer(),redPacket.getMoney());
        RedPacketManager.getRedPacketManager().add(redPacket);
        return redPacket;
    }
    public void parse(Player player, String data){
        try {
            switch (state) {
                case WaitExtra:
                    builder.extraData(data);
                    break;
                case WaitGiver:
                    builder.givers(data);
                    break;
                case WaitMoney:
                    builder.money(OperatorHelper.toTwoPrecision(Double.parseDouble(data)));
                    break;
                case WaitAmount:
                    builder.amount(Integer.parseInt(data));
                    break;
            }
        }catch (NumberFormatException e){
            sendSimpleMsg(player, ChatColor.RED,"格式错误！");
        }
        sendSimpleMsg(player,builder.getInfo());
    }

    private boolean isExpired(){
        if(System.currentTimeMillis()>expiredTime){
            getSessionManager().remove(this);
            return true;
        }else{
            return false;
        }
    }

    public boolean setState(State state) {
        if(!isExpired()&&state!=State.Cancel) {
            this.state = state;
            return true;
        }else {
            return false;
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public RedPacket.Builder getBuilder() {
        return builder;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CreateSession&&((CreateSession) obj).builder.equals(this.builder);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(expiredTime)+builder.hashCode();
    }
}