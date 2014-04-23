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
import ccm.pay2spawn.permissions.BanHelper;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import static ccm.pay2spawn.util.Constants.JSON_PARSER;

public class TestPacket extends AbstractPacket
{
    private String     name;
    private JsonObject data;

    public TestPacket()
    {

    }

    public TestPacket(String name, JsonObject data)
    {
        this.name = name;
        this.data = data;
    }

    public static void sendToServer(String name, JsonObject data)
    {
        if (Minecraft.getMinecraft().isGamePaused()) Helper.msg(EnumChatFormatting.RED + "Some tests don't work while paused! Use your chat key to lose focus.");
        PacketPipeline.PIPELINE.sendToServer(new TestPacket(name, data));
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, name);
        ByteBufUtils.writeUTF8String(buffer, data.toString());
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        name = ByteBufUtils.readUTF8String(buffer);
        data = JSON_PARSER.parse(ByteBufUtils.readUTF8String(buffer)).getAsJsonObject();
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        //Noop
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        NBTTagCompound rewardData = new NBTTagCompound();
        Helper.sendChatToPlayer(player, "Testing reward " + name + ".");
        Pay2Spawn.getLogger().info("Test by " + player.getCommandSenderName() + " Type: " + name + " Data: " + data);
        TypeBase type = TypeRegistry.getByName(name);
        NBTTagCompound nbt = JsonNBTHelper.parseJSON(data);

        Node node = type.getPermissionNode(player, nbt);
        if (BanHelper.isBanned(node))
        {
            Helper.sendChatToPlayer(player, "This node (" + node + ") is banned.", EnumChatFormatting.RED);
            Pay2Spawn.getLogger().warn(player.getCommandSenderName() + " tried using globally banned node " + node + ".");
            return;
        }
        if (PermissionsHandler.needPermCheck(player) && !PermissionsHandler.hasPermissionNode(player, node))
        {
            Pay2Spawn.getLogger().warn(player.getDisplayName() + " doesn't have perm node " + node.toString());
            return;
        }
        type.spawnServerSide(player, nbt, rewardData);
    }
}
