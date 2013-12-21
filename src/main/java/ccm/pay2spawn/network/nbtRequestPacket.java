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
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.types.guis.ItemTypeGui;
import ccm.pay2spawn.util.JsonNBTHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChatMessageComponent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import static ccm.pay2spawn.util.Constants.CHANNEL_NBT_REQUEST;
import static ccm.pay2spawn.util.Constants.CHANNEL_TEST;

public class NbtRequestPacket
{
    public static final byte ITEM = 0;
    public static ItemTypeGui callbackItemTypeGui;

    public static void request(ItemTypeGui itemTypeGui)
    {
        callbackItemTypeGui = itemTypeGui;
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(ITEM);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(CHANNEL_NBT_REQUEST, streambyte.toByteArray()));
    }

    public static void reconstruct(Packet250CustomPayload packet, Player player)
    {
        try
        {
            ByteArrayInputStream streambyte = new ByteArrayInputStream(packet.data);
            DataInputStream stream = new DataInputStream(streambyte);
            switch (stream.readByte())
            {
                case ITEM:
                    if (FMLCommonHandler.instance().getEffectiveSide().isClient()) callbackItemTypeGui.serverImport(stream.readUTF());
                    else respondItem(player);
            }
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void respondItem(Player player)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(ITEM);
            stream.writeUTF(JsonNBTHelper.parseNBT(((EntityPlayer) player).inventory.getCurrentItem().writeToNBT(new NBTTagCompound())).toString());
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_NBT_REQUEST, streambyte.toByteArray()), player);
    }
}
