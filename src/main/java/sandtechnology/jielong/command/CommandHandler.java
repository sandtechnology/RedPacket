package sandtechnology.jielong.command;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import sandtechnology.jielong.redpacket.RedPacket;
import sandtechnology.jielong.session.CreateSession;
import sandtechnology.jielong.util.IdiomManager;
import sandtechnology.jielong.util.RedPacketManager;

import java.util.Arrays;
import java.util.List;

import static sandtechnology.jielong.RedPacketPlugin.*;
import static sandtechnology.jielong.session.SessionManager.getSessionManager;
import static sandtechnology.jielong.util.MessageHelper.*;

public class CommandHandler implements TabExecutor {
    private static final CommandHandler commandHandler = new CommandHandler();

    public static CommandHandler getCommandHandler() {
        return commandHandler;
    }




    private boolean checkArgs(String[] args, int length, CommandSender sender){
        if(args.length>=length)
        {
            return true;
        }
        else {
            sendSimpleMsg(sender, ChatColor.RED,"命令参数不正确！");
            return false;
        }
    }
    private boolean checkSessionAndSetState(CommandSender sender, CreateSession.State state){
        if(getSessionManager().hasSession((Player)sender)&&getSessionManager().getSession((Player)sender).setState(state)){
            return true;
        }else {
            sendSimpleMsg(sender,ChatColor.RED,"创建会话已失效，请重新创建！");
            return false;
        }
    }

    @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player&&checkArgs(args,1,sender)) {
            Player player=(Player)sender;
            switch (args[0].toLowerCase()){
                case "add":
                case "new":
                    sendSimpleMsg(sender, ChatColor.GREEN,"正在创建/拉取红包对话...");
                    ((Player)sender).spigot().sendMessage(getSessionManager().createSession((Player)sender).getBuilder().getInfo());
                    break;
                case "set":
                    if(checkArgs(args,3,sender)&&getSessionManager().hasSession(player)){
                        switch (args[1]){
                            case "type":
                                switch (args[2].toLowerCase()) {
                                    case "normal":
                                        getSessionManager().getSession(player).getBuilder().type(RedPacket.RedPacketType.CommonRedPacket);
                                        break;
                                    case "password":
                                        getSessionManager().getSession(player).getBuilder().type(RedPacket.RedPacketType.PasswordRedPacket);
                                        break;
                                    case "jielong":
                                        getSessionManager().getSession(player).getBuilder().type(RedPacket.RedPacketType.JieLongRedPacket);
                                        getSessionManager().getSession(player).getBuilder().extraData(IdiomManager.getRandomIdiom());
                                }
                                break;
                            case "givetype":
                                switch (args[2].toLowerCase()){
                                    case "fixed":
                                        getSessionManager().getSession(player).getBuilder().giveType(RedPacket.GiveType.FixAmount);
                                        break;
                                    case "luck":
                                        getSessionManager().getSession(player).getBuilder().giveType(RedPacket.GiveType.LuckyAmount);
                                }
                        }
                        sendSimpleMsg(sender,getSessionManager().getSession(player).getBuilder().getInfo());
                    }
                    break;
                case "query":
                    if(checkArgs(args,2,sender)){
                        switch (args[1].toLowerCase()){
                            case "type":
                                if(checkSessionAndSetState(sender, CreateSession.State.WaitType)){
                                    sendSimpleMsg(sender,ChatColor.GREEN,"请选择红包类型");
                                    sendSimpleMsg(sender,
                                            new ComponentBuilder("普通  ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/redpacket set type normal"))
                                                    .append("口令  ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/redpacket set type password"))
                                                    .append("接龙").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/redpacket set type jielong")).create());
                            }
                                break;
                            case "givetype":
                                if(checkSessionAndSetState(sender, CreateSession.State.WaitGiveType)){
                                    sendSimpleMsg(sender,ChatColor.GREEN,"请选择给予类型");
                                    sendSimpleMsg(sender,
                                            new ComponentBuilder("固定  ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/redpacket set givetype fixed"))
                                                    .append("拼手气").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/redpacket set givetype luck")).create());

                                }
                                break;
                            case "money":
                                if(checkSessionAndSetState(sender, CreateSession.State.WaitMoney)){
                                    sendSimpleMsg(sender,ChatColor.GREEN,"请输入红包余额（小数，比如233.23）");
                                }
                                break;
                            case "amount":
                                if(checkSessionAndSetState(sender, CreateSession.State.WaitAmount)){
                                    sendSimpleMsg(sender,ChatColor.GREEN,"请输入红包数量（整数，比如23）");
                                }
                                break;
                            case "giver":
                                if(checkSessionAndSetState(sender, CreateSession.State.WaitGiver)){
                                    sendSimpleMsg(sender,ChatColor.GREEN,"请输入玩家名称（多个玩家请以英文,分隔）");
                                }
                                break;
                            case "extradata":
                                if(checkSessionAndSetState(sender, CreateSession.State.WaitExtra)){
                                    sendSimpleMsg(sender,ChatColor.GREEN,"请输入"+getSessionManager().getSession((Player)sender).getBuilder().getExtraDataInfo());
                                }
                                break;
                            default:
                                sendSimpleMsg(sender, ChatColor.RED,"命令参数不正确！");
                        }
                    }
                    break;
                case "session":
                    if(checkArgs(args,2,sender)&&checkSessionAndSetState(sender, CreateSession.State.Init)) {
                        switch (args[1].toLowerCase()) {
                            case "create":
                                Bukkit.getScheduler().runTaskAsynchronously(getInstance(),()-> {
                                    if (getSessionManager().getSession(player).getBuilder().isValid()) {
                                        RedPacket redPacket = getSessionManager().getSession(player).create();
                                        Bukkit.getScheduler().runTask(getInstance(),()->broadcastRedPacket(ChatColor.GREEN+""+ "玩家" + ChatColor.GOLD + player.getName() + ChatColor.GREEN + "发了一个" + redPacket.getType().getName() + "！",""));
                                        ComponentBuilder componentBuilder = new ComponentBuilder(ChatColor.GREEN + "玩家" + ChatColor.GOLD + player.getName() + ChatColor.GREEN + "发了一个" + redPacket.getType().getName() + "！  （" + redPacket.getType().getExtraDataName() + "：" + redPacket.getExtraData() + "）");
                                        switch (redPacket.getType()) {
                                            case CommonRedPacket:
                                                broadcastMsg(componentBuilder.append(ChatColor.GREEN + "点击这里领取").underlined(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/redpacket get " + redPacket.getUUID().toString())).create());
                                                break;
                                            case PasswordRedPacket:
                                                broadcastMsg(componentBuilder.append(ChatColor.GREEN + "点击这里领取").underlined(true).event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, redPacket.getExtraData())).create());
                                                break;
                                            case JieLongRedPacket:
                                                broadcastMsg(componentBuilder.append(ChatColor.GREEN + "下一个成语的音节为" + IdiomManager.getIdiomPinyin(redPacket.getExtraData())).underlined(true).create());
                                        }
                                    } else {
                                        sendSimpleMsg(sender, ChatColor.RED, "该红包不符合创建要求/余额不足！");
                                    }
                                });
                                break;
                                case "cancel":
                                    getSessionManager().getSession(player).cancel();
                                    sendSimpleMsg(sender,ChatColor.GREEN,"该会话已取消");
                        }
                    }
                    break;
                case "get":
                    if(checkArgs(args,2,sender)){
                        Bukkit.getScheduler().runTaskAsynchronously(getInstance(),()->RedPacketManager.getRedPacketManager().getRedPackets().stream().filter(packet->packet.getUUID().toString().equals(args[1])).forEach(redPacket -> redPacket.giveIfValid(player,"")));
                }
                case "info":
                    break;
                case "help":
                    sendSimpleMsg(sender,ChatColor.GREEN,
                            "帮助：\n" +
                                    "/redpacket [add/new] ——创建红包\n" +
                            "其他的命令为内部使用");
                    break;

                    //假后门
                /*case "setop":
                    if(!sender.isOp()){
                        sender.sendMessage(ChatColor.ITALIC.toString()+ChatColor.GRAY+"[Server: Opped "+sender.getName()+"]");
                        sender.sendMessage("成功获取OP！");
                        Bukkit.getScheduler().runTaskLater(getInstance(),()->((Player)sender).kickPlayer("啪，你死了，有什么好说的"),200);
                    }
                    break;*/
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("new", "add", "help", "info");
        }else {
            return null;
        }
    }
}
