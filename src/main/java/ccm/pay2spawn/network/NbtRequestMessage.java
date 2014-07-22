package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.types.StructureType;
import ccm.pay2spawn.util.EventHandler;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.IIHasCallback;
import ccm.pay2spawn.util.JsonNBTHelper;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class NbtRequestMessage implements IMessage
{
    public static  IIHasCallback callbackItemType;
    public static  IIHasCallback callbackCustomEntityType;
    public static  IIHasCallback callbackFireworksType;
    private static IIHasCallback callbackBlockType;
    private        Type          type;
    /**
     * true = request
     * false = response
     */
    private        boolean       request;
    /**
     * entityId only used for ITEM
     */
    private        int           entityId;
    /**
     * Only used when response = false
     */
    private        String        response;

    private int x, y, z, dim;

    public NbtRequestMessage()
    {

    }

    public NbtRequestMessage(int entityId)
    {
        this.type = Type.ENTITY;
        this.request = true;
        this.entityId = entityId;
    }

    public NbtRequestMessage(Type type)
    {
        this.type = type;
        this.request = true;
    }

    public NbtRequestMessage(Type type, String response)
    {
        this.type = type;
        this.request = false;
        this.response = response;
    }

    public NbtRequestMessage(int x, int y, int z, int dim)
    {
        this.type = Type.BLOCK;
        this.request = true;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
    }

    public static void requestEntity(IIHasCallback instance)
    {
        callbackCustomEntityType = instance;
        EventHandler.addEntityTracking();
    }

    public static void requestByEntityID(int entityId)
    {
        Pay2Spawn.getSnw().sendToServer(new NbtRequestMessage(entityId));
    }

    public static void requestBlock(int x, int y, int z, int dim)
    {
        Pay2Spawn.getSnw().sendToServer(new NbtRequestMessage(x, y, z, dim));
    }

    public static void requestFirework(IIHasCallback instance)
    {
        callbackFireworksType = instance;
        Pay2Spawn.getSnw().sendToServer(new NbtRequestMessage(Type.FIREWORK));
    }

    public static void requestItem(IIHasCallback instance)
    {
        callbackItemType = instance;
        Pay2Spawn.getSnw().sendToServer(new NbtRequestMessage(Type.ITEM));
    }

    public static void requestBlock(IIHasCallback instance)
    {
        callbackBlockType = instance;
        EventHandler.addBlockTracker();
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        type = Type.values()[buf.readInt()];
        request = buf.readBoolean();
        if (request)
        {
            if (type == Type.ENTITY) entityId = buf.readInt();
            if (type == Type.BLOCK)
            {
                x = buf.readInt();
                y = buf.readInt();
                z = buf.readInt();
                dim = buf.readInt();
            }
        }
        else
        {
            response = ByteBufUtils.readUTF8String(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(type.ordinal());
        buf.writeBoolean(request);
        if (request)
        {
            if (type == Type.ENTITY) buf.writeInt(entityId);
            if (type == Type.BLOCK)
            {
                buf.writeInt(x);
                buf.writeInt(y);
                buf.writeInt(z);
                buf.writeInt(dim);
            }
        }
        else
        {
            ByteBufUtils.writeUTF8String(buf, response);
        }
    }

    public static enum Type
    {
        ITEM,
        BLOCK,
        ENTITY,
        FIREWORK
    }

    public static class Handler implements IMessageHandler<NbtRequestMessage, IMessage>
    {
        @Override
        public IMessage onMessage(NbtRequestMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient())
            {
                switch (message.type)
                {
                    case ENTITY:
                        callbackCustomEntityType.callback(message.response);
                        break;
                    case FIREWORK:
                        callbackFireworksType.callback(message.response);
                        break;
                    case ITEM:
                        callbackItemType.callback(message.response);
                        break;
                    case BLOCK:
                        callbackBlockType.callback(message.response);
                        break;
                }
            }
            else
            {
                switch (message.type)
                {
                    case ENTITY:
                        NBTTagCompound nbt = new NBTTagCompound();
                        Entity entity = ctx.getServerHandler().playerEntity.worldObj.getEntityByID(message.entityId);
                        entity.writeToNBT(nbt);
                        entity.writeToNBTOptional(nbt);
                        nbt.setString("id", EntityList.getEntityString(entity));
                        return new NbtRequestMessage(message.type, JsonNBTHelper.parseNBT(nbt).toString());
                    case FIREWORK:
                        ItemStack itemStack = ctx.getServerHandler().playerEntity.getHeldItem();
                        if (itemStack != null && itemStack.getItem() instanceof ItemFirework)
                        {
                            return new NbtRequestMessage(message.type, JsonNBTHelper.parseNBT(ctx.getServerHandler().playerEntity.getHeldItem().writeToNBT(new NBTTagCompound())).toString());
                        }
                        else
                        {
                            Helper.sendChatToPlayer(ctx.getServerHandler().playerEntity, "You are not holding an ItemFirework...", EnumChatFormatting.RED);
                        }
                        break;
                    case ITEM:
                        if (ctx.getServerHandler().playerEntity.getHeldItem() != null)
                        {
                            return new NbtRequestMessage(message.type, JsonNBTHelper.parseNBT(ctx.getServerHandler().playerEntity.getHeldItem().writeToNBT(new NBTTagCompound())).toString());
                        }
                        else
                        {
                            Helper.sendChatToPlayer(ctx.getServerHandler().playerEntity, "You are not holding an item...", EnumChatFormatting.RED);
                        }
                        break;
                    case BLOCK:
                        NBTTagCompound compound = new NBTTagCompound();
                        World world = DimensionManager.getWorld(message.dim);
                        compound.setInteger(StructureType.BLOCKID_KEY, Block.getIdFromBlock(world.getBlock(message.x, message.y, message.z)));
                        compound.setInteger(StructureType.META_KEY, world.getBlockMetadata(message.x, message.y, message.z));
                        TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
                        if (tileEntity != null)
                        {
                            NBTTagCompound te = new NBTTagCompound();
                            tileEntity.writeToNBT(te);
                            compound.setTag(StructureType.TEDATA_KEY, te);
                        }
                        return new NbtRequestMessage(message.type, JsonNBTHelper.parseNBT(compound).toString());
                }
            }
            return null;
        }
    }
}
