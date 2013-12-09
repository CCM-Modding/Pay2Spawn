package ccm.pay2spawn.network;

import ccm.pay2spawn.util.Data;
import ccm.pay2spawn.util.EnumSpawnType;
import ccm.pay2spawn.util.Helper;
import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

public class P2SPacket
{
    private NBTTagCompound root = new NBTTagCompound();

    public P2SPacket(EnumSpawnType type, NBTTagCompound data)
    {
        root.setInteger("type", type.ordinal());
        root.setCompoundTag("data", data);
    }

    public void sendToServer()
    {
        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(Data.MODID, Helper.nbtToByteArray(getNBT())));
    }

    public NBTTagCompound getNBT()
    {
        return root;
    }
}
