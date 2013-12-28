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

package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.util.Helper;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import static ccm.pay2spawn.util.Constants.CHANNEL_REDONATE;

public class RedonatePacket
{
    public static void reconstruct(Packet250CustomPayload packet, Player player)
    {
        try
        {
            ByteArrayInputStream streambyte = new ByteArrayInputStream(packet.data);
            DataInputStream stream = new DataInputStream(streambyte);
            if (stream.readBoolean()) redonatet(stream.readInt());
            else fakeDonation(stream.readDouble());
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void fakeDonation(double amount)
    {
        JsonObject donation = new JsonObject();
        donation.addProperty("amount", amount);
        donation.addProperty("twitchUsername", Minecraft.getMinecraft().thePlayer.getDisplayName());
        donation.addProperty("note", "");
        Pay2Spawn.getRewardsDB().process(donation);
        Helper.msg("[P2S] Faking donation of " + amount + ".");
    }

    private static void redonatet(int i)
    {
        JsonObject donation = Pay2Spawn.getDonationCheckerThread().getLatestById(i);
        Pay2Spawn.getRewardsDB().process(donation);
        Helper.msg("[P2S] Redoing " + donation.get("twitchUsername").getAsString() + "'s donation of " + donation.get("amount").getAsString() + ".");
    }

    public static void send(EntityPlayer player, double amount)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeBoolean(false);
            stream.writeDouble(amount);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_REDONATE, streambyte.toByteArray()), (Player) player);
    }

    public static void send(Player player, int id)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeBoolean(true);
            stream.writeInt(id);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_REDONATE, streambyte.toByteArray()), player);
    }
}
