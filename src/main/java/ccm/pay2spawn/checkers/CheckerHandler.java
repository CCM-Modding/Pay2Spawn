package ccm.pay2spawn.checkers;

import ccm.pay2spawn.misc.Donation;
import net.minecraftforge.common.config.Configuration;

import java.util.Comparator;
import java.util.HashMap;

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
        register(new StreamtipChecker());
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
        for (AbstractChecker abstractChecker : map.values())
        {
            if (abstractChecker.enabled())
                abstractChecker.fakeDonation(amount);
        }
    }
}
