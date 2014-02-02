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
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.MemoryConnection;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;

import static ccm.pay2spawn.util.Constants.CHANNEL_TEST;
import static ccm.pay2spawn.util.Constants.JSON_PARSER;

public class TestPacket
{
    public static void sendToServer(String name, JsonObject data)
    {
        if (((MemoryConnection) Minecraft.getMinecraft().thePlayer.sendQueue.getNetManager()).isGamePaused()) Helper.msg(EnumChatFormatting.RED + "Some tests don't work while paused! Use your chat key to lose focus.");
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeUTF(name);
            stream.writeUTF(data.toString());

            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(CHANNEL_TEST, streambyte.toByteArray()));
    }

    public static void reconstruct(Packet250CustomPayload packet, Player playerI) throws IOException
    {
        EntityPlayer player = (EntityPlayer) playerI;
        ByteArrayInputStream streamByte = new ByteArrayInputStream(packet.data);
        DataInputStream stream = new DataInputStream(streamByte);
        String name = stream.readUTF();
        String json = stream.readUTF();
        NBTTagCompound rewardData = new NBTTagCompound();
        stream.close();
        streamByte.close();

        Pay2Spawn.getLogger().info(json);

        player.sendChatToPlayer(ChatMessageComponent.createFromText("Testing reward " + name + "."));
        Pay2Spawn.getLogger().info("Test by " + player.getEntityName() + " Type: " + name + " Data: " + json);
        TypeBase type = TypeRegistry.getByName(name);
        NBTTagCompound nbt = JsonNBTHelper.parseJSON(JSON_PARSER.parse(json).getAsJsonObject());

        Node node = type.getPermissionNode(player, nbt);
        if (BanHelper.isBanned(node))
        {
            player.sendChatToPlayer(ChatMessageComponent.createFromText("This node (" + node + ") is banned.").setColor(EnumChatFormatting.RED));
            Pay2Spawn.getLogger().warning(player.getCommandSenderName() + " tried using globally banned node " + node + ".");
            return;
        }
        if (PermissionsHandler.needPermCheck(player) && !PermissionsHandler.hasPermissionNode(player, node))
        {
            Pay2Spawn.getLogger().warning(player.getDisplayName() + " doesn't have perm node " + node.toString());
            return;
        }
        type.spawnServerSide(player, nbt, rewardData);
    }
}
