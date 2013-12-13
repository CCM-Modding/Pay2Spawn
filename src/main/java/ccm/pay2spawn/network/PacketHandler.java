package ccm.pay2spawn.network;

import ccm.pay2spawn.util.Reward;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

public class PacketHandler implements IPacketHandler
{
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        try
        {
            Reward.reconstruct(packet).spawnOnServer((EntityPlayer) player);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
