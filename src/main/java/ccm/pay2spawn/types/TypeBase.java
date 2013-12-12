package ccm.pay2spawn.types;

import ccm.pay2spawn.network.P2SPacket;
import ccm.pay2spawn.util.Helper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;

import static ccm.pay2spawn.util.Archive.MODID;

public abstract class TypeBase<T>
{
    public static final String CONFIGCAT_MESSAGE = MODID + ".messages";
    public int    amountOfRewards;
    public int    avgPrice;
    public double maxPrice;
    public double minPrice;
    public int    totalPrice;

    public void send(NBTTagCompound data)
    {
        new P2SPacket(this, data).sendToServer();
    }

    public void doMessage(String name, String amount, String spawned)
    {
        Helper.msg(this.getMessageTemplate().replace("$name", name).replace("$amount", amount).replace("$spawned", spawned));
    }

    public abstract String getName();

    public abstract String getMessageTemplate();

    public abstract void doConfig(Configuration configuration);

    public abstract T getExample();

    public abstract NBTTagCompound convertToNBT(T thing);

    public abstract T convertFromNBT(NBTTagCompound nbt);

    public abstract void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient);

    public abstract void sendToServer(String name, String amount, NBTTagCompound dataFromDB);

    public int getAmountOfRewards()
    {
        return amountOfRewards;
    }

    public int getAvgPrice()
    {
        return avgPrice;
    }

    public double getMaxPrice()
    {
        return maxPrice;
    }

    public double getMinPrice()
    {
        return minPrice;
    }
}
