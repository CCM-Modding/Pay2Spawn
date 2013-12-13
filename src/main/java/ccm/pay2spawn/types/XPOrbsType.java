package ccm.pay2spawn.types;

import ccm.pay2spawn.util.Helper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class XPOrbsType extends TypeBase<NBTTagCompound>
{
    @Override
    public String getName()
    {
        return "xporbs";
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound out = new NBTTagCompound();
        out.setInteger("amoutOfOrbs", 100);
        return out;
    }

    @Override
    public NBTTagCompound convertToNBT(NBTTagCompound thing)
    {
        return thing;
    }

    @Override
    public NBTTagCompound convertFromNBT(NBTTagCompound nbt)
    {
        return nbt;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        for (int i = 0; i < dataFromClient.getInteger("amoutOfOrbs"); i++)
            player.worldObj.spawnEntityInWorld(new EntityXPOrb(player.worldObj, player.posX, player.posY, player.posZ, Helper.RANDOM.nextInt(5) + 1));
    }
}
