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

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.hud.Hud;
import ccm.pay2spawn.hud.JsonBasedHudEntry;
import ccm.pay2spawn.random.RandomRegistry;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import ccm.pay2spawn.util.MetricsHelper;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.*;

/**
 * The thread that does the actual checking with nightdevs Streamdonations
 *
 * @author Dries007
 */
public class DonationCheckerThread extends Thread
{
    public static JsonBasedHudEntry topJsonBasedHudEntry, recentJsonBasedHudEntry;
    final int    interval;
    final String channel;
    final URL    donationsUrl;
    final URL    subsUrl;
    public boolean firstrun = true;
    JsonArray latest;
    HashMap<String, String> subs = new HashMap<>();

    public DonationCheckerThread() throws MalformedURLException
    {
        super(DonationCheckerThread.class.getSimpleName());
        this.interval = Pay2Spawn.getConfig().interval;
        this.channel = Pay2Spawn.getConfig().channel;
        this.donationsUrl = new URL("http://www.streamdonations.net/api/poll?channel=" + channel + "&key=" + Pay2Spawn.getConfig().API_Key);
        this.subsUrl = new URL("https://api.twitch.tv/kraken/channels/" + channel + "/subscriptions?limit=100&oauth_token=" + Pay2Spawn.getConfig().twitchToken);

        topJsonBasedHudEntry = new JsonBasedHudEntry("topDonations", 5, 1, 5, "$name: $$amount", "-- Top donations --");
        Hud.INSTANCE.set.add(topJsonBasedHudEntry);
        recentJsonBasedHudEntry = new JsonBasedHudEntry("recentDonations", 5, 2, 5, "$name: $$amount", "-- Recent donations --");
        Hud.INSTANCE.set.add(recentJsonBasedHudEntry);
    }

    ArrayList<String>     doneIDs = new ArrayList<>();
    ArrayList<JsonObject> backlog = new ArrayList<>();

    public synchronized JsonObject getLatestById(int id) throws IndexOutOfBoundsException
    {
        return latest.get(id).getAsJsonObject();
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                if (!Strings.isNullOrEmpty(Pay2Spawn.getConfig().API_Key)) doDonations();
            }
            catch (Exception e)
            {
                Pay2Spawn.getLogger().severe("ERROR TYPE 1: Error while contacting Streamdonations.");
                if (Minecraft.getMinecraft().running) e.printStackTrace();
            }
            try
            {
                if (!Strings.isNullOrEmpty(Pay2Spawn.getConfig().twitchToken)) doSubs();
            }
            catch (Exception e)
            {
                Pay2Spawn.getLogger().severe("ERROR TYPE 1: Error while contacting Twitch api.");
                if (Minecraft.getMinecraft().running) e.printStackTrace();
            }
            firstrun = false;
            doWait(interval);
        }
    }

    private void doDonations() throws Exception
    {
        for (JsonObject donation : backlog) process(donation);

        JsonObject root = JSON_PARSER.parse(Helper.readUrl(donationsUrl)).getAsJsonObject();

        if (root.get("status").getAsString().equals("success"))
        {
            root = JsonNBTHelper.fixNulls(root);
            doFileAndHud(root);
            latest = root.getAsJsonArray("mostRecent");
            for (JsonElement donation : root.getAsJsonArray("mostRecent")) process(donation.getAsJsonObject());
        }
        else
        {
            throw new IllegalArgumentException(root.get("error").getAsString());
        }
    }

    private void doSubs() throws Exception
    {
        HashMap<String, String> newSubs = new HashMap<>();
        JsonObject root = JSON_PARSER.parse(Helper.readUrl(subsUrl)).getAsJsonObject();
        parseSubs(newSubs, root);
        int total = root.getAsJsonPrimitive("_total").getAsInt();
        for (int offset = 100; offset < total; offset += 100)
        {
            root = JSON_PARSER.parse(Helper.readUrl(new URL(subsUrl.toString() + "&offset=" + offset))).getAsJsonObject();
            parseSubs(newSubs, root);
        }

        for (String sub : newSubs.keySet())
        {
            if (!subs.containsKey(sub) && !firstrun)
            {
                JsonObject donation = new JsonObject();
                donation.addProperty("amount", Double.parseDouble(RandomRegistry.solveRandom(DOUBLE, Pay2Spawn.getConfig().subReward)));
                donation.addProperty("note", "");
                donation.addProperty("twitchUsername", newSubs.get(sub));
                try
                {
                    Pay2Spawn.getRewardsDB().process(donation);
                }
                catch (Exception e)
                {
                    Pay2Spawn.getLogger().warning("Error processing a donation.");
                    e.printStackTrace();
                }
            }
        }
        subs = newSubs;
    }

    private void parseSubs(HashMap<String, String> subs, JsonObject object)
    {
        for (JsonElement sub : object.getAsJsonArray("subscriptions"))
        {
            subs.put(sub.getAsJsonObject().getAsJsonObject("user").get("_id").getAsString(), sub.getAsJsonObject().getAsJsonObject("user").get("display_name").getAsString());
        }
    }

    private void process(JsonObject donation)
    {
        if (firstrun) doneIDs.add(donation.get("transactionID").getAsString());
        if (Minecraft.getMinecraft().thePlayer == null || !Pay2Spawn.enable)
        {
            if (!backlog.contains(donation)) backlog.add(donation);
        }
        else if (!doneIDs.contains(donation.get("transactionID").getAsString()))
        {
            doneIDs.add(donation.get("transactionID").getAsString());
            MetricsHelper.totalMoney += donation.get("amount").getAsDouble();
            if (donation.get("amount").getAsDouble() < Pay2Spawn.getConfig().min_donation) return;
            try
            {
                Pay2Spawn.getRewardsDB().process(donation);
            }
            catch (Exception e)
            {
                Pay2Spawn.getLogger().warning("Error processing a donation.");
                e.printStackTrace();
            }
        }
    }

    private void doWait(int time)
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void doFileAndHud(JsonObject root)
    {
        /**
         * Hud
         */
        {
            /**
             * Top
             */
            topJsonBasedHudEntry.strings.clear();
            if (topJsonBasedHudEntry.getPosition() != 0)
            {
                if (!Strings.isNullOrEmpty(topJsonBasedHudEntry.getHeader())) Helper.addWithEmptyLines(topJsonBasedHudEntry.strings, topJsonBasedHudEntry.getHeader());
                for (int i = 0; i < topJsonBasedHudEntry.getAmount() && i < root.getAsJsonArray("top").size(); i++)
                {
                    JsonObject donation = root.getAsJsonArray("top").get(i).getAsJsonObject();
                    if (donation.get("amount").getAsDouble() < Pay2Spawn.getConfig().min_donation) continue;
                    topJsonBasedHudEntry.strings.add(Helper.formatText(topJsonBasedHudEntry.getFormat(), donation, null));
                }
            }
            /**
             * Recent
             */
            recentJsonBasedHudEntry.strings.clear();
            if (recentJsonBasedHudEntry.getPosition() != 0)
            {
                if (!Strings.isNullOrEmpty(recentJsonBasedHudEntry.getHeader())) Helper.addWithEmptyLines(recentJsonBasedHudEntry.strings, recentJsonBasedHudEntry.getHeader());
                for (int i = 0; i < recentJsonBasedHudEntry.getAmount() && i < root.getAsJsonArray("mostRecent").size(); i++)
                {
                    JsonObject donation = root.getAsJsonArray("mostRecent").get(i).getAsJsonObject();
                    if (donation.get("amount").getAsDouble() < Pay2Spawn.getConfig().min_donation) continue;
                    recentJsonBasedHudEntry.strings.add(Helper.formatText(recentJsonBasedHudEntry.getFormat(), donation, null));
                }
            }
        }
        /**
         * File
         */
        {
            P2SConfig.FileSettings fileSettings = Pay2Spawn.getConfig().file;
            /**
             * Top
             */
            if (fileSettings.top != 0)
            {
                try
                {
                    String end = (fileSettings.top == 1 ? "\n" : "");
                    File file = new File(Pay2Spawn.getFolder(), "topList.txt");
                    //file.delete();
                    file.createNewFile();
                    PrintWriter pw = new PrintWriter(file);
                    for (int i = 0; i < fileSettings.top_amount && i < root.getAsJsonArray("top").size(); i++)
                    {
                        if (i == fileSettings.top_amount - 1) end = "";
                        JsonObject donation = root.getAsJsonArray("top").get(i).getAsJsonObject();
                        if (donation.get("amount").getAsDouble() < Pay2Spawn.getConfig().min_donation) continue;
                        pw.print(Helper.formatText(fileSettings.top_format, donation, null) + end);
                    }
                    pw.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            /**
             * Recent
             */
            if (fileSettings.recent != 0)
            {
                try
                {
                    String end = (fileSettings.recent == 1 ? "\n" : "");
                    File file = new File(Pay2Spawn.getFolder(), "recentList.txt");
                    //file.delete();
                    file.createNewFile();

                    PrintWriter pw = new PrintWriter(file);

                    for (int i = 0; i < fileSettings.recent_amount && i < root.getAsJsonArray("mostRecent").size(); i++)
                    {
                        if (i == fileSettings.recent_amount - 1) end = "";
                        JsonObject donation = root.getAsJsonArray("mostRecent").get(i).getAsJsonObject();
                        if (donation.get("amount").getAsDouble() < Pay2Spawn.getConfig().min_donation) continue;
                        pw.print(Helper.formatText(fileSettings.recent_format, donation, null) + end);
                    }
                    pw.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void fakeDonation(double amount)
    {
        JsonObject donation = new JsonObject();
        donation.addProperty(DONATION_AMOUNT, amount);
        donation.addProperty(DONATION_USERNAME, Minecraft.getMinecraft().thePlayer.getDisplayName());
        donation.addProperty(DONATION_NOTE, "");
        Pay2Spawn.getRewardsDB().process(donation);
        Helper.msg(EnumChatFormatting.GOLD + "[P2S] Faking donation of " + amount + ".");
    }

    public static void redonate(int id)
    {
        JsonObject donation = Pay2Spawn.getDonationCheckerThread().getLatestById(id);
        Pay2Spawn.getRewardsDB().process(donation);
        Helper.msg(EnumChatFormatting.GOLD + "[P2S] Redoing " + donation.get(DONATION_USERNAME).getAsString() + "'s donation of " + donation.get(DONATION_AMOUNT).getAsString() + ".");
    }
}
