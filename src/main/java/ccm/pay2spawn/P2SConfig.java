package ccm.pay2spawn;

import net.minecraftforge.common.Configuration;

import java.io.File;

import static ccm.pay2spawn.util.Archive.MODID;

public class P2SConfig
{
    private Configuration configuration;
    public boolean printEntityList = true;
    public int     port            = 1562;
    public int     interval        = 10;
    public String API_Key = "";
    public String channel = "";
    public String currency = "$";

    P2SConfig(File file)
    {
        configuration = new Configuration(file);

        printEntityList = configuration.get(MODID, "printEntityList", printEntityList, "Make a list with entity to ID").getBoolean(true);
        interval = configuration.get(MODID, "interval", interval, "Amount of seconds in between each pull.").getInt();
        channel = configuration.get(MODID, "channel", channel, "Your channel name, see http://donationtrack.nightdev.com/").getString();
        API_Key = configuration.get(MODID, "API_Key", API_Key, "Your API Key, see http://donationtrack.nightdev.com/").getString();
        currency = configuration.get(MODID, "currency", currency).getString();

        configuration.save();
    }
}
