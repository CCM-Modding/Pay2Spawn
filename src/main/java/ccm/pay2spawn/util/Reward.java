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

import ccm.pay2spawn.types.TypeRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.*;

public class Reward
{
    private String    message;
    private String    name;
    private Double    amount;
    private JsonArray rewards;

    public Reward(JsonObject json)
    {
        name = json.get("name").getAsString();
        amount = json.get("amount").getAsDouble();
        message = Helper.formatColors(json.get("message").getAsString());
        rewards = json.getAsJsonArray("rewards");
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

    public void sendToServer(JsonObject donation)
    {
        Helper.msg(Helper.formatText(message, donation));
        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(Archive.MODID, toBytes(Helper.formatText(rewards, donation).toString())));
    }

    private byte[] toBytes(String formattedData)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeUTF(name);
            stream.writeDouble(amount);
            stream.writeUTF(formattedData);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return streambyte.toByteArray();
    }

    public static Reward reconstruct(Packet250CustomPayload packet) throws IOException
    {
        ByteArrayInputStream streambyte = new ByteArrayInputStream(packet.data);
        DataInputStream stream = new DataInputStream(streambyte);
        String name = stream.readUTF();
        Double amount = stream.readDouble();
        JsonArray rewards = Helper.PARSER.parse(stream.readUTF()).getAsJsonArray();

        return new Reward(name, amount, rewards);
    }

    public void spawnOnServer(EntityPlayer player)
    {
        for (JsonElement element : rewards)
        {
            JsonObject reward = element.getAsJsonObject();
            TypeRegistry.getByName(reward.get("type").getAsString().toLowerCase()).spawnServerSide(player, JsonNBTHelper.parseJSON(reward.getAsJsonObject("data")));
        }
    }
}
