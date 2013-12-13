package ccm.pay2spawn;

import ccm.pay2spawn.util.Helper;
import net.minecraftforge.common.Configuration;

import java.io.File;

import static ccm.pay2spawn.util.Archive.MODID;
import static ccm.pay2spawn.util.Archive.NAME;

public class P2SConfig
{

    Configuration configuration;

    public String  messageNoReward = "&a[$name donated $$amount]";
    public boolean printHelpLists = true;
    public int     interval        = 10;
    public String  API_Key         = "";
    public String  channel         = "";
    public HudSettings hud;
    public FileSettings file;

    P2SConfig(File file)
    {
        configuration = new Configuration(file);

        configuration.addCustomCategoryComment(MODID, "All config settings for " + NAME + "\nDon't forget the other files in this folder!\nFor all message type things (all text basically) use & for color codes!");

        printHelpLists = configuration.get(MODID, "printHelpLists", printHelpLists, "Make a list with entity to ID").getBoolean(true);
        interval = configuration.get(MODID, "interval", interval, "Amount of seconds in between each pull.").getInt();
        channel = configuration.get(MODID, "channel", channel, "Your channel name, see http://donationtrack.nightdev.com/").getString();
        API_Key = configuration.get(MODID, "API_Key", API_Key, "Your API Key, see http://donationtrack.nightdev.com/").getString();
        messageNoReward = Helper.formatColors(configuration.get(MODID, "NoRewardMessage", messageNoReward, "Gets used when the amount donated doesn't match anything.").getString());

        this.hud = new HudSettings();
        this.file = new FileSettings();

        configuration.save();
    }

    class HudSettings
    {
        public final static String HUD = MODID + ".Hud";

        public int     top           = 1;
        public int     top_amount    = 5;
        public String  top_format    = "$name: $$amount";
        public String  top_header    = "-- Top donations --";
        public int     recent        = 2;
        public int     recent_amount = 5;
        public String  recent_format = "$name: $$amount";
        public String  recent_header = "-- Recent donations --";

        public HudSettings()
        {
            configuration.addCustomCategoryComment(HUD, "Donation lists on screen!");

            top =        configuration.get(HUD, "top",        top,        "Display a list of the top donations on screen. 0 = off, 1 = left, 2 = right.").getInt();
            top_amount = configuration.get(HUD, "top_amount", top_amount, "Amount of top donations, max = 5.").getInt();
            if (top_amount > 5) top_amount = 5;
            top_format = Helper.formatColors(configuration.get(HUD, "top_format", top_format, "Vars: $name, $amount, $note.").getString());
            top_header = Helper.formatColors(configuration.get(HUD, "top_header", top_header, "empty for no header.").getString());

            recent        = configuration.get(HUD, "recent",        recent,        "Display a list of the most recent donations on screen. 0 = off, 1 = left, 2 = right.").getInt();
            recent_amount = configuration.get(HUD, "recent_amount", recent_amount, "Amount of recent donations, max = 5.").getInt();
            if (recent_amount > 5) recent_amount = 5;
            recent_format = Helper.formatColors(configuration.get(HUD, "recent_format", recent_format, "Vars: $name, $amount, $note.").getString());
            recent_header = Helper.formatColors(configuration.get(HUD, "recent_header", recent_header, "empty for no header.").getString());
        }
    }

    class FileSettings
    {
        public final static String FILE = MODID + ".file";

        public int top = 1;
        public int top_amount = 5;
        public String top_format = "$name: $$amount";
        public int recent = 1;
        public int recent_amount = 5;
        public String recent_format = "$name: $$amount";

        public FileSettings()
        {
            configuration.addCustomCategoryComment(FILE, "Donation lists on file!\nUse with OBS (or others) and a text on screen plugin.");

            top = configuration.get(FILE, "top", top, "0 = off, 1 = 1 per line, 2 = all on 1 line").getInt();
            top_amount = configuration.get(FILE, "top_amount", top_amount, "Amount of top donations, max = 5.").getInt();
            if (top_amount > 5) top_amount = 5;
            top_format = Helper.formatColors(configuration.get(FILE, "top_format", top_format, "Vars: $name, $amount, $note.").getString());

            recent = configuration.get(FILE, "recent", recent, "0 = off, 1 = 1 per line, 2 = all on 1 line").getInt();
            recent_amount = configuration.get(FILE, "recent_amount", recent_amount, "Amount of recent donations, max = 5.").getInt();
            if (recent_amount > 5) recent_amount = 5;
            recent_format = Helper.formatColors(configuration.get(FILE, "recent_format", recent_format, "Vars: $name, $amount, $note.").getString());
        }
    }
}
