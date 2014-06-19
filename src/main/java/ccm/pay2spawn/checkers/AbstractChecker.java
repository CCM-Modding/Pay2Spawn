package ccm.pay2spawn.checkers;

import net.minecraftforge.common.config.Configuration;

public abstract class AbstractChecker
{
    public abstract String getName();

    public abstract void fakeDonation(double amount);

    public abstract void init();

    public abstract boolean enabled();

    public abstract void doConfig(Configuration configuration);

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
}
