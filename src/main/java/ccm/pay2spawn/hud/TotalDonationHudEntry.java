package ccm.pay2spawn.hud;

import ccm.pay2spawn.P2SConfig;
import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.util.Constants;
import ccm.pay2spawn.util.Helper;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;

public class TotalDonationHudEntry implements IHudEntry
{
    final int    position;
    final String format;
    private double amount = 0;

    public TotalDonationHudEntry(String configCat, int defaultPosition, String defaultFormat, double amount)
    {
        Configuration config = Pay2Spawn.getConfig().configuration;

        position = config.get(P2SConfig.HUD + "." + configCat, "position", defaultPosition, "0 = off, 1 = left top, 2 = right top, 3 = left bottom, 4 = right bottom.").getInt(defaultPosition);

        format = Helper.formatColors(config.get(P2SConfig.HUD + "." + configCat, "format", defaultFormat).getString());
        this.amount = amount;

        Pay2Spawn.getConfig().save();
    }

    @Override
    public int getPosition()
    {
        return position;
    }

    @Override
    public int getAmount()
    {
        return 1;
    }

    @Override
    public String getHeader()
    {
        return "";
    }

    @Override
    public String getFormat()
    {
        return format;
    }

    public void addToDonationamount(double amount)
    {
        this.amount += amount;
    }

    @Override
    public void addToList(ArrayList<String> list)
    {
        if (position != 0)
        {
            list.add(format.replace("$amount", Constants.CURRENCY_FORMATTER.format(amount)));
        }
    }

    public double getDonated()
    {
        return amount;
    }
}
