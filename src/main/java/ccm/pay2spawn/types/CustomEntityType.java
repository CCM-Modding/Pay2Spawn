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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;

import static ccm.pay2spawn.random.RandomRegistry.RANDOM;
import static ccm.pay2spawn.util.Constants.MODID;

/**
 * A reward for complex custom entities
 * (aka custom nbt based ones)
 *
 * @author Dries007
 */
public class CustomEntityType extends TypeBase
{
    private static final String NAME   = "customeentity";
    private static       int    radius = 10;

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        radius = configuration.get(MODID + "." + NAME, "radius", radius, "The radius in wich the entity is randomly spawed").getInt();
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound tag = new NBTTagCompound();
        Entity entity = EntityList.createEntityByName("Wolf", null);
        entity.writeMountToNBT(tag);
        tag.setBoolean("agro", true);
        return tag;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        double x, y, z;

        y = player.posY + 1;

        x = player.posX + (radius / 2 - RANDOM.nextInt(radius));
        z = player.posZ + (radius / 2 - RANDOM.nextInt(radius));

        Entity entity = EntityList.createEntityFromNBT(dataFromClient, player.getEntityWorld());

        if (entity != null)
        {
            entity.setPosition(x, y, z);
            player.worldObj.spawnEntityInWorld(entity);

            Entity entity1 = entity;
            for (NBTTagCompound tag = dataFromClient; tag.hasKey("Riding"); tag = tag.getCompoundTag("Riding"))
            {
                Entity entity2 = EntityList.createEntityFromNBT(tag.getCompoundTag("Riding"), player.getEntityWorld());

                if (entity2 != null)
                {
                    entity2.setPosition(x, y, z);
                    player.worldObj.spawnEntityInWorld(entity2);
                    entity1.mountEntity(entity2);
                }

                entity1 = entity2;
            }
        }
    }
}
