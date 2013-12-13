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
import ccm.pay2spawn.util.HudHelper;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.net.URL;

public class DonationCheckerThread extends Thread
{
    final int    interval;
    final String channel;
    final String API_Key;
    final String URL;
    String lastKnownDonation;

    public DonationCheckerThread(int interval, String channel, String API_Key)
    {
        super(DonationCheckerThread.class.getSimpleName());
        this.interval = interval;
        this.channel = channel;
        this.API_Key = API_Key;
        this.URL = "http://donationtrack.nightdev.com/api/poll?channel=" + channel + "&key=" + API_Key;
    }

    @Override
    public void run()
    {
        String message;
        while (true)
        {
            try
            {
                if (Pay2Spawn.debug) lastKnownDonation = "";
                String input = readUrl(URL);
                JsonObject root = Helper.PARSER.parse(input).getAsJsonObject();

                if (root.get("status").getAsString().equals("success"))
                {
                    doFileAndHud(root);
                    go(root.getAsJsonArray("mostRecent"));
                }
                else
                {
                    message = root.get("error").getAsString();
                    break;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    synchronized (this)
                    {
                        this.wait(interval * 1000);
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        throw new IllegalArgumentException(message);
    }

    private void doFileAndHud(JsonObject root)
    {
        HudHelper.reset();
        if (Pay2Spawn.getConfig().hud.top != 0)
        {
            String header = Pay2Spawn.getConfig().hud.top_header.trim();
            if (!Strings.isNullOrEmpty(header)) HudHelper.add(Pay2Spawn.getConfig().hud.top, header);

            for (int i = 0; i < Pay2Spawn.getConfig().hud.top_amount; i++)
            {
                JsonObject donation = root.getAsJsonArray("top").get(i).getAsJsonObject();
                HudHelper.add(Pay2Spawn.getConfig().hud.top, Helper.formatText(Pay2Spawn.getConfig().hud.top_format, donation));
            }
        }
        if (Pay2Spawn.getConfig().file.top != 0)
        {
            try
            {
                String end = (Pay2Spawn.getConfig().file.top == 1 ? "\n" : "");
                File file = new File(Pay2Spawn.getFolder(), "topList.txt");
                //file.delete();
                file.createNewFile();

                PrintWriter pw = new PrintWriter(file);

                for (int i = 0; i < Pay2Spawn.getConfig().file.top_amount; i++)
                {
                    if (i  == Pay2Spawn.getConfig().file.top_amount-1) end = "";
                    JsonObject donation = root.getAsJsonArray("top").get(i).getAsJsonObject();
                    pw.print(Helper.formatText(Pay2Spawn.getConfig().hud.top_format, donation) + end);
                }
                pw.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if (Pay2Spawn.getConfig().hud.recent != 0)
        {
            String header = Pay2Spawn.getConfig().hud.recent_header.trim();
            if (!Strings.isNullOrEmpty(header)) HudHelper.add(Pay2Spawn.getConfig().hud.recent, header);

            for (int i = 0; i < Pay2Spawn.getConfig().hud.recent_amount; i++)
            {
                JsonObject donation = root.getAsJsonArray("mostRecent").get(i).getAsJsonObject();
                HudHelper.add(Pay2Spawn.getConfig().hud.recent, Helper.formatText(Pay2Spawn.getConfig().hud.recent_format, donation));
            }
        }
        if (Pay2Spawn.getConfig().file.recent != 0)
        {
            try
            {
                String end = (Pay2Spawn.getConfig().file.recent == 1 ? "\n" : "");
                File file = new File(Pay2Spawn.getFolder(), "recentList.txt");
                //file.delete();
                file.createNewFile();

                PrintWriter pw = new PrintWriter(file);

                for (int i = 0; i < Pay2Spawn.getConfig().file.recent_amount; i++)
                {
                    if (i  == Pay2Spawn.getConfig().file.recent_amount-1) end = "";
                    JsonObject donation = root.getAsJsonArray("mostRecent").get(i).getAsJsonObject();
                    pw.print(Helper.formatText(Pay2Spawn.getConfig().hud.recent_format, donation) + end);
                }
                pw.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void go(JsonArray mostRecent)
    {
        if (Minecraft.getMinecraft().thePlayer == null) return;
        for (JsonElement aMostRecent : mostRecent)
        {
            JsonObject donation = aMostRecent.getAsJsonObject();

            if (lastKnownDonation == null || lastKnownDonation.equals(donation.get("transactionID").getAsString())) break;

            Pay2Spawn.getRewardsDB().process(donation);
        }
        lastKnownDonation = mostRecent.get(0).getAsJsonObject().get("transactionID").getAsString();
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
