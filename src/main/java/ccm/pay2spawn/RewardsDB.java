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

import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import ccm.pay2spawn.util.Reward;
import com.google.gson.*;

import java.io.*;
import java.util.HashMap;

public class RewardsDB
{
    private final HashMap<Double, Reward> map = new HashMap<>();

    RewardsDB(File file)
    {
        try
        {
            if (file.exists())
            {
                JsonArray rootArray = Helper.PARSER.parse(new FileReader(file)).getAsJsonArray();

                for (JsonElement element : rootArray)
                {
                    Reward reward = new Reward(element.getAsJsonObject());
                    map.put(reward.getAmount(), reward);
                }
            }
            else
            {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
                JsonArray rootArray = new JsonArray();

                JsonObject group = new JsonObject();
                group.addProperty("name", "EXAMPLE");
                group.addProperty("amount", 10);
                group.addProperty("message", "&a[$name donated $$amount]");
                JsonArray rewards = new JsonArray();
                for (TypeBase type : TypeRegistry.getAllTypes())
                {
                    JsonObject element = new JsonObject();
                    element.addProperty("type", type.getName());
                    element.add("data", JsonNBTHelper.parseNBT(type.convertToNBT(type.getExample())));
                    rewards.add(element);
                }
                group.add("rewards", rewards);
                rootArray.add(group);

                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                bw.write(gson.toJson(rootArray));
                bw.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public synchronized boolean process(JsonObject donation)
    {
        if (!map.containsKey(donation.get("amount").getAsDouble()))
        {
            Helper.msg(Helper.formatText(Pay2Spawn.getConfig().messageNoReward, donation));
            return false;
        }
        else
        {
            map.get(donation.get("amount").getAsDouble()).sendToServer(donation);
            return true;
        }
    }
}
