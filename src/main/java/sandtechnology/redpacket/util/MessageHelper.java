package sandtechnology.redpacket.util;

import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;
import static sandtechnology.redpacket.RedPacketPlugin.getInstance;
import static sandtechnology.redpacket.util.JsonHelper.getGson;

public class MessageHelper {
    private static final Map<UUID, List<String>> massageMap = new HashMap<>();
    private static final Map<UUID, List<BaseComponent[]>> componentMassageMap = new HashMap<>();
    private static final Type massageMapType = new TypeToken<Map<UUID, List<String>>>() {}.getType();
    private static final BaseComponent[] baseComponentType = new BaseComponent[]{};

    private MessageHelper() {
    }

    public static Map<UUID, List<String>> getMassageMap() {
        return massageMap;
    }

    public static Map<UUID, List<BaseComponent[]>> getComponentMassageMap() {
        return componentMassageMap;
    }

    /**
     * 内部使用的离线玩家信息添加方法
     *
     * @param uuid    离线玩家UUID
     * @param massage 要发送的信息内容
     */
    private static void addMassage(UUID uuid, String massage) {
        if (massageMap.containsKey(uuid)) {
            massageMap.get(uuid).add(massage);
        } else {
            massageMap.put(uuid, new ArrayList<>(Collections.singleton(massage)));
        }
    }

    /**
     * 内部使用的离线玩家信息添加方法
     * @param uuid 离线玩家UUID
     * @param massage 要发送的JSON信息内容
     */
    private static void addMassage(UUID uuid, BaseComponent... massage) {
        if (componentMassageMap.containsKey(uuid)) {
            componentMassageMap.get(uuid).add(massage);
        } else {
            componentMassageMap.put(uuid, new ArrayList<>(Collections.singleton(massage)));
        }
    }

    /**
     * 发送专享红包的ActionBar信息
     *
     * @param players  可领取该红包的玩家列表
     * @param title    标题
     * @param subtitle 子标题
     */
    public static void broadcastSelectiveRedPacket(List<OfflinePlayer> players, String title, String subtitle) {
        players.stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).forEach(player -> {
            CompatibilityHelper.playLevelUpSound(player);
            CompatibilityHelper.sendTitle(player, title, subtitle);
        });
    }

    /**
     * 发送一般红包的ActionBar信息
     * @param title 标题
     * @param subtitle 子标题
     */
    public static void broadcastRedPacket(String title, String subtitle){
        getServer().getOnlinePlayers().forEach(player->{
            CompatibilityHelper.playLevelUpSound(player);
            CompatibilityHelper.sendTitle(player, title, subtitle);
        });
    }

    /**
     * 公告普通信息
     * @param color 颜色
     * @param msg 内容
     */
    public static void broadcastMsg(ChatColor color, String msg) {
        getServer().broadcastMessage(ChatColor.GREEN + "[红包]" + color + msg);
    }

    /**
     * 公告JSON信息
     * @param msg 内容
     */
    public static void broadcastMsg(BaseComponent...msg) {
        List<BaseComponent> components = new ArrayList<>(Arrays.asList(msg));
        components.add(0, new TextComponent(ChatColor.GREEN + "[红包]"));
        getServer().getOnlinePlayers().forEach(player -> CompatibilityHelper.sendJSONMessage(player, components.toArray(baseComponentType)));

    }

    /**
     * 发送普通信息（需要玩家在线）
     * @param sender 接收者
     * @param color 颜色
     * @param msg 内容
     */
    public static void sendSimpleMsg(CommandSender sender, ChatColor color, String msg){
        sender.sendMessage(ChatColor.GREEN + "[红包]" + color + msg);
    }

    /**
     * 发送JSON信息（需要玩家在线）
     * @param sender 接收者
     * @param msg 内容
     */
    public static void sendSimpleMsg(Player sender, BaseComponent... msg) {
        CompatibilityHelper.sendJSONMessage(sender, msg);
    }

    /**
     * 发送JSON信息（不需要玩家在线）
     * @param sender 接收者
     * @param msg 内容
     */
    public static void sendServiceMsg(OfflinePlayer sender, BaseComponent...msg) {
        if (sender.isOnline()) {
            sendSimpleMsg(sender.getPlayer(),msg);
        } else {
            addMassage(sender.getUniqueId(), msg);
        }
    }

    /**
     * 发送JSON信息（不需要玩家在线）
     * @param sender 接收者
     * @param msg 内容
     */
    public static void sendServiceMsg(OfflinePlayer sender, ChatColor color, String msg) {
        if (sender.isOnline()) {
            sendSimpleMsg(sender.getPlayer(),color,msg);
        } else {
            addMassage(sender.getUniqueId(), msg);
        }
    }

    /**
     * 初始化方法
     * @param status 设置状态。true为启动。false为禁用
     */
    public static void setStatus(boolean status) {
        Path path = getInstance().getDataFolder().toPath().resolve("PlayerData.json");
        if (status) {
            if (Files.exists(path)) {
                try {
                    FromJson(Files.readAllLines(path));
                } catch (IOException ex) {
                    throw new RuntimeException("无法加载将要发送给玩家的消息！", ex);
                }
            }
        } else {
            try {
                Files.write(path, getJson(), StandardCharsets.UTF_8);
            }catch (IOException e){
                throw new RuntimeException("无法保存将要发送给玩家的消息！", e);
            }
        }
    }

    /**
     * 离线玩家信息反序列化方法
     * @param json 序列化后的内容
     */
    @SuppressWarnings("unchecked")
    private static void FromJson(List<String> json) {
        if(json.size()==2) {
            massageMap.putAll(getGson().fromJson(json.get(0), massageMapType));
            ((Map<UUID, List<String>>) getGson().fromJson(json.get(1), massageMapType)).forEach((k, v) -> componentMassageMap.put(k, v.parallelStream().map(ComponentSerializer::parse).collect(Collectors.toList())));
        }
    }

    /**
     * 离线玩家信息序列化方法
     */
    private static List<String> getJson() {
        HashMap<UUID, List<String>> componentMap = new HashMap<>();
        componentMassageMap.entrySet().parallelStream().forEach(e -> componentMap.put(e.getKey(), e.getValue().parallelStream().map(ComponentSerializer::toString).collect(Collectors.toList())));
        return Arrays.asList(getGson().toJson(massageMap), getGson().toJson(componentMap));
    }
}
