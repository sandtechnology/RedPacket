import sandtechnology.redpacket.redpacket.RedPacket;

import java.util.Random;
import java.util.UUID;

import static sandtechnology.redpacket.RedPacketPlugin.getDatabaseManager;
import static sandtechnology.redpacket.util.OperatorHelper.multiply;
import static sandtechnology.redpacket.util.OperatorHelper.toTwoPrecision;


class LuckAmountTest {
    public static void main(String[] args) {
        int testNum = 0;
        long time;
        Random random = new Random();
        while (testNum != 1E5) {
            testNum++;
            int amount = random.nextInt(500);
            double randomDouble = toTwoPrecision(random.nextDouble());
            double randomMoney = Math.max(multiply(amount, multiply(randomDouble, random.nextInt(10))), amount);
            time = System.currentTimeMillis();
            System.out.print("#" + testNum + "|" + amount + "|" + randomDouble + "|" + randomMoney);
            RedPacket redPacket = new RedPacket.Builder(new FakePlayer("Test")).money(randomMoney).amount(amount).extraData("233").build();
            getDatabaseManager().store(redPacket);
            for (; redPacket.getCurrentAmount() > 0; ) {
                if (redPacket.getCurrentMoney() < 0) {
                    throw new RuntimeException(
                            "\nTest #" + testNum +
                                    "\nPlayer#" + redPacket.getCurrentAmount() + " error!" +
                                    "\nRandomAmount:" + amount +
                                    "\nRandomMoney:" + randomMoney +
                                    "\nCurrentMoney:" + redPacket.getCurrentMoney() +
                                    "\nCurrentAmount:" + redPacket.getCurrentAmount()
                    );
                }
                redPacket.giveMoney(new FakePlayer(UUID.randomUUID()));
            }
            System.out.println("|Time:" + (System.currentTimeMillis() - time) + "ms");
        }
    }
}
