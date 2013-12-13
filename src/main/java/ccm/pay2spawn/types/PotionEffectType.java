package ccm.pay2spawn.types;

import ccm.pay2spawn.util.Helper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class PotionEffectType extends TypeBase<PotionEffect>
{
    private static final String NAME = "potioneffect";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public PotionEffect getExample()
    {
        Potion potion = null;
        while (potion == null)
        {
            potion = Potion.potionTypes[Helper.RANDOM.nextInt(Potion.potionTypes.length)];
        }
        return new PotionEffect(potion.getId(), (int) (Helper.RANDOM.nextDouble() * 1000));
    }

    @Override
    public NBTTagCompound convertToNBT(PotionEffect thing)
    {
        return thing.writeCustomPotionEffectToNBT(new NBTTagCompound());
    }

    @Override
    public PotionEffect convertFromNBT(NBTTagCompound nbt)
    {
        return PotionEffect.readCustomPotionEffectFromNBT(nbt);
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        player.addPotionEffect(convertFromNBT(dataFromClient));
    }
}
