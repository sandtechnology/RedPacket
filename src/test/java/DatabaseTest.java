import org.bukkit.entity.Player;
import sandtechnology.jielong.database.AbstractDatabaseManager;
import sandtechnology.jielong.redpacket.RedPacket;

import java.util.*;

import static sandtechnology.jielong.RedPacketPlugin.getDatabaseManager;

public class DatabaseTest {
    public static void main(String[] args) {
        AbstractDatabaseManager abstractDatabaseManager = getDatabaseManager();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                    System.err.println("Error form " + t.getName() + ": ");
                    e.printStackTrace();
                    abstractDatabaseManager.setRunning(false);
                }
        );
        long time = System.currentTimeMillis();
        int i = 0;
        Player player = new FakePlayer("Test");
        while (i != 10) {
            long time1 = System.currentTimeMillis();
            abstractDatabaseManager.store(new RedPacket.Builder().player(player).amount(100).money(10).givers(new HashSet<>(Collections.singletonList(new FakePlayer("233").getUniqueId()))).build());
            System.out.println("Store Time: " + (System.currentTimeMillis() - time1) + " ms");
            i++;
        }
        System.out.println("Total Store Time: " + (System.currentTimeMillis() - time) + " ms");
        try {
            Thread.sleep(10000L);
        } catch (Exception ignored) {
        }
        //LuckAmountTest.main(args);
        time = System.currentTimeMillis();
        abstractDatabaseManager.getAll(new FakePlayer("Test"), 1000).forEach(System.out::println);
        System.out.println("Total Read Time: " + (System.currentTimeMillis() - time) + " ms");
        abstractDatabaseManager.setRunning(false);
    }
}
