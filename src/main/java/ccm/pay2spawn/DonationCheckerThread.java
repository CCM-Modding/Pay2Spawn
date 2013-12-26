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

import ccm.pay2spawn.util.EventHandler;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import scala.sys.process.processInternal;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * The thread that does the actual checking with nightdevs donationtracker
 *
 * @author Dries007
 */
public class DonationCheckerThread extends Thread
{
    final int    interval;
    final String channel;
    final String API_Key;
    final String URL;
    boolean firstrun = true;

    public DonationCheckerThread(int interval, String channel, String API_Key)
    {
        super(DonationCheckerThread.class.getSimpleName());
        this.interval = interval;
        this.channel = channel;
        this.API_Key = API_Key;
        this.URL = "http://donationtrack.nightdev.com/api/poll?channel=" + channel + "&key=" + API_Key;
    }

    ArrayList<String> doneIDs = new ArrayList<>();
    ArrayList<JsonObject> backlog = new ArrayList<>();

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                for (JsonObject donation : backlog) process(donation);

                String input = readUrl(URL);
                JsonObject root = JsonNBTHelper.PARSER.parse(input).getAsJsonObject();

                if (root.get("status").getAsString().equals("success"))
                {
                    doFileAndHud(root);
                    for (JsonElement donation : root.getAsJsonArray("mostRecent")) process(donation.getAsJsonObject());
                }
                else
                {
                    throw new IllegalArgumentException(root.get("error").getAsString());
                }

                firstrun = false;
                doWait(interval);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void process(JsonObject donation)
    {
        if (Minecraft.getMinecraft().thePlayer == null || !Pay2Spawn.enable)
        {
            if (!backlog.contains(donation)) backlog.add(donation);
        }
        else if (Pay2Spawn.debug || !doneIDs.contains(donation.get("transactionID").getAsString()))
        {
            doneIDs.add(donation.get("transactionID").getAsString());
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
            EventHandler.TOP.clear();
            P2SConfig.HudSettings hudSettings = Pay2Spawn.getConfig().hud;
            if (hudSettings.top != 0)
            {
                String header = hudSettings.top_header.trim();
                if (!Strings.isNullOrEmpty(header)) EventHandler.TOP.add(header);
                for (int i = 0; i < hudSettings.top_amount; i++)
                {
                    JsonObject donation = root.getAsJsonArray("top").get(i).getAsJsonObject();
                    if (donation.get("amount").getAsDouble() < Pay2Spawn.getConfig().min_donation) continue;
                    EventHandler.TOP.add(Helper.formatText(hudSettings.top_format, donation));
                }
            }
            /**
             * Recent
             */
            EventHandler.RECENT.clear();
            if (hudSettings.recent != 0)
            {
                String header = hudSettings.recent_header.trim();
                if (!Strings.isNullOrEmpty(header)) EventHandler.RECENT.add(header);
                for (int i = 0; i < hudSettings.recent_amount; i++)
                {
                    JsonObject donation = root.getAsJsonArray("mostRecent").get(i).getAsJsonObject();
                    if (donation.get("amount").getAsDouble() < Pay2Spawn.getConfig().min_donation) continue;
                    EventHandler.RECENT.add(Helper.formatText(hudSettings.recent_format, donation));
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
                    for (int i = 0; i < fileSettings.top_amount; i++)
                    {
                        if (i == fileSettings.top_amount - 1) end = "";
                        JsonObject donation = root.getAsJsonArray("top").get(i).getAsJsonObject();
                        if (donation.get("amount").getAsDouble() < Pay2Spawn.getConfig().min_donation) continue;
                        pw.print(Helper.formatText(fileSettings.top_format, donation) + end);
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

                    for (int i = 0; i < fileSettings.recent_amount; i++)
                    {
                        if (i == fileSettings.recent_amount - 1) end = "";
                        JsonObject donation = root.getAsJsonArray("mostRecent").get(i).getAsJsonObject();
                        if (donation.get("amount").getAsDouble() < Pay2Spawn.getConfig().min_donation) continue;
                        pw.print(Helper.formatText(fileSettings.recent_format, donation) + end);
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

    private String readUrl(String urlString) throws Exception
    {
        BufferedReader reader = null;
        try
        {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);

            return buffer.toString();
        }
        finally
        {
            if (reader != null) reader.close();
        }
    }
}
