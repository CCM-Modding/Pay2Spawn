///*
// * The MIT License (MIT)
// *
// * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy of
// * this software and associated documentation files (the "Software"), to deal in
// * the Software without restriction, including without limitation the rights to
// * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
// * the Software, and to permit persons to whom the Software is furnished to do so,
// * subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
// * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// */
//
//package ccm.pay2spawn.misc;
//
//import ccm.pay2spawn.Pay2Spawn;
//import ccm.pay2spawn.hud.Hud;
//import ccm.pay2spawn.hud.DonationsBasedHudEntry;
//import ccm.pay2spawn.random.RandomRegistry;
//import ccm.pay2spawn.util.Helper;
//import ccm.pay2spawn.util.JsonNBTHelper;
//import ccm.pay2spawn.util.MetricsHelper;
//import ccm.pay2spawn.util.Statistics;
//import com.google.common.base.Strings;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import net.minecraft.client.Minecraft;
//import net.minecraft.util.EnumChatFormatting;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import static ccm.pay2spawn.util.Constants.*;
//
///**
// * The thread that does the actual checking with nightdevs Streamdonations
// *
// * @author Dries007
// */
//public class DonationCheckerThread extends Thread
//{
//    public static DonationsBasedHudEntry topDonationsBasedHudEntry, recentDonationsBasedHudEntry;
//    final int    interval;
//    final String channel;
//    final URL    donationsUrl;
//    final URL    subsUrl;
//    public boolean firstrun = true;
//    JsonArray latest;
//    HashMap<String, String> subs    = new HashMap<>();
//    ArrayList<String>       doneIDs = new ArrayList<>();
//    ArrayList<JsonObject>   backlog = new ArrayList<>();
//
//    public DonationCheckerThread() throws MalformedURLException
//    {
//        super(DonationCheckerThread.class.getSimpleName());
//        this.interval = Pay2Spawn.getConfig().interval;
//        this.channel = Pay2Spawn.getConfig().channel;
//        this.donationsUrl = new URL("http://www.streamdonations.net/api/poll?channel=" + channel + "&key=" + Pay2Spawn.getConfig().API_Key);
//        this.subsUrl = new URL("https://api.twitch.tv/kraken/channels/" + channel + "/subscriptions?limit=100&oauth_token=" + Pay2Spawn.getConfig().twitchToken);
//
//        topDonationsBasedHudEntry = new DonationsBasedHudEntry("topDonations", 5, 1, 5, "$name: $$amount", "-- Top donations --");
//        Hud.INSTANCE.set.add(topDonationsBasedHudEntry);
//        recentDonationsBasedHudEntry = new DonationsBasedHudEntry("recentDonations", 5, 2, 5, "$name: $$amount", "-- Recent donations --");
//        Hud.INSTANCE.set.add(recentDonationsBasedHudEntry);
//    }
//
//    public static void fakeDonation(double amount)
//    {
//        JsonObject donation = new JsonObject();
//        donation.addProperty(DONATION_AMOUNT, amount);
//        donation.addProperty(DONATION_USERNAME, Minecraft.getMinecraft().thePlayer.getDisplayName());
//        donation.addProperty(DONATION_NOTE, "");
//        Pay2Spawn.getRewardsDB().process(donation, false);
//        Helper.msg(EnumChatFormatting.GOLD + "[P2S] Faking donation of " + amount + ".");
//    }
//
//    public static void redonate(int id)
//    {
//        JsonObject donation = Pay2Spawn.getDonationCheckerThread().getLatestById(id);
//        Pay2Spawn.getRewardsDB().process(donation, false);
//        Helper.msg(EnumChatFormatting.GOLD + "[P2S] Redoing " + donation.get(DONATION_USERNAME).getAsString() + "'s donation of " + donation.get(DONATION_AMOUNT).getAsString() + ".");
//    }
//
//    public synchronized JsonObject getLatestById(int id) throws IndexOutOfBoundsException
//    {
//        return latest.get(id).getAsJsonObject();
//    }
//
//    @Override
//    public void run()
//    {
//        while (true)
//        {
//            try
//            {
//                if (!Strings.isNullOrEmpty(Pay2Spawn.getConfig().API_Key)) doDonations();
//            }
//            catch (Exception e)
//            {
//                Pay2Spawn.getLogger().warn("You can ignore this if you closed mc right before it happened.");
//                Pay2Spawn.getLogger().warn("ERROR TYPE 1: Error while contacting Streamdonations.");
//                if (Minecraft.getMinecraft().isIntegratedServerRunning()) e.printStackTrace();
//            }
//            try
//            {
//                if (!Strings.isNullOrEmpty(Pay2Spawn.getConfig().twitchToken)) doSubs();
//            }
//            catch (Exception e)
//            {
//                Pay2Spawn.getLogger().warn("You can ignore this if you closed mc right before it happened.");
//                Pay2Spawn.getLogger().warn("ERROR TYPE 1: Error while contacting Twitch api.");
//                e.printStackTrace();
//            }
//            firstrun = false;
//            doWait(interval);
//        }
//    }
//
//    private void doDonations() throws Exception
//    {
//        for (JsonObject donation : backlog) process(donation, true);
//
//        JsonObject root = JSON_PARSER.parse(Helper.readUrl(donationsUrl)).getAsJsonObject();
//
//        if (root.get("status").getAsString().equals("success"))
//        {
//            root = JsonNBTHelper.fixNulls(root);
//            doFileAndHud(root);
//            latest = root.getAsJsonArray("mostRecent");
//            for (JsonElement donation : root.getAsJsonArray("mostRecent")) process(donation.getAsJsonObject(), true);
//        }
//        else
//        {
//            throw new IllegalArgumentException(root.get("error").getAsString());
//        }
//    }
//
//    private void doSubs() throws Exception
//    {
//        HashMap<String, String> newSubs = new HashMap<>();
//        JsonObject root = JSON_PARSER.parse(Helper.readUrl(subsUrl)).getAsJsonObject();
//        parseSubs(newSubs, root);
//        int total = root.getAsJsonPrimitive("_total").getAsInt();
//        for (int offset = 100; offset < total; offset += 100)
//        {
//            root = JSON_PARSER.parse(Helper.readUrl(new URL(subsUrl.toString() + "&offset=" + offset))).getAsJsonObject();
//            parseSubs(newSubs, root);
//        }
//
//        for (String sub : newSubs.keySet())
//        {
//            if (!subs.containsKey(sub) && !firstrun)
//            {
//                JsonObject donation = new JsonObject();
//                donation.addProperty("amount", Double.parseDouble(RandomRegistry.solveRandom(DOUBLE, Pay2Spawn.getConfig().subReward)));
//                donation.addProperty("note", "");
//                donation.addProperty("twitchUsername", newSubs.get(sub));
//                try
//                {
//                    Pay2Spawn.getRewardsDB().process(donation, true);
//                }
//                catch (Exception e)
//                {
//                    Pay2Spawn.getLogger().warn("Error processing a donation.");
//                    e.printStackTrace();
//                }
//            }
//        }
//        subs = newSubs;
//    }
//
//    private void parseSubs(HashMap<String, String> subs, JsonObject object)
//    {
//        for (JsonElement sub : object.getAsJsonArray("subscriptions"))
//        {
//            subs.put(sub.getAsJsonObject().getAsJsonObject("user").get("_id").getAsString(), sub.getAsJsonObject().getAsJsonObject("user").get("display_name").getAsString());
//        }
//    }
//
//    private void process(JsonObject donation, boolean msg)
//    {
//        if (firstrun) doneIDs.add(donation.get("transactionID").getAsString());
//        if (Minecraft.getMinecraft().thePlayer == null || !Pay2Spawn.enable)
//        {
//            if (!backlog.contains(donation)) backlog.add(donation);
//        }
//        else if (!doneIDs.contains(donation.get("transactionID").getAsString()))
//        {
//            doneIDs.add(donation.get("transactionID").getAsString());
//            MetricsHelper.totalMoney += donation.get("amount").getAsDouble();
//            Statistics.addToDonationAmount(donation.get("amount").getAsDouble());
//            if (donation.get("amount").getAsDouble() < Pay2Spawn.getConfig().min_donation) return;
//            try
//            {
//                Pay2Spawn.getRewardsDB().process(donation, msg);
//            }
//            catch (Exception e)
//            {
//                Pay2Spawn.getLogger().warn("Error processing a donation.");
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void doWait(int time)
//    {
//        try
//        {
//            synchronized (this)
//            {
//                this.wait(time * 1000);
//            }
//        }
//        catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }
//    }
//}
