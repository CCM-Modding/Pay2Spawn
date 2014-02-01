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

import ccm.pay2spawn.P2SConfig;
import ccm.pay2spawn.Pay2Spawn;
import com.google.common.base.Strings;
import net.minecraft.nbt.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Statistics
{
    private static File statisticsFile;
    private static NBTTagCompound root = new NBTTagCompound();
    private static HashMap<String, Integer> killsMap = new HashMap<>();
    private static TreeMap<String, Integer> sortedKillsMap = new TreeMap<>(new ValueComparator(killsMap));

    private static HashMap<String, Integer> spawnsMap = new HashMap<>();
    private static TreeMap<String, Integer> sortedSpawnsMap = new TreeMap<>(new ValueComparator(spawnsMap));

    private Statistics() {}

    public static void handleKill(NBTTagCompound data)
    {
        String name = data.getString("Reward");

        sortedKillsMap.clear();

        Integer i = killsMap.get(name);
        if (i == null) i = 0;
        killsMap.put(name, i + 1);

        sortedKillsMap.putAll(killsMap);

        EventHandler.KILLERS.clear();
        P2SConfig.HudSettings hudSettings = Pay2Spawn.getConfig().hud;

        if (hudSettings.top_killers != 0)
        {
            String header = hudSettings.top_killers_header.trim();
            if (!Strings.isNullOrEmpty(header)) Helper.addWithEmptyLines(EventHandler.KILLERS, header);

            Iterator<String> iterator = sortedKillsMap.navigableKeySet().iterator();
            for (i = 0; i < hudSettings.top_killers_amount && iterator.hasNext(); i++)
            {
                String key = iterator.next();
                EventHandler.KILLERS.add(hudSettings.top_killers_format.replace("$name", key).replace("$amount", killsMap.get(key).toString()));
            }
        }

        save();
    }

    public static void handleSpawn(String name)
    {
        sortedSpawnsMap.clear();
        Integer i = spawnsMap.get(name);
        if (i == null) i = 0;
        spawnsMap.put(name, i + 1);

        sortedSpawnsMap.putAll(spawnsMap);

        EventHandler.SPAWNED.clear();
        P2SConfig.HudSettings hudSettings = Pay2Spawn.getConfig().hud;

        if (hudSettings.spawned != 0)
        {
            String header = hudSettings.spawned_header.trim();
            if (!Strings.isNullOrEmpty(header)) Helper.addWithEmptyLines(EventHandler.SPAWNED, header);

            Iterator<String> iterator = sortedSpawnsMap.navigableKeySet().iterator();
            for (i = 0; i < hudSettings.spawned_amount && iterator.hasNext(); i++)
            {
                String key = iterator.next();
                EventHandler.SPAWNED.add(hudSettings.spawned_format.replace("$name", key).replace("$amount", spawnsMap.get(key).toString()));
            }
        }
    }

    public static void preInit() throws IOException
    {
        statisticsFile = new File(Pay2Spawn.getFolder(), "Statistics.dat");
        if (statisticsFile.exists())
        {
            root = CompressedStreamTools.read(statisticsFile);
            if (root.hasKey("kills"))
            {
                for (Object tag : root.getCompoundTag("kills").getTags())
                {
                    if (tag instanceof NBTTagInt)
                    {
                        NBTTagInt tagI = (NBTTagInt) tag;
                        killsMap.put(tagI.getName(), tagI.data);
                    }
                }
                sortedKillsMap.putAll(killsMap);
            }

            if (root.hasKey("spawns"))
            {
                for (Object tag : root.getCompoundTag("spawns").getTags())
                {
                    if (tag instanceof NBTTagInt)
                    {
                        NBTTagInt tagI = (NBTTagInt) tag;
                        spawnsMap.put(tagI.getName(), tagI.data);
                    }
                }
                sortedSpawnsMap.putAll(spawnsMap);
            }

        }

        EventHandler.KILLERS.clear();
        EventHandler.SPAWNED.clear();
        P2SConfig.HudSettings hudSettings = Pay2Spawn.getConfig().hud;
        if (hudSettings.top_killers != 0)
        {
            String header = hudSettings.top_killers_header.trim();
            if (!Strings.isNullOrEmpty(header)) Helper.addWithEmptyLines(EventHandler.KILLERS, header);

            Iterator<String> iterator = sortedKillsMap.navigableKeySet().iterator();
            for (int i = 0; i < hudSettings.top_killers_amount && iterator.hasNext(); i ++)
            {
                String key = iterator.next();
                EventHandler.KILLERS.add(hudSettings.top_killers_format.replace("$name", key).replace("$amount", killsMap.get(key).toString()));
            }
        }

        if (hudSettings.spawned != 0)
        {
            String header = hudSettings.spawned_header.trim();
            if (!Strings.isNullOrEmpty(header)) Helper.addWithEmptyLines(EventHandler.SPAWNED, header);

            Iterator<String> iterator = sortedSpawnsMap.navigableKeySet().iterator();
            for (int i = 0; i < hudSettings.spawned_amount && iterator.hasNext(); i++)
            {
                String key = iterator.next();
                EventHandler.SPAWNED.add(hudSettings.spawned_format.replace("$name", key).replace("$amount", spawnsMap.get(key).toString()));
            }
        }
    }

    public static void save()
    {
        NBTTagCompound kills = new NBTTagCompound();
        for (String name : killsMap.keySet())
        {
            kills.setInteger(name, killsMap.get(name));
        }
        root.setCompoundTag("kills", kills);

        NBTTagCompound spawns = new NBTTagCompound();
        for (String name : spawnsMap.keySet())
        {
            spawns.setInteger(name, spawnsMap.get(name));
        }
        root.setCompoundTag("spawns", kills);

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
