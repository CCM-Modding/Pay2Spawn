package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.permissions.BanHelper;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.util.Donation;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import ccm.pay2spawn.util.Reward;
import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import static ccm.pay2spawn.util.Constants.*;

public class RewardMessage implements IMessage
{
    private JsonArray rewards;
    private NBTTagCompound rewardData = new NBTTagCompound();
    private String formattedData;

    public RewardMessage()
    {

    }

    public RewardMessage(Reward reward, Donation donation, Reward actualReward)
    {
        this.rewards = reward.getRewards();
        this.formattedData = Helper.formatText(rewards, donation, actualReward == null ? reward : actualReward).toString();
        rewardData.setString("name", reward.getName());
        rewardData.setDouble("amount", reward.getAmount());
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        // Cause the ByteBufUtils method doesn't like big strings
        byte[] string = new byte[buf.readInt()];
        buf.readBytes(string);
        rewards = JSON_PARSER.parse(new String(string, Charsets.UTF_8)).getAsJsonArray();

        //rewards = JSON_PARSER.parse(ByteBufUtils.readUTF8String(buf)).getAsJsonArray();
        rewardData = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        // Cause the ByteBufUtils method doesn't like big strings
        buf.writeInt(formattedData.length());
        buf.writeBytes(formattedData.getBytes(Charsets.UTF_8));

        //ByteBufUtils.writeUTF8String(buf, formattedData);
        ByteBufUtils.writeTag(buf, rewardData);
    }

    public static class Handler implements IMessageHandler<RewardMessage, IMessage>
    {
        @Override
        public IMessage onMessage(RewardMessage message, MessageContext ctx)
        {
            if (ctx.side.isServer())
            {
                for (JsonElement element : message.rewards)
                {
                    JsonObject reward = element.getAsJsonObject();
                    try
                    {
                        TypeBase type = TypeRegistry.getByName(reward.get("type").getAsString().toLowerCase());
                        NBTTagCompound nbt = JsonNBTHelper.parseJSON(reward.getAsJsonObject("data"));
                        Node node = type.getPermissionNode(ctx.getServerHandler().playerEntity, nbt);
                        if (BanHelper.isBanned(node))
                        {
                            Helper.sendChatToPlayer(ctx.getServerHandler().playerEntity, "This node (" + node + ") is banned.", EnumChatFormatting.RED);
                            Pay2Spawn.getLogger().warn(ctx.getServerHandler().playerEntity.getCommandSenderName() + " tried using globally banned node " + node + ".");
                            continue;
                        }
                        if (PermissionsHandler.needPermCheck(ctx.getServerHandler().playerEntity) && !PermissionsHandler.hasPermissionNode(ctx.getServerHandler().playerEntity, node))
                        {
                            Pay2Spawn.getLogger().warn(ctx.getServerHandler().playerEntity.getDisplayName() + " doesn't have perm node " + node.toString());
                            continue;
                        }
                        type.spawnServerSide(ctx.getServerHandler().playerEntity, nbt, message.rewardData);
                    }
                    catch (Exception e)
                    {
                        Pay2Spawn.getLogger().warn("ERROR TYPE 3: Error spawning a reward on the server.");
                        Pay2Spawn.getLogger().warn("Type: " + reward.get("type").getAsString().toLowerCase());
                        Pay2Spawn.getLogger().warn("Data: " + reward.getAsJsonObject("data"));
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}
