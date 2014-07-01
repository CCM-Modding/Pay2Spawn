package ccm.pay2spawn.checkers;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.misc.Donation;
import ccm.pay2spawn.util.Helper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration;

import java.util.*;

public class CheckerHandler
{
    public static final Comparator<Donation> RECENT_DONATION_COMPARATOR = new Comparator<Donation>() {
        @Override
        public int compare(Donation o1, Donation o2)
        {
            if (o1.time == o2.time) return 0;
            return o1.time > o2.time ? -1 : 1;
        }
    };
    public static final Comparator<Donation> AMOUNT_DONATION_COMPARATOR = new Comparator<Donation>() {
        @Override
        public int compare(Donation o1, Donation o2)
        {
            if (o1.amount == o2.amount) return 0;
            return o1.amount > o2.amount ? -1 : 1;
        }
    };

    private static HashMap<String, AbstractChecker> map = new HashMap<>();
    static
    {
        register(StreamtipChecker.INSTANCE);
        register(ChildsplayChecker.INSTANCE);
        register(TwitchChecker.INSTANCE);
    }

    public static Collection<AbstractChecker> getAbstractCheckers()
    {
        return map.values();
    }

    public static void register(AbstractChecker abstractChecker)
    {
        map.put(abstractChecker.getName(), abstractChecker);
    }

    public static void doConfig(Configuration configuration)
    {
        for (AbstractChecker abstractChecker : map.values())
        {
            abstractChecker.doConfig(configuration);
        }
    }

    public static void init()
    {
        for (AbstractChecker abstractChecker : map.values())
        {
            if (abstractChecker.enabled())
                abstractChecker.init();
        }
    }

    public static void fakeDonation(double amount)
    {
        Donation donation = new Donation(UUID.randomUUID().toString(), amount, new Date().getTime());
        Helper.msg(EnumChatFormatting.GOLD + "[P2S] Faking donation of " + amount + ".");
        Pay2Spawn.getRewardsDB().process(donation, false);
    }
}
