package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.permissions.BanHelper;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import static ccm.pay2spawn.util.Constants.JSON_PARSER;

/**
 * Allows testing of rewards
 *
 * @author Dries007
 */
public class TestMessage implements IMessage
{
    private String     name;
    private JsonObject data;

    public TestMessage()
    {

    }

    public TestMessage(String name, JsonObject data)
    {
        this.name = name;
        this.data = data;
    }

    public static void sendToServer(String name, JsonObject data)
    {
        if (Minecraft.getMinecraft().isGamePaused()) Helper.msg(EnumChatFormatting.RED + "Some tests don't work while paused! Use your chat key to lose focus.");
        Pay2Spawn.getSnw().sendToServer(new TestMessage(name, data));
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        name = ByteBufUtils.readUTF8String(buf);
        data = JSON_PARSER.parse(ByteBufUtils.readUTF8String(buf)).getAsJsonObject();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
        ByteBufUtils.writeUTF8String(buf, data.toString());
    }

    public static class Handler implements IMessageHandler<TestMessage, IMessage>
    {
        @Override
        public IMessage onMessage(TestMessage message, MessageContext ctx)
        {
            if (ctx.side.isServer())
            {
                NBTTagCompound rewardData = new NBTTagCompound();
                Helper.sendChatToPlayer(ctx.getServerHandler().playerEntity, "Testing reward " + message.name + ".");
                Pay2Spawn.getLogger().info("Test by " + ctx.getServerHandler().playerEntity.getCommandSenderName() + " Type: " + message.name + " Data: " + message.data);
                TypeBase type = TypeRegistry.getByName(message.name);
                NBTTagCompound nbt = JsonNBTHelper.parseJSON(message.data);

                Node node = type.getPermissionNode(ctx.getServerHandler().playerEntity, nbt);
                if (BanHelper.isBanned(node))
                {
                    Helper.sendChatToPlayer(ctx.getServerHandler().playerEntity, "This node (" + node + ") is banned.", EnumChatFormatting.RED);
                    Pay2Spawn.getLogger().warn(ctx.getServerHandler().playerEntity.getCommandSenderName() + " tried using globally banned node " + node + ".");
                    return null;
                }
                if (PermissionsHandler.needPermCheck(ctx.getServerHandler().playerEntity) && !PermissionsHandler.hasPermissionNode(ctx.getServerHandler().playerEntity, node))
                {
                    Pay2Spawn.getLogger().warn(ctx.getServerHandler().playerEntity.getDisplayName() + " doesn't have perm node " + node.toString());
                    return null;
                }
                type.spawnServerSide(ctx.getServerHandler().playerEntity, nbt, rewardData);
            }
            return null;
        }
    }
}
