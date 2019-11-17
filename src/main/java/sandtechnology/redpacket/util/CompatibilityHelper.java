package sandtechnology.redpacket.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;
import static sandtechnology.redpacket.RedPacketPlugin.getInstance;

public class CompatibilityHelper {
    /*
    多版本兼容
    1.14.3->14
    1.13.2->13
    1.12.2->12
    1.8.8->8
    1.7.10->7
    */
    private static final int version = Integer.parseInt(getServer().getBukkitVersion().split("\\.")[1]);

    //NMS名： "org.bukkit.craftbukkit.v1_x_Rx"->{"org","bukkit","craftbukkit","v1_x_Rx"}->"v1_x_Rx"
    private static final String nmsName = getServer().getClass().getPackage().getName().split("\\.")[3];

    private static Class<?> IChatBaseComponent;
    private static Class<?> chatSerializer;
    private static Class<?> craftPlayer;
    private static Class<?> entityPlayer;
    private static Class<?> PacketPlayOutTitle;
    private static Class<?> EnumTitleAction;
    private static Class<?> PlayerConnection;
    private static Enum<? extends Enum>[] EnumTitleActions;
    private static Method getHandle;
    private static Method sendMessage;
    private static Method toComponent;
    private static Method sendPacket;
    private static Constructor<?> CPacketPlayOutTitle;

    private CompatibilityHelper() {
    }


    private static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + nmsName + "." + name);
    }

    public static void setup() {
        try {
            entityPlayer = getNMSClass("EntityPlayer");
            chatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
            IChatBaseComponent = getNMSClass("IChatBaseComponent");
            PacketPlayOutTitle = getNMSClass("PacketPlayOutTitle");
            PlayerConnection = getNMSClass("PlayerConnection");
            craftPlayer = Class.forName("org.bukkit.craftbukkit." + nmsName + "." + "entity.CraftPlayer");
            getHandle = craftPlayer.getMethod("getHandle");
            sendMessage = entityPlayer.getMethod("sendMessage", IChatBaseComponent);
            toComponent = chatSerializer.getMethod("a", String.class);
            sendPacket = PlayerConnection.getMethod("sendPacket", getNMSClass("Packet"));
            EnumTitleAction = Arrays.stream(PacketPlayOutTitle.getClasses()).filter(Class::isEnum).collect(Collectors.toList()).get(0);
            EnumTitleActions = (Enum<? extends Enum>[]) EnumTitleAction.getEnumConstants();
            CPacketPlayOutTitle = PacketPlayOutTitle.getConstructor(EnumTitleAction, IChatBaseComponent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object invoke(Method method, Object obj, Object... objs) {
        try {
            return method.invoke(obj, objs);
        } catch (Exception e) {
            throw new RuntimeException("在反射调用方法时发生错误！" + method.getName(), e);
        }
    }

    private static Object newInstance(Constructor constructor, Object... objs) {
        try {
            return constructor.newInstance(objs);
        } catch (Exception e) {
            throw new RuntimeException("在反射实例化类时发生错误！类名：", e);
        }
    }

    public static void playLevelUpSound(Player player) {
        if (version > 8) {
            playSound(player, "ENTITY_PLAYER_LEVELUP");
        } else {
            playSound(player, "LEVEL_UP");
        }
    }

    public static void playMeowSound(Player player) {
        if (version > 8) {
            playSound(player, "ENTITY_CAT_AMBIENT");
        } else {
            playSound(player, "CAT_MEOW");
        }
    }

    private static void playSound(Player player, String name) {
        player.playSound(player.getLocation(), Sound.valueOf(name), 100, 1);
    }

    private static Object getDeclaredFieldAndGetIt(Class<?> target, String field, Object instance) {
        try {
            return target.getDeclaredField(field).get(instance);
        } catch (Exception e) {
            throw new RuntimeException("在反射获取字段时发生错误！方法名：", e);
        }
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        if (version >= 11) {
            player.sendTitle(title, subtitle, -1, -1, -1);
        } else {
            if (version >= 8) {
                //反射需要较长时间，采取异步处理再发送消息
                Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> {
                    Object connectionInstance = getDeclaredFieldAndGetIt(entityPlayer, "playerConnection", invoke(getHandle, player));
                    Object titlePacket = newInstance(CPacketPlayOutTitle, EnumTitleActions[0], invoke(toComponent, null, ComponentSerializer.toString(new TextComponent(title))));
                    Object subtitlePacket = newInstance(CPacketPlayOutTitle, EnumTitleActions[1], invoke(toComponent, null, ComponentSerializer.toString(new TextComponent(subtitle))));
                    Bukkit.getScheduler().runTask(getInstance(), () -> {
                        invoke(sendPacket, connectionInstance, titlePacket);
                        invoke(sendPacket, connectionInstance, subtitlePacket);
                    });
                });
            }
        }
    }

    public static void sendJSONMessage(Player player, BaseComponent... components) {
        if (version >= 12) {
            player.spigot().sendMessage(components);
        } else {
            if (version >= 7) {
                //https://www.spigotmc.org/threads/get-player-ping-with-reflection.147773/
                //反射需要较长时间，采取异步处理再发送消息
                Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> {
                    Object playerInstance = invoke(getHandle, player);
                    Object JSONString = invoke(toComponent, null, ComponentSerializer.toString(components));
                    Bukkit.getScheduler().runTask(getInstance(), () -> invoke(sendMessage, playerInstance, JSONString));
                });
            }
        }
    }

}
