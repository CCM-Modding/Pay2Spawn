/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ccm.pay2spawn;

import ccm.pay2spawn.util.Helper;
import net.minecraftforge.common.Configuration;

import java.io.File;

import static ccm.pay2spawn.util.Constants.MODID;
import static ccm.pay2spawn.util.Constants.NAME;

/**
 * Uses subclasses to make file cleaner
 *
 * @author Dries007
 * @see ccm.pay2spawn.Pay2Spawn#getConfig()
 */
public class P2SConfig
{
    Configuration configuration;

    public boolean forceServerconfig = true;
    public double  min_donation      = 1;
    public int     interval          = 10;
    public String  API_Key           = "";
    public String  channel           = "";

    public HudSettings  hud;
    public FileSettings file;

    P2SConfig(File file)
    {
        configuration = new Configuration(file);

        configuration.addCustomCategoryComment(MODID, "All config settings for " + NAME + "\nDon't forget the other files in this folder!\nFor all message type things (all text basically) use & for color codes!");

        interval = configuration.get(MODID, "interval", interval, "Amount of seconds in between each pull.").getInt();
        channel = configuration.get(MODID, "channel", channel, "Your channel name, see http://donationtrack.nightdev.com/").getString();
        API_Key = configuration.get(MODID, "API_Key", API_Key, "Your API Key, see http://donationtrack.nightdev.com/").getString();
        min_donation = configuration.get(MODID, "min_donation", min_donation, "Below this threshold no donations will be resisted. Set to 0 to disable.").getDouble(min_donation);
        forceServerconfig = configuration.get(MODID, "forceServerconfig", forceServerconfig, "If a client connects, force the config from the server to the client.").getBoolean(forceServerconfig);

        this.hud = new HudSettings();
        this.file = new FileSettings();

        configuration.save();
    }

    public class HudSettings
    {
        public final static String HUD = MODID + ".Hud";

        public int    top           = 1;
        public int    top_amount    = 5;
        public String top_format    = "$name: $$amount";
        public String top_header    = "-- Top donations --";
        public int    recent        = 2;
        public int    recent_amount = 5;
        public String recent_format = "$name: $$amount";
        public String recent_header = "-- Recent donations --";

        public int    countdown        = 2;
        public String countdown_format = "$name incoming in $time sec.";
        public String countdown_header = "-- Countdown --";

        private HudSettings()
        {
            configuration.addCustomCategoryComment(HUD, "Donation lists on screen!");

            top = configuration.get(HUD,
                    "top",
                    top,
                    "Display a list of the top donations on screen. 0 = off, 1 = left top, 2 = right top, 3 = left bottom, 4 = right bottom.").getInt();
            top_amount = configuration.get(HUD, "top_amount", top_amount, "Amount of top donations, max = 5.").getInt();
            if (top_amount > 5) top_amount = 5;
            top_format = Helper.formatColors(configuration.get(HUD, "top_format", top_format, "Vars: $name, $amount, $note.").getString());
            top_header = Helper.formatColors(configuration.get(HUD, "top_header", top_header, "empty for no header. Use \\n for a blank line.").getString());

            recent = configuration.get(HUD,
                    "recent",
                    recent,
                    "Display a list of the most recent donations on screen. 0 = off, 1 = left, 2 = right, 3 = left bottom, 4 = right bottom.").getInt();
            recent_amount = configuration.get(HUD, "recent_amount", recent_amount, "Amount of recent donations, max = 5.").getInt();
            if (recent_amount > 5) recent_amount = 5;
            recent_format = Helper.formatColors(configuration.get(HUD, "recent_format", recent_format, "Vars: $name, $amount, $note.").getString());
            recent_header = Helper.formatColors(configuration.get(HUD, "recent_header", recent_header, "empty for no header. Use \\n for a blank line.").getString());

            countdown = configuration.get(HUD,
                    "countdown",
                    countdown,
                    "Display a list of the rewards on countdown on screen. 0 = off, 1 = left, 2 = right, 3 = left bottom, 4 = right bottom.").getInt();
            countdown_format = Helper.formatColors(configuration.get(HUD, "countdown_format", countdown_format, "Vars: $name (of the group), $time (in seconds).").getString());
            countdown_header = Helper.formatColors(configuration.get(HUD, "countdown_header", countdown_header, "empty for no header. Use \\n for a blank line.").getString());
        }
    }

    public class FileSettings
    {
        public final static String FILE = MODID + ".file";

        public int    top           = 1;
        public int    top_amount    = 5;
        public String top_format    = "$name: $$amount";
        public int    recent        = 1;
        public int    recent_amount = 5;
        public String recent_format = "$name: $$amount";

        private FileSettings()
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
