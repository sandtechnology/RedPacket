package sandtechnology.redpacket.util;

import org.bukkit.Bukkit;

/**
 * 一般工具类
 */
public class CommonHelper {
    //空函数占位符
    public static final voidFunction emptyFunction = () -> {
    };

    private CommonHelper() {
    }

    /**
     * 懒人辅助方法
     *
     * @param check     输入的布尔值
     * @param doIfTrue  当check为true时执行的函数
     * @param doIfFalse 当check为false时执行的函数
     * @return check的布尔值
     */
    public static boolean checkAndDoSomething(boolean check, voidFunction doIfTrue, voidFunction doIfFalse) {
        if (check) {
            doIfTrue.func();
        } else {
            doIfFalse.func();
        }
        return check;
    }

    /**
     * 为版本兼容而使用的转发命令方法
     *
     * @param start 指令名称
     * @param args  参数
     * @return 命令是否成功执行
     */
    public static boolean executeCommand(String start, String... args) {
        return Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), start + " " + String.join(" ", args));
    }

    /**
     * void函数
     */
    @FunctionalInterface
    public interface voidFunction {
        void func();
    }
}
