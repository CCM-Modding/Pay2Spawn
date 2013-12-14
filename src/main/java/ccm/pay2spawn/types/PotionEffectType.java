/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
        while (potion == null) potion = Potion.potionTypes[Helper.RANDOM.nextInt(Potion.potionTypes.length)];
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
