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

import ccm.pay2spawn.checkers.CheckerHandler;
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
    public Pattern[]    blacklist_Name_p;
    public Pattern[]    blacklist_Note_p;
    public Pattern[]    whitelist_Name_p;
    public Pattern[]    whitelist_Note_p;
    public  String   subReward      = "-2";
    public  String   serverMessage  = "$streamer got $$amount from $name and $reward_name was triggered!";
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

        forceServerconfig = configuration.get(MODID, "forceServerconfig", forceServerconfig, "If a client connects, force the config from the server to the client.").getBoolean(forceServerconfig);
        forceP2S = configuration.get(MODID, "forceP2S", forceP2S, "If a client connects, kick it if there is no P2S. If there is, p2s will be locked in ON mode.").getBoolean(forceP2S);
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

        CheckerHandler.doConfig(configuration);

        save();
    }

    public void save()
    {
        if (configuration.hasChanged()) configuration.save();
    }
}
