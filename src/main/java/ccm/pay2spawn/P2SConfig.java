package ccm.pay2spawn;

import ccm.pay2spawn.util.EnumSpawnType;
import ccm.pay2spawn.util.Helper;
import net.minecraftforge.common.Configuration;

import java.io.File;

import static ccm.pay2spawn.util.Archive.MODID;
import static ccm.pay2spawn.util.Archive.NAME;

public class P2SConfig
{
    private Configuration configuration;
    public boolean printEntityList = true;
    public int     port            = 1562;
    public int     interval        = 10;
    public String  API_Key         = "";
    public String  channel         = "";
    public String  currency        = "$";

    public String[] messages = new String[EnumSpawnType.values().length + 1];

    P2SConfig(File file)
    {
        configuration = new Configuration(file);

        configuration.addCustomCategoryComment(MODID, "All config settings for " + NAME + "\nDon't forget the other files in this folder!");

        printEntityList = configuration.get(MODID, "printEntityList", printEntityList, "Make a list with entity to ID").getBoolean(true);
        interval = configuration.get(MODID, "interval", interval, "Amount of seconds in between each pull.").getInt();
        channel = configuration.get(MODID, "channel", channel, "Your channel name, see http://donationtrack.nightdev.com/").getString();
        API_Key = configuration.get(MODID, "API_Key", API_Key, "Your API Key, see http://donationtrack.nightdev.com/").getString();
        currency = configuration.get(MODID, "currency", currency).getString();

        configuration.addCustomCategoryComment(MODID + ".messages",
                "$name is the Twitch.tv name, $amount is the amount, $spawned is the item/entity/potion effect name. Use & for MC colours.");
        for (int i = 0; i < EnumSpawnType.values().length; i++)
        {
            EnumSpawnType type = EnumSpawnType.values()[i];
            messages[type.ordinal()] = Helper.formatColors(configuration.get(MODID + ".messages", type.name(), type.getDefaultMessage()).getString());
        }
        messages[EnumSpawnType.values().length] = Helper.formatColors(configuration.get(MODID + ".messages",
                "NO_REWARD",
                "&a[$name donated $amount]",
                "This message gets used when the amount donated doesn't match anything.").getString());

        configuration.save();
    }
}
