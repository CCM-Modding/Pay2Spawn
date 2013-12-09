package ccm.pay2spawn.network;

import ccm.pay2spawn.util.EnumSpawnType;
import ccm.pay2spawn.util.Helper;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

public class PacketHandler implements IPacketHandler
{
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        try
        {
            NBTTagCompound root = Helper.byteArrayToNBT(packet.data);
            EnumSpawnType.values()[root.getInteger("type")].spawnFromData((EntityPlayer) player, root.getCompoundTag("data"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
