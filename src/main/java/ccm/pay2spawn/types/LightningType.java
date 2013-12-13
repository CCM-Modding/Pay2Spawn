package ccm.pay2spawn.types;

import ccm.pay2spawn.util.Helper;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class LightningType extends TypeBase<NBTTagCompound>
{
    @Override
    public String getName()
    {
        return "lightning";
    }

    @Override
    public NBTTagCompound getExample()
    {
        return new NBTTagCompound();
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
        double X = player.posX, Y = player.posY, Z = player.posZ;

        X += (0.5 - Helper.RANDOM.nextDouble());
        Z += (0.5 - Helper.RANDOM.nextDouble());

        player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
    }
}
