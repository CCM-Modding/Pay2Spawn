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

import ccm.pay2spawn.util.EventHandler;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.IIHasCallback;
import ccm.pay2spawn.util.JsonNBTHelper;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

public class NbtRequestPacket extends AbstractPacket
{
    public static IIHasCallback callbackItemType;
    public static IIHasCallback callbackCustomEntityType;
    public static IIHasCallback callbackFireworksType;
    private Type    type;
    /**
     * true = request
     * false = response
     */
    private boolean request;
    /**
     * entityId only used for ITEM
     */
    private int     entityId;
    /**
     * Only used when response = false
     */
    private String  response;

    public NbtRequestPacket()
    {

    }

    public NbtRequestPacket(int entityId)
    {
        this.type = Type.ITEM;
        this.request = true;
        this.entityId = entityId;
    }
    public NbtRequestPacket(Type type)
    {
        this.type = type;
        this.request = true;
    }
    public NbtRequestPacket(Type type, String response)
    {
        this.type = type;
        this.request = false;
        this.response = response;
    }

    public static void requestEntity(IIHasCallback instance)
    {
        callbackCustomEntityType = instance;
        EventHandler.addEntityTracking();
    }

    public static void requestByEntityID(int entityId)
    {
        PacketPipeline.PIPELINE.sendToServer(new NbtRequestPacket(entityId));
    }

    public static void requestFirework(IIHasCallback instance)
    {
        callbackFireworksType = instance;
        PacketPipeline.PIPELINE.sendToServer(new NbtRequestPacket(Type.FIREWORK));
    }

    public static void requestItem(IIHasCallback instance)
    {
        callbackItemType = instance;
        PacketPipeline.PIPELINE.sendToServer(new NbtRequestPacket(Type.ITEM));
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeInt(type.ordinal());
        buffer.writeBoolean(request);
        if (request)
        {
            if (type == Type.ENTITY) buffer.writeInt(entityId);
        }
        else
        {
            ByteBufUtils.writeUTF8String(buffer, response);
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        type = Type.values()[buffer.readInt()];
        request = buffer.readBoolean();
        if (request)
        {
            if (type == Type.ENTITY) entityId = buffer.readInt();
        }
        else
        {
            response = ByteBufUtils.readUTF8String(buffer);
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        switch (type)
        {
            case ENTITY:
                callbackItemType.callback(response);
                break;
            case FIREWORK:
                callbackItemType.callback(response);
                break;
            case ITEM:
                callbackItemType.callback(response);
                break;
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        switch (type)
        {
            case ENTITY:
                NBTTagCompound nbt = new NBTTagCompound();
                Entity entity = player.worldObj.getEntityByID(entityId);
                entity.writeToNBT(nbt);
                entity.writeToNBTOptional(nbt);
                PacketPipeline.PIPELINE.sendTo(new NbtRequestPacket(type, JsonNBTHelper.parseNBT(nbt).toString()), (EntityPlayerMP) player);
                break;
            case FIREWORK:
                ItemStack itemStack = player.getHeldItem();
                if (itemStack != null && itemStack.getItem() instanceof ItemFirework)
                {
                    PacketPipeline.PIPELINE.sendTo(new NbtRequestPacket(type, JsonNBTHelper.parseNBT(player.getHeldItem().writeToNBT(new NBTTagCompound())).toString()), (EntityPlayerMP) player);
                }
                else
                {
                    Helper.sendChatToPlayer(player, "You are not holding an ItemFirework...", EnumChatFormatting.RED);
                }
                break;
            case ITEM:
                if (player.getHeldItem() != null)
                {
                    PacketPipeline.PIPELINE.sendTo(new NbtRequestPacket(type, JsonNBTHelper.parseNBT(player.getHeldItem().writeToNBT(new NBTTagCompound())).toString()), (EntityPlayerMP) player);
                }
                else
                {
                    Helper.sendChatToPlayer(player, "You are not holding an item...", EnumChatFormatting.RED);
                }
                break;
        }
    }

    public static enum Type
    {
        ITEM,
        ENTITY,
        FIREWORK
    }
}
