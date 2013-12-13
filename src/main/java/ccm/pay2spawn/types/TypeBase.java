package ccm.pay2spawn.types;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;

public abstract class TypeBase<T>
{
    public abstract String getName();

    public abstract T getExample();

    public abstract NBTTagCompound convertToNBT(T thing);

    public abstract T convertFromNBT(NBTTagCompound nbt);

    public abstract void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient);

    public void doConfig(Configuration configuration)
    {

    }
}
