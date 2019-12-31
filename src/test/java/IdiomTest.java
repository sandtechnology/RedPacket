import sandtechnology.redpacket.util.IdiomManager;

public class IdiomTest {
    public static void main(String[] args) {
        IdiomManager.debugSetup(new IdiomTest());
        String pingyin = IdiomManager.getIdiomPinyin("海阔天空");

        System.out.print(pingyin);

    }
}
