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
import ccm.pay2spawn.network.StatusPacket;
import ccm.pay2spawn.permissions.BanHelper;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.*;

public class Reward
{
    private String    message;
    private String    name;
    private Double    amount;
    private JsonArray rewards;
    private Integer   countdown;
    private NBTTagCompound rewardData = new NBTTagCompound();

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
            Pay2Spawn.getLogger().severe("ERROR TYPE 2: Error in reward " + name + "'s NBT data.");
            throw e;
        }

        rewardData.setString("name", name);
    }

    public Reward(String name, Double amount, JsonArray rewards)
    {
        this.name = name;
        this.amount = amount;
        this.rewards = rewards;
        this.rewardData.setString("name", name);
    }

    public String getName()
    {
        return name;
    }

    public Double getAmount()
    {
        return amount;
    }

    public void addToCountdown(JsonObject donation, boolean addToHUD, Reward reward)
    {
        if (!Strings.isNullOrEmpty(message) && addToHUD) Helper.msg(Helper.formatText(message, donation, reward == null ? this : reward));
        if (StatusPacket.doesServerHaveMod()) ClientTickHandler.INSTANCE.add(this, donation, addToHUD, reward);
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
        JsonArray rewards = JSON_PARSER.parse(stream.readUTF()).getAsJsonArray();
        stream.close();
        streambyte.close();

        return new Reward(name, amount, rewards);
    }

    public void spawnOnServer(EntityPlayer player)
    {
        for (JsonElement element : rewards)
        {
            JsonObject reward = element.getAsJsonObject();
            try
            {
                TypeBase type = TypeRegistry.getByName(reward.get("type").getAsString().toLowerCase());
                NBTTagCompound nbt = JsonNBTHelper.parseJSON(reward.getAsJsonObject("data"));
                Node node = type.getPermissionNode(player, nbt);
                if (BanHelper.isBanned(node))
                {
                    player.sendChatToPlayer(ChatMessageComponent.createFromText("This node (" + node + ") is banned.").setColor(EnumChatFormatting.RED));
                    Pay2Spawn.getLogger().warning(player.getCommandSenderName() + " tried using globally banned node " + node + ".");
                    continue;
                }
                if (PermissionsHandler.needPermCheck(player) && !PermissionsHandler.hasPermissionNode(player, node))
                {
                    Pay2Spawn.getLogger().warning(player.getDisplayName() + " doesn't have perm node " + node.toString());
                    continue;
                }
                type.spawnServerSide(player, nbt, rewardData);
            }
            catch (Exception e)
            {
                Pay2Spawn.getLogger().severe("ERROR TYPE 3: Error spawning a reward on the server.");
                Pay2Spawn.getLogger().severe("Type: " + reward.get("type").getAsString().toLowerCase());
                Pay2Spawn.getLogger().severe("Data: " + reward.getAsJsonObject("data"));
                e.printStackTrace();
            }
        }
    }

    public void send(JsonObject donation, Reward actualReward)
    {
        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(CHANNEL_REWARD, toBytes(Helper.formatText(rewards, donation, actualReward == null ? this : actualReward).toString())));
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
}
