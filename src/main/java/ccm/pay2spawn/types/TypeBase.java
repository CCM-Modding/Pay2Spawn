package ccm.pay2spawn.types;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;

import java.io.File;

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

    public void printHelpList(File configFolder)
    {

    }
}
