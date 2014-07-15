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
import ccm.pay2spawn.network.MessageMessage;
import ccm.pay2spawn.random.RandomRegistry;
import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.Collection;
import java.util.Set;

import static ccm.pay2spawn.util.Constants.GSON;
import static ccm.pay2spawn.util.Constants.JSON_PARSER;

/**
 * The rewards database
 *
 * @author Dries007
 * @see ccm.pay2spawn.Pay2Spawn#getRewardsDB()
 */
public class RewardsDB
{
    private final HashMultimap<Double, Reward> map = HashMultimap.create();
    public boolean editable;

    public RewardsDB(String input)
    {
        editable = false;
        JsonArray rootArray = JSON_PARSER.parse(input).getAsJsonArray();

        for (JsonElement element : rootArray)
        {
            Reward reward = new Reward(element.getAsJsonObject());
            map.put(reward.getAmount(), reward);
        }
    }

    public RewardsDB(File file)
    {
        editable = true;
        try
        {
            if (file.exists())
            {
                try
                {
                    JsonArray rootArray = JSON_PARSER.parse(new FileReader(file)).getAsJsonArray();

                    for (JsonElement element : rootArray)
                    {
                        Reward reward = new Reward(element.getAsJsonObject());
                        map.put(reward.getAmount(), reward);
                    }
                }
                catch (Exception e)
                {
                    Pay2Spawn.getLogger().warn("ERROR TYPE 2: There is an error in your config file.");
                    e.printStackTrace();
                }
            }
            else
            {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
                JsonArray rootArray = new JsonArray();

                JsonObject group = new JsonObject();
                group.addProperty("name", "EXAMPLE");
                group.addProperty("amount", 2);
                group.addProperty("countdown", 10);
                group.addProperty("message", "&a[$name donated $$amount]");
                JsonArray rewards = new JsonArray();
                for (TypeBase type : TypeRegistry.getAllTypes())
                {
                    JsonObject element = new JsonObject();
                    element.addProperty("type", type.getName());
                    //noinspection unchecked
                    element.add("data", JsonNBTHelper.parseNBT(type.getExample()));
                    rewards.add(element);
                }
                group.add("rewards", rewards);
                rootArray.add(group);

                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(GSON.toJson(rootArray));
                bw.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public synchronized void process(Donation donation, boolean msg)
    {
        double highestmatch = 0d;

        Reward reward = null;
        if (map.containsKey(donation.amount)) reward = RandomRegistry.getRandomFromSet(map.get(donation.amount));
        else
        {
            for (double key : map.keySet())
                if (key < donation.amount && highestmatch < key) highestmatch = key;

            if (map.containsKey(highestmatch)) reward = RandomRegistry.getRandomFromSet(map.get(highestmatch));
        }

        if (reward != null)
        {
            Statistics.handleSpawn(reward.getName());
            reward.addToCountdown(donation, true, null);
        }

        /**
         * -1 will always spawn
         */
        if (map.containsKey(-1D))
        {
            RandomRegistry.getRandomFromSet(map.get(-1D)).addToCountdown(donation, false, reward);
        }

        Pay2Spawn.getSnw().sendToServer(new MessageMessage(reward, donation));
    }

    public Set<Double> getAmounts()
    {
        return map.keySet();
    }

    public Collection<Reward> getRewards()
    {
        return map.values();
    }
}
