package sandtechnology.jielong.util;

import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
    private static Map<UUID, List<String>> massageMap = new HashMap<>();
    private static Map<UUID, List<BaseComponent[]>> componentMassageMap = new HashMap<>();
    private static Type massageMapType = new TypeToken<Map<UUID, List<String>>>() {}.getType();
    private static Type componentMassageMapType=new TypeToken<Map<UUID, List<BaseComponent[]>>>() {}.getType();

    private MessageHelper() {
    }

    public static Map<UUID, List<String>> getMassageMap() {
        return massageMap;
    }

    public static void addMassage(UUID uuid, String massage) {
        if (massageMap.containsKey(uuid)) {
            massageMap.get(uuid).add(massage);
        } else {
            massageMap.put(uuid, new ArrayList<>(Collections.singleton(massage)));
        }
    }

    public static void addMassage(UUID uuid, BaseComponent...massage) {
        if (massageMap.containsKey(uuid)) {
            componentMassageMap.get(uuid).add(massage);
        } else {
            componentMassageMap.put(uuid, new ArrayList<>(Collections.singleton(massage)));
        }
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
        try {

            if (status) {
                if (Files.exists(path)) {
                    FromJson(Files.readAllLines(path));
                }
            } else {
                if (!Files.exists(path)) {
                    Files.createFile(path);
                }
                FileWriter fileWriter = new FileWriter(path.toFile(), false);
                fileWriter.write(getJson());
                fileWriter.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException("无法加载/保存将要发送给玩家的消息！");
        }
    }

    public static void FromJson(List<String> json) {
        if(json.size()==2) {
            massageMap.putAll(getGson().fromJson(json.get(0), massageMapType));
            componentMassageMap.putAll(getGson().fromJson(json.get(1), componentMassageMapType));
        }
    }

    public static String getJson() {
        return getGson().toJson(massageMap)+"\n"+getGson().toJson(componentMassageMap);
    }
}
