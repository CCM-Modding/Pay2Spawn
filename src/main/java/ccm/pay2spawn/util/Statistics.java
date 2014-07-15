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

package ccm.pay2spawn.util;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.hud.Hud;
import ccm.pay2spawn.hud.StatisticsHudEntry;
import ccm.pay2spawn.hud.TotalDonationHudEntry;
import com.google.common.base.Strings;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Statistics
{
    private static File statisticsFile;
    private static NBTTagCompound root = new NBTTagCompound();

    private static HashMap<String, Integer> killsMap       = new HashMap<>();
    private static TreeMap<String, Integer> sortedKillsMap = new TreeMap<>(new ValueComparator(killsMap));

    private static HashMap<String, Integer> spawnsMap       = new HashMap<>();
    private static TreeMap<String, Integer> sortedSpawnsMap = new TreeMap<>(new ValueComparator(spawnsMap));

    private static StatisticsHudEntry spawnsStatisticsHudEntry;
    private static TotalDonationHudEntry totalDonationHudEntry;

    private Statistics() {}

    public static void addToDonationAmount(double amount)
    {
        MetricsHelper.totalMoney += amount;
        totalDonationHudEntry.addToDonationamount(amount);
    }

    public static void handleKill(NBTTagCompound data)
    {
        Pay2Spawn.getLogger().warn("Debug kill data:" + JsonNBTHelper.parseNBT(data).toString());
        String name = data.getString("Reward");

        sortedKillsMap.clear();

        Integer i = killsMap.get(name);
        if (i == null) i = 0;
        killsMap.put(name, i + 1);

        sortedKillsMap.putAll(killsMap);

        save();
    }

    public static void handleSpawn(String name)
    {
        sortedSpawnsMap.clear();
        Integer i = spawnsMap.get(name);
        if (i == null) i = 0;
        spawnsMap.put(name, i + 1);

        sortedSpawnsMap.putAll(spawnsMap);

        update(sortedSpawnsMap, spawnsStatisticsHudEntry);

        save();
    }

    public static void preInit() throws IOException
    {
        statisticsFile = new File(Pay2Spawn.getFolder(), "Statistics.dat");
        if (statisticsFile.exists())
        {
            root = CompressedStreamTools.read(statisticsFile);
            if (root.hasKey("kills"))
            {
                for (Object tagName : root.getCompoundTag("kills").func_150296_c())
                {
                    NBTBase tag = root.getCompoundTag("kills").getTag(tagName.toString());
                    if (tag instanceof NBTTagInt)
                    {
                        killsMap.put(tagName.toString(), ((NBTTagInt) tag).func_150287_d());
                    }
                }
                sortedKillsMap.putAll(killsMap);
            }

            if (root.hasKey("spawns"))
            {
                for (Object tagName : root.getCompoundTag("spawns").func_150296_c())
                {
                    NBTBase tag = root.getCompoundTag("spawns").getTag(tagName.toString());
                    if (tag instanceof NBTTagInt)
                    {
                        spawnsMap.put(tagName.toString(), ((NBTTagInt) tag).func_150287_d());
                    }
                }
                sortedSpawnsMap.putAll(spawnsMap);
            }
        }

        spawnsStatisticsHudEntry = new StatisticsHudEntry("topSpawned", -1, 2, 5, "$amount x $name", "-- Top spawned rewards: --");
        Hud.INSTANCE.set.add(spawnsStatisticsHudEntry);
        update(sortedSpawnsMap, spawnsStatisticsHudEntry);

        totalDonationHudEntry = new TotalDonationHudEntry("totalDonation", 1, "Total amount donated: $$amount", root.hasKey("donated") ? root.getDouble("donated") : 0);
        Hud.INSTANCE.set.add(totalDonationHudEntry);
    }

    private static void update(TreeMap<String, Integer> map, StatisticsHudEntry hudEntry)
    {
        if (map == null || hudEntry == null) return;
        int i = 0;
        hudEntry.strings.clear();
        if (!Strings.isNullOrEmpty(hudEntry.getHeader())) Helper.addWithEmptyLines(hudEntry.strings, hudEntry.getHeader());
        for (Map.Entry<String, Integer> entry : map.entrySet())
        {
            if (i > hudEntry.getAmount()) break;
            i++;

            String key = entry.getKey();
            Integer value = entry.getValue();

            hudEntry.strings.add(hudEntry.getFormat().replace("$name", key).replace("$amount", value.toString()));
        }
    }

    public static void save()
    {
        NBTTagCompound kills = new NBTTagCompound();
        for (String name : killsMap.keySet())
        {
            kills.setInteger(name, killsMap.get(name));
        }
        root.setTag("kills", kills);

        NBTTagCompound spawns = new NBTTagCompound();
        for (String name : spawnsMap.keySet())
        {
            spawns.setInteger(name, spawnsMap.get(name));
        }
        root.setTag("spawns", spawns);
        root.setDouble("donated", totalDonationHudEntry.getDonated());

        try
        {
            CompressedStreamTools.write(root, statisticsFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    static class ValueComparator implements Comparator<String>
    {
        Map<String, Integer> base;

        public ValueComparator(Map<String, Integer> base)
        {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(String a, String b)
        {
            if (base.get(a) >= base.get(b))
            {
                return -1;
            }
            else
            {
                return 1;
            } // returning 0 would merge keys
        }
    }
}
