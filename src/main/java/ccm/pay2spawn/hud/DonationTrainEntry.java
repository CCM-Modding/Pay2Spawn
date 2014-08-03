package ccm.pay2spawn.hud;

import ccm.pay2spawn.P2SConfig;
import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.util.Helper;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;

/**
 * @author Dries007
 */
public class DonationTrainEntry implements IHudEntry
{
    public final ArrayList<String> strings = new ArrayList<>();
    final int position;
    final String format;
    private final String timeoutMessage;
    private int timeout;
    int time = -1, amount = 0;
    private String line;

    public DonationTrainEntry()
    {
        Configuration config = Pay2Spawn.getConfig().configuration;

        String configCat = "DonationTrain";
        position = config.get(P2SConfig.HUD + "." + configCat, "position", 2, "0 = off, 1 = left top, 2 = right top, 3 = left bottom, 4 = right bottom.").getInt(2);
        format = Helper.formatColors(config.get(P2SConfig.HUD + "." + configCat, "format", "Donationtrain! $amount donations already! Expires in $time.").getString());
        line = timeoutMessage = config.get(P2SConfig.HUD + "." + configCat, "timeoutMessage", "No donation train going :(").getString();
        timeout = config.get(P2SConfig.HUD + "." + configCat, "timeout", 60 * 3).getInt();

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

    @Override
    public void addToList(ArrayList<String> list)
    {
        list.add(line);
    }

    public void resetTimeout()
    {
        amount ++;
        time = timeout;
    }

    public void tick()
    {
        if (time > 0) time --;
        else return;

        if (time == 0)
        {
            line = timeoutMessage;
            amount = 0;
        }
        else
        {
            line = format.replace("$amount", amount + "").replace("$time", time + "");
        }
    }
}
