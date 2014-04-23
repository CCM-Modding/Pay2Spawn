package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.misc.Reward;
import ccm.pay2spawn.permissions.BanHelper;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import static ccm.pay2spawn.util.Constants.JSON_PARSER;

public class RewardPacket extends AbstractPacket
{
    private JsonArray rewards;
    private NBTTagCompound rewardData = new NBTTagCompound();
    private String formattedData;

    public RewardPacket()
    {

    }

    public RewardPacket(Reward reward, JsonObject donation, Reward actualReward)
    {
        this.rewards = reward.getRewards();
        this.formattedData = Helper.formatText(rewards, donation, actualReward == null ? reward : actualReward).toString();
        rewardData.setString("name", reward.getName());
        rewardData.setDouble("amount", reward.getAmount());
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, formattedData);
        ByteBufUtils.writeTag(buffer, rewardData);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        rewards = JSON_PARSER.parse(ByteBufUtils.readUTF8String(buffer)).getAsJsonArray();
        rewardData = ByteBufUtils.readTag(buffer);
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        // Doesn't happen, ever
    }

    @Override
    public void handleServerSide(EntityPlayer player)
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
                    Helper.sendChatToPlayer(player, "This node (" + node + ") is banned.", EnumChatFormatting.RED);
                    Pay2Spawn.getLogger().warn(player.getCommandSenderName() + " tried using globally banned node " + node + ".");
                    continue;
                }
                if (PermissionsHandler.needPermCheck(player) && !PermissionsHandler.hasPermissionNode(player, node))
                {
                    Pay2Spawn.getLogger().warn(player.getDisplayName() + " doesn't have perm node " + node.toString());
                    continue;
                }
                type.spawnServerSide(player, nbt, rewardData);
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
}
