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

import ccm.pay2spawn.types.guis.XPOrbsGui;
import com.google.gson.JsonObject;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;

import static ccm.pay2spawn.random.RandomRegistry.RANDOM;
import static ccm.pay2spawn.util.JsonNBTHelper.*;

public class XPOrbsType extends TypeBase
{
    public static final String AMOUNTOFORBS_KEY = "amoutOfOrbs";

    public static final HashMap<String, String> typeMap = new HashMap<>();

    static
    {
        typeMap.put(AMOUNTOFORBS_KEY, NBTBase.NBTTypes[STRING]);
    }

    @Override
    public String getName()
    {
        return "xporbs";
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound out = new NBTTagCompound();
        out.setInteger(AMOUNTOFORBS_KEY, 100);
        return out;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        for (int i = 0; i < dataFromClient.getInteger(AMOUNTOFORBS_KEY); i++)
        {
            double X = player.posX, Y = player.posY, Z = player.posZ;

            X += (0.5 - RANDOM.nextDouble());
            Z += (0.5 - RANDOM.nextDouble());

            player.worldObj.spawnEntityInWorld(new EntityXPOrb(player.worldObj, X, Y, Z, RANDOM.nextInt(5) + 1));
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new XPOrbsGui(rewardID, getName(), data, typeMap);
    }
}
