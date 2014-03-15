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

import ccm.pay2spawn.misc.Reward;
import ccm.pay2spawn.util.Helper;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import static ccm.pay2spawn.util.Constants.*;

/**
 * Packet handler for both sides.
 *
 * @author Dries007
 */
public class PacketHandler implements IPacketHandler
{
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        try
        {
            switch (packet.channel)
            {
                case CHANNEL_STATUS:
                    StatusPacket.reconstruct(packet, player);
                    break;
                case CHANNEL_MESSAGE:
                    Helper.doMessage(packet);
                    break;
                case CHANNEL_REWARD:
                    Reward.reconstruct(packet).spawnOnServer((EntityPlayer) player);
                    break;
                case CHANNEL_TEST:
                    TestPacket.reconstruct(packet, player);
                    break;
                case CHANNEL_NBT_REQUEST:
                    NbtRequestPacket.reconstruct(packet, player);
                    break;
                case CHANNEL_MUSIC:
                    MusicPacket.get(packet.data);
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
