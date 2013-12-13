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
        {
            double X = player.posX, Y = player.posY, Z = player.posZ;

            X += (0.5 - Helper.RANDOM.nextDouble());
            Z += (0.5 - Helper.RANDOM.nextDouble());

            player.worldObj.spawnEntityInWorld(new EntityXPOrb(player.worldObj, X, Y, Z, Helper.RANDOM.nextInt(5) + 1));
        }
    }
}
