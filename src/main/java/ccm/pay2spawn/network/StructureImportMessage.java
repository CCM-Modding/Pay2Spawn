package ccm.pay2spawn.network;

import ccm.pay2spawn.types.guis.StructureTypeGui;
import ccm.pay2spawn.util.JsonNBTHelper;
import ccm.pay2spawn.util.shapes.PointI;
import ccm.pay2spawn.util.shapes.Shapes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import static ccm.pay2spawn.types.StructureType.*;
import static ccm.pay2spawn.util.Constants.JSON_PARSER;

/**
 * Reads all blockID, metadata and NBT from a list of points
 *
 * @author Dries007
 */
public class StructureImportMessage implements IMessage
{
    int x, y, z;
    JsonArray jsonArray;

    public StructureImportMessage()
    {
    }

    public StructureImportMessage(int x, int y, int z, JsonArray jsonArray)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.jsonArray = jsonArray;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        jsonArray = JSON_PARSER.parse(ByteBufUtils.readUTF8String(buf)).getAsJsonArray();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, jsonArray.toString());
    }

    public static class Handler implements IMessageHandler<StructureImportMessage, IMessage>
    {
        @Override
        public IMessage onMessage(StructureImportMessage message, MessageContext ctx)
        {
            if (ctx.side.isServer())
            {
                JsonArray newArray = new JsonArray();

                for (JsonElement jsonElement : message.jsonArray)
                {
                    NBTTagCompound shapeNbt = JsonNBTHelper.parseJSON(jsonElement.getAsJsonObject());
                    // Can only be a point anyways
                    PointI point = (PointI)Shapes.loadShape(shapeNbt);
                    World world = ctx.getServerHandler().playerEntity.worldObj;
                    int x = message.x + point.getX(), y = message.y + point.getY(), z = message.z + point.getZ();

                    // Set up the correct block data
                    NBTTagList blockDataNbt = new NBTTagList();
                    {
                        NBTTagCompound compound = new NBTTagCompound();

                        // BlockID
                        compound.setInteger(BLOCKID_KEY, Block.getIdFromBlock(world.getBlock(x, y, z)));

                        // metaData
                        int meta = world.getBlockMetadata(x, y, z);
                        if (meta != 0) compound.setInteger(META_KEY, meta);

                        // TileEntity
                        TileEntity te = world.getTileEntity(x, y, z);
                        if (te != null)
                        {
                            NBTTagCompound teNbt = new NBTTagCompound();
                            te.writeToNBT(teNbt);
                            teNbt.removeTag("x");
                            teNbt.removeTag("y");
                            teNbt.removeTag("z");
                            compound.setTag(TEDATA_KEY, teNbt);
                        }

                        blockDataNbt.appendTag(compound);
                    }
                    shapeNbt.setTag(BLOCKDATA_KEY, blockDataNbt);

                    // Add to new array
                    newArray.add(JsonNBTHelper.parseNBT(shapeNbt));
                }

                return new StructureImportMessage(message.x, message.y, message.z, newArray);
            }
            else
            {
                StructureTypeGui.importCallback(message.jsonArray);
                return null;
            }
        }
    }
}
