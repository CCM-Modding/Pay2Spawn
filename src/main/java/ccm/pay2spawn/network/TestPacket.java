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
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.MemoryConnection;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;

import static ccm.pay2spawn.util.Constants.CHANNEL_TEST;

public class TestPacket
{
    public static void sendToServer(String name, JsonObject data)
    {
        if (((MemoryConnection) Minecraft.getMinecraft().thePlayer.sendQueue.getNetManager()).isGamePaused()) Helper.msg(EnumChatFormatting.RED + "Some tests don't work while paused! Use your chat key.");
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

    public static void reconstruct(Packet250CustomPayload packet, Player player) throws IOException
    {
        ByteArrayInputStream streambyte = new ByteArrayInputStream(packet.data);
        DataInputStream stream = new DataInputStream(streambyte);
        String name = stream.readUTF();
        String json = stream.readUTF();
        ((EntityPlayer) player).sendChatToPlayer(ChatMessageComponent.createFromText("Testing reward " + name + "."));
        Pay2Spawn.getLogger().info("Test by " + ((EntityPlayer) player).getEntityName() + " Type: " + name + " Data: " + json);
        TypeRegistry.getByName(name).spawnServerSide((EntityPlayer) player, JsonNBTHelper.parseJSON(JsonNBTHelper.PARSER.parse(json).getAsJsonObject()));
        stream.close();
        streambyte.close();
    }
}