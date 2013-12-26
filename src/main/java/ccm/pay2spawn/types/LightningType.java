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

import ccm.pay2spawn.random.RandomRegistry;
import ccm.pay2spawn.types.guis.LightningTypeGui;
import com.google.gson.JsonObject;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import java.util.HashMap;
import java.util.List;

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
    public static final String TYPE_KEY = "type";

    public static final int PLAYER_ENTITY = 0;
    public static final int NEAREST_ENTITY = 1;
    public static final int RND_ENTITY = 2;
    public static final int RND_SPOT = 3;


    public static final HashMap<String, String> typeMap = new HashMap<>();

    static
    {
        typeMap.put(SPREAD_KEY, NBTBase.NBTTypes[INT]);
        typeMap.put(TYPE_KEY, NBTBase.NBTTypes[INT]);
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
        nbt.setInteger(SPREAD_KEY, 10);
        nbt.setInteger(TYPE_KEY, RND_ENTITY);
        return nbt;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        double spread = dataFromClient.getInteger(SPREAD_KEY);
        double X = player.posX, Y = player.posY - 1, Z = player.posZ;
        if (!dataFromClient.hasKey(TYPE_KEY)) dataFromClient.setInteger(TYPE_KEY, RND_SPOT);

        switch (dataFromClient.getInteger(TYPE_KEY))
        {
            case PLAYER_ENTITY:
            {
                player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
                break;
            }
            case NEAREST_ENTITY:
            {
                AxisAlignedBB AABB = AxisAlignedBB.getAABBPool().getAABB(X - spread, Y - spread, Z - spread, X + spread, Y + spread, Z + spread);
                Entity entity = player.getEntityWorld().findNearestEntityWithinAABB(EntityLiving.class, AABB, player);
                player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), entity.posX, entity.posY, entity.posZ));
                break;
            }
            case RND_SPOT:
            {
                X += (spread - (RANDOM.nextDouble() * spread));
                Z += (spread - (RANDOM.nextDouble() * spread));
                player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
                break;
            }
            case RND_ENTITY:
            {
                IEntitySelector iEntitySelector = new IEntitySelector()
                {
                    @Override
                    public boolean isEntityApplicable(Entity entity)
                    {
                        return entity instanceof EntityLiving;
                    }
                };
                AxisAlignedBB AABB = AxisAlignedBB.getAABBPool().getAABB(X - spread, Y - spread, Z - spread, X + spread, Y + spread, Z + spread);
                //noinspection unchecked
                List<EntityLiving> entity = player.getEntityWorld().getEntitiesWithinAABBExcludingEntity(player, AABB, iEntitySelector);
                EntityLiving entityLiving = RandomRegistry.getRandomFromSet(entity);
                player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), entityLiving.posX, entityLiving.posY, entityLiving.posZ));
            }
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new LightningTypeGui(rewardID, getName(), data, typeMap);
    }
}
