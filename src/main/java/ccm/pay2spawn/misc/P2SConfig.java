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

package ccm.pay2spawn.misc;

import ccm.pay2spawn.random.RandomRegistry;
import ccm.pay2spawn.util.Helper;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.regex.Pattern;

import static ccm.pay2spawn.util.Constants.*;

/**
 * Uses subclasses to make file cleaner
 *
 * @author Dries007
 * @see ccm.pay2spawn.Pay2Spawn#getConfig()
 */
public class P2SConfig
{
    public final static String HUD = MODID + ".Hud";

    public Configuration configuration;

    public boolean forceServerconfig = true;
    public boolean forceP2S          = false;
    public double  min_donation      = 1;
    public int     interval          = 10;
    public String  API_Key           = "";
    public String  channel           = "";
    public String  twitchToken       = "";
    public Pattern[]    blacklist_Name_p;
    public Pattern[]    blacklist_Note_p;
    public Pattern[]    whitelist_Name_p;
    public Pattern[]    whitelist_Note_p;
    public FileSettings file;
    public  String   subReward      = "-2";
    public  String   serverMessage  = "$streamer got $$amount from $name and $reward_name was triggerd!";
    @SuppressWarnings("FieldCanBeLocal")
    private String[] blacklist_Name = {"fuck", "cunt", "dick", "shit"};
    @SuppressWarnings("FieldCanBeLocal")
    private String[] blacklist_Note = {"fuck", "cunt", "dick", "shit"};
    @SuppressWarnings("FieldCanBeLocal")
    private String[] whitelist_Name = {"\"[\\w-]*\""};
    @SuppressWarnings("FieldCanBeLocal")
    private String[] whitelist_Note = {};

    public P2SConfig(File file)
    {
        configuration = new Configuration(file);

        configuration.addCustomCategoryComment(MODID, "All config settings for " + NAME + "\nDon't forget the other files in this folder!\nFor all message type things (all text basically) use & for color codes!");

        interval = configuration.get(MODID, "interval", interval, "Amount of seconds in between each pull.").getInt();
        channel = configuration.get(MODID, "channel", channel, "Your channel name, see http://donationtrack.nightdev.com/").getString();
        API_Key = configuration.get(MODID, "API_Key", API_Key, "Your API Key, see http://donationtrack.nightdev.com/").getString();
        twitchToken = configuration.get(MODID, "twitchToken", twitchToken, "Get it from http://dries007.net/ccm/p2s/ ONLY WORKS IF YOU HAVE A SUB BUTTON.").getString();
        min_donation = configuration.get(MODID, "min_donation", min_donation, "Below this threshold no donations will be resisted. Set to 0 to disable.").getDouble(min_donation);
        forceServerconfig = configuration.get(MODID, "forceServerconfig", forceServerconfig, "If a client connects, force the config from the server to the client.").getBoolean(forceServerconfig);
        forceP2S = configuration.get(MODID, "forceP2S", forceP2S, "If a client connects, kick it if there is no P2S. If there is, p2s will be locked in ON mode.").getBoolean(forceP2S);
        subReward = configuration.get(MODID, "subReward", subReward, "The reward triggerd by someone subscribing. You can use the random tags in here.").getString();
        serverMessage = configuration.get(MODID, "serverMessage", serverMessage, "Server config deferments the structure. Vars: $name, $amount, $note, $streamer, $reward_message, $reward_name, $reward_amount, $reward_countdown.").getString();
        try
        {
            //noinspection ResultOfMethodCallIgnored
            Double.parseDouble(RandomRegistry.solveRandom(DOUBLE, subReward));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Configuration error. " + subReward + " doesn't parse properly. Must result in a boolean.");
        }

        String filterCat = MODID + ".filter";
        configuration.addCustomCategoryComment(filterCat, "All filters use regex, very useful site: http://gskinner.com/RegExr/\nMatching happens case insensitive.\nUSE DOUBLE QUOTES (\") AROUND EACH LINE!");
        blacklist_Name = configuration.get(filterCat, "blacklist_Name", blacklist_Name, "If matches, name gets changed to Anonymous. Overrules whitelist.").getStringList();
        blacklist_Name_p = new Pattern[blacklist_Name.length];
        for (int i = 0; i < blacklist_Name.length; i++) blacklist_Name_p[i] = Pattern.compile(Helper.removeQuotes(blacklist_Name[i]), Pattern.CASE_INSENSITIVE);

        blacklist_Note = configuration.get(filterCat, "blacklist_Note", blacklist_Note, "If matches, the match gets removed. Overrules whitelist.").getStringList();
        blacklist_Note_p = new Pattern[blacklist_Note.length];
        for (int i = 0; i < blacklist_Note.length; i++) blacklist_Note_p[i] = Pattern.compile(Helper.removeQuotes(blacklist_Note[i]), Pattern.CASE_INSENSITIVE);

        whitelist_Name = configuration.get(filterCat, "whitelist_Name", whitelist_Name, "If NOT matches, name gets changed to Anonymous. Overruled by blacklist.").getStringList();
        whitelist_Name_p = new Pattern[whitelist_Name.length];
        for (int i = 0; i < whitelist_Name.length; i++) whitelist_Name_p[i] = Pattern.compile(Helper.removeQuotes(whitelist_Name[i]), Pattern.CASE_INSENSITIVE);

        whitelist_Note = configuration.get(filterCat, "whitelist_Note", whitelist_Note, "If NOT matches, note gets removed. Overruled by blacklist.").getStringList();
        whitelist_Note_p = new Pattern[whitelist_Note.length];
        for (int i = 0; i < whitelist_Note.length; i++) whitelist_Note_p[i] = Pattern.compile(Helper.removeQuotes(whitelist_Note[i]), Pattern.CASE_INSENSITIVE);

        this.file = new FileSettings();

        configuration.save();
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
