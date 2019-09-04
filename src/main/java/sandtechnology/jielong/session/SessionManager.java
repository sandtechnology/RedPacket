package sandtechnology.jielong.session;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {


    private static SessionManager sessionManager=new SessionManager();
    private Map<UUID,CreateSession> sessions=new ConcurrentHashMap<>();

    public static SessionManager getSessionManager() {
        return sessionManager;
    }

    public Map<UUID, CreateSession> getSessions() {
        return sessions;
    }

    private CreateSession add(CreateSession createSession){
        sessions.put(createSession.getPlayerUUID(),createSession);
        return createSession;
    }

    public void remove(CreateSession createSession){
        sessions.remove(createSession.getPlayerUUID());
    }
    public void remove(Player player){
        sessions.remove(player.getUniqueId());
    }

    public boolean hasSession(Player player){
        return sessions.containsKey(player.getUniqueId());
    }

    public CreateSession createSession(Player player){
        if(hasSession(player)){
            return getSession(player);
        }else {
            CreateSession session=new CreateSession(player);
            sessions.put(player.getUniqueId(),session);
         return session;
        }
    }
    public CreateSession getSession(Player player){
       return sessions.get(player.getUniqueId());
    }
}
