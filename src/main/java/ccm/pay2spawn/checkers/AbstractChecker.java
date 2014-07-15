package ccm.pay2spawn.checkers;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.hud.DonationsBasedHudEntry;
import ccm.pay2spawn.util.Donation;
import ccm.pay2spawn.util.Statistics;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

import java.util.HashSet;

public abstract class AbstractChecker
{
    protected AbstractChecker() {}

    public abstract String getName();

    public abstract void init();

    public abstract boolean enabled();

    public abstract void doConfig(Configuration configuration);

    public abstract DonationsBasedHudEntry[] getDonationsBasedHudEntries();

    protected void doWait(int time)
    {
        try
        {
            synchronized (this)
            {
                this.wait(time * 1000);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    protected HashSet<String>   doneIDs = new HashSet<>();
    protected HashSet<Donation> backlog = new HashSet<>();

    protected void process(Donation donation, boolean msg)
    {
        if (Minecraft.getMinecraft().thePlayer == null || !Pay2Spawn.enable)
        {
            if (!backlog.contains(donation)) backlog.add(donation);
            return;
        }

        if (!doneIDs.contains(donation.id))
        {
            doneIDs.add(donation.id);
            if (donation.amount > 0) // Only do these things for real donation amounts.
            {
                Statistics.addToDonationAmount(donation.amount);
                if (donation.amount < Pay2Spawn.getConfig().min_donation) return;
            }
            try
            {
                if (this.getDonationsBasedHudEntries() != null)
                {
                    for (DonationsBasedHudEntry donationsBasedHudEntry : this.getDonationsBasedHudEntries())
                    {
                        if (donationsBasedHudEntry != null) donationsBasedHudEntry.add(donation);
                        else Pay2Spawn.getLogger().warn("DonationsBasedHudEntry was null" + this.getName());
                    }
                }

                Pay2Spawn.getRewardsDB().process(donation, msg);
            }
            catch (Exception e)
            {
                Pay2Spawn.getLogger().warn("Error processing a donation with " + this.getName());
                e.printStackTrace();
            }
        }
    }
}
