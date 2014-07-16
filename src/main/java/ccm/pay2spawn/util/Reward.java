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
import ccm.pay2spawn.types.TypeRegistry;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.CUSTOMHTML;
import static ccm.pay2spawn.util.Constants.JOINER_COMMA_SPACE;

public class Reward
{
    private String    message;
    private String    name;
    private Double    amount;
    private JsonArray rewards;
    private Integer   countdown;

    public Reward(JsonObject json)
    {
        name = json.get("name").getAsString();
        amount = json.get("amount").getAsDouble();
        message = Helper.formatColors(json.get("message").getAsString());
        rewards = json.getAsJsonArray("rewards");
        try
        {
            countdown = json.get("countdown").getAsInt();
        }
        catch (Exception e)
        {
            countdown = 0;
        }
        /**
         * To try and catch errors in the config file ASAP
         */
        try
        {
            JsonNBTHelper.parseJSON(rewards);
        }
        catch (Exception e)
        {
            Pay2Spawn.getLogger().warn("ERROR TYPE 2: Error in reward " + name + "'s NBT data.");
            Pay2Spawn.getLogger().warn(rewards.toString());
            throw e;
        }
    }

    public Reward(String name, Double amount, JsonArray rewards)
    {
        this.name = name;
        this.amount = amount;
        this.rewards = rewards;
    }

    public String getName()
    {
        return name;
    }

    public Double getAmount()
    {
        return amount;
    }

    public void addToCountdown(Donation donation, boolean addToHUD, Reward reward)
    {
        if (!Strings.isNullOrEmpty(message) && addToHUD) Helper.msg(Helper.formatText(message, donation, reward == null ? this : reward));
        ClientTickHandler.INSTANCE.add(this, donation, addToHUD, reward);
    }

    public Integer getCountdown()
    {
        return countdown;
    }

    public String getMessage()
    {
        return message;
    }

    public String getTypes()
    {
        HashSet<String> types = new HashSet<>();
        for (JsonElement element : rewards) types.add(element.getAsJsonObject().get("type").getAsString());
        return JOINER_COMMA_SPACE.join(types);
    }

    public String getHTML() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        for (JsonElement element : rewards)
        {
            JsonObject object = element.getAsJsonObject();
            if (object.has(CUSTOMHTML) && !Strings.isNullOrEmpty(object.get(CUSTOMHTML).getAsString())) sb.append(object.get(CUSTOMHTML).getAsString());
            else sb.append(TypeRegistry.getByName(object.get("type").getAsString()).getHTML(object.getAsJsonObject("data")));
        }
        return sb.toString();
    }

    public JsonArray getRewards()
    {
        return rewards;
    }

    @Override
    public String toString()
    {
        return "Reward[" + name + ", " + hashCode() + "]";
    }
}
