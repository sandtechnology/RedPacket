package sandtechnology.jielong.util;

import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.bukkit.Bukkit.getServer;
import static sandtechnology.jielong.RedPacketPlugin.getInstance;
import static sandtechnology.jielong.util.JsonHelper.getGson;

public class MessageHelper {
    private static final Map<UUID, List<String>> massageMap = new HashMap<>();
    private static final Map<UUID, List<BaseComponent[]>> componentMassageMap = new HashMap<>();
    private static final Type massageMapType = new TypeToken<Map<UUID, List<String>>>() {}.getType();
    private static final Type componentMassageMapType=new TypeToken<Map<UUID, List<BaseComponent[]>>>() {}.getType();

    private MessageHelper() {
    }

    public static Map<UUID, List<String>> getMassageMap() {
        return massageMap;
    }

    private static void addMassage(UUID uuid, String massage) {
        if (massageMap.containsKey(uuid)) {
            massageMap.get(uuid).add(massage);
        } else {
            massageMap.put(uuid, new ArrayList<>(Collections.singleton(massage)));
        }
    }

    private static void addMassage(UUID uuid, BaseComponent... massage) {
        if (massageMap.containsKey(uuid)) {
            componentMassageMap.get(uuid).add(massage);
        } else {
            componentMassageMap.put(uuid, new ArrayList<>(Collections.singleton(massage)));
        }
    }
    public static void broadcastRedPacket(String title,String subtitle){
        getServer().getOnlinePlayers().forEach(player->{
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,100,1);
            player.sendTitle(title,subtitle,-1,-1,-1);
        });
    }

    public static void broadcastMsg(ChatColor color, String msg) {
        getServer().broadcastMessage(ChatColor.GREEN + "[红包]" + color + msg);
    }
    public static void broadcastMsg(BaseComponent...msg) {
        getServer().spigot().broadcast(new ComponentBuilder(ChatColor.GREEN + "[红包]").append(msg).create());
    }

    public static void sendSimpleMsg(CommandSender sender, ChatColor color, String msg){
        sender.sendMessage(ChatColor.GREEN + "[红包]" + color + msg);
    }

    public static void sendSimpleMsg(CommandSender sender,BaseComponent...msg){
        sender.spigot().sendMessage(new ComponentBuilder(ChatColor.GREEN + "[红包]").append(msg).create());
    }
    public static void sendServiceMsg(OfflinePlayer sender, BaseComponent...msg) {
        if (sender.isOnline()) {
            sendSimpleMsg(sender.getPlayer(),msg);
        } else {
            addMassage(sender.getUniqueId(), msg);
        }
    }

    public static void sendServiceMsg(OfflinePlayer sender, ChatColor color, String msg) {
        if (sender.isOnline()) {
            sendSimpleMsg(sender.getPlayer(),color,msg);
        } else {
            addMassage(sender.getUniqueId(), msg);
        }
    }

    public static void setStatus(boolean status) {
        Path path = getInstance().getDataFolder().toPath().resolve("PlayerData.json");

            if (status) {
                if (Files.exists(path)) {
                    try {
                        FromJson(Files.readAllLines(path));
                    }
                    catch (IOException ex) {
                            throw new RuntimeException("无法加载将要发送给玩家的消息！");
                        }
                }
            } else {
                try (FileWriter fileWriter = new FileWriter(Files.exists(path)? path.toFile():Files.createFile(path).toFile(), false)){
                    fileWriter.write(getJson());
                }catch (IOException e){
                    throw new RuntimeException("无法保存将要发送给玩家的消息！");
                }
            }
        }

    private static void FromJson(List<String> json) {
        if(json.size()==2) {
            massageMap.putAll(getGson().fromJson(json.get(0), massageMapType));
            componentMassageMap.putAll(getGson().fromJson(json.get(1), componentMassageMapType));
        }
    }

    private static String getJson() {
        return getGson().toJson(massageMap)+"\n"+getGson().toJson(componentMassageMap);
    }
}
