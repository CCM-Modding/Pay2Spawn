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

import ccm.pay2spawn.types.guis.CustomEntityTypeGui;
import ccm.pay2spawn.types.guis.FireworksTypeGui;
import ccm.pay2spawn.types.guis.ItemTypeGui;
import ccm.pay2spawn.util.EventHandler;
import ccm.pay2spawn.util.JsonNBTHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import static ccm.pay2spawn.util.Constants.CHANNEL_NBT_REQUEST;

public class NbtRequestPacket
{
    public static final byte ITEM     = 0;
    public static final byte ENTITY   = 1;
    public static final byte FIREWORK = 2;
    public static ItemTypeGui         callbackItemTypeGui;
    public static CustomEntityTypeGui callbackCustomEntityTypeGui;
    public static FireworksTypeGui    callbackFireworksTypeGui;

    public static void request(CustomEntityTypeGui instance)
    {
        callbackCustomEntityTypeGui = instance;
        EventHandler.addEntityTracking();
    }

    public static void requestByEntityID(int entityId)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(ENTITY);
            stream.writeInt(entityId);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(CHANNEL_NBT_REQUEST, streambyte.toByteArray()));
    }

    public static void request(FireworksTypeGui instance)
    {
        callbackFireworksTypeGui = instance;
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(FIREWORK);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(CHANNEL_NBT_REQUEST, streambyte.toByteArray()));
    }

    public static void request(ItemTypeGui instance)
    {
        callbackItemTypeGui = instance;
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
                    break;
                case ENTITY:
                    if (FMLCommonHandler.instance().getEffectiveSide().isClient()) callbackCustomEntityTypeGui.serverImport(stream.readUTF());
                    else respondEntity(player, stream.readInt());
                    break;
                case FIREWORK:
                    if (FMLCommonHandler.instance().getEffectiveSide().isClient()) callbackFireworksTypeGui.serverImport(stream.readUTF());
                    else respondFirework(player);
                    break;
            }
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void respondFirework(Player player)
    {
        ItemStack itemStack = ((EntityPlayer) player).inventory.getCurrentItem();
        if (itemStack != null && itemStack.getItem() instanceof ItemFirework)
        {
            ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(streambyte);
            try
            {
                stream.writeByte(FIREWORK);
                stream.writeUTF(JsonNBTHelper.parseNBT(itemStack.writeToNBT(new NBTTagCompound())).toString());
                stream.close();
                streambyte.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_NBT_REQUEST, streambyte.toByteArray()), player);
        }
        else
        {
            ((EntityPlayer) player).sendChatToPlayer(ChatMessageComponent.createFromText(EnumChatFormatting.RED + "You are not holding an ItemFirework..."));
        }
    }

    private static void respondEntity(Player player, int i)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(ENTITY);
            NBTTagCompound nbt = new NBTTagCompound();
            Entity entity = ((EntityPlayer) player).worldObj.getEntityByID(i);
            entity.writeToNBT(nbt);
            entity.writeToNBTOptional(nbt);
            stream.writeUTF(JsonNBTHelper.parseNBT(nbt).toString());
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_NBT_REQUEST, streambyte.toByteArray()), player);
    }

    private static void respondItem(Player player)
    {
        if (((EntityPlayer) player).inventory.getCurrentItem() != null)
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
        else
        {
            ((EntityPlayer) player).sendChatToPlayer(ChatMessageComponent.createFromText(EnumChatFormatting.RED + "You are not holding an item..."));
        }
    }
}
