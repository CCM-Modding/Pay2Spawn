package ccm.pay2spawn.network;

import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.util.Archive;
import ccm.pay2spawn.util.Helper;
import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.nbt.NBTTagCompound;

public class P2SPacket
{
    private NBTTagCompound root = new NBTTagCompound();

    public P2SPacket(TypeBase type, NBTTagCompound data)
    {
        root.setString("type", type.getName());
        root.setCompoundTag("data", data);
    }

    public void sendToServer()
    {
        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(Archive.MODID, Helper.nbtToByteArray(getNBT())));
    }

    public NBTTagCompound getNBT()
    {
        return root;
    }
}
