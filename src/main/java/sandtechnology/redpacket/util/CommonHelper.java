package sandtechnology.redpacket.util;

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
     * void函数
     */
    @FunctionalInterface
    public interface voidFunction {
        void func();
    }
}
