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

import ccm.pay2spawn.types.guis.LightningTypeGui;
import com.google.gson.JsonObject;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;

import static ccm.pay2spawn.random.RandomRegistry.RANDOM;
import static ccm.pay2spawn.util.JsonNBTHelper.INT;

/**
 * Strikes the world within 1 block of the player
 * (randomness to avoid always striking a full 6 heart hit)
 * <p/>
 * No extra data
 *
 * @author Dries007
 */
public class LightningType extends TypeBase
{
    public static final String SPREAD_KEY = "spread";

    public static final HashMap<String, String> typeMap = new HashMap<>();

    static
    {
        typeMap.put(SPREAD_KEY, NBTBase.NBTTypes[INT]);
    }

    @Override
    public String getName()
    {
        return "lightning";
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(SPREAD_KEY, 2);
        return nbt;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        double X = player.posX, Y = player.posY, Z = player.posZ;

        double spread = dataFromClient.getInteger(SPREAD_KEY) / 2;

        X += (spread - (RANDOM.nextDouble() * spread));
        Z += (spread - (RANDOM.nextDouble() * spread));

        player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new LightningTypeGui(rewardID, getName(), data, typeMap);
    }
}
