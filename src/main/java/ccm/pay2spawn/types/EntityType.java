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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static ccm.pay2spawn.util.Archive.MODID;

public class EntityType extends TypeBase<String>
{
    private static final String NAME   = "entity";
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
    public String getExample()
    {
        return Helper.getRndEntity();
    }

    @Override
    public NBTTagCompound convertToNBT(String thing)
    {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setString("name", thing);
        return tagCompound;
    }

    @Override
    public String convertFromNBT(NBTTagCompound nbt)
    {
        return nbt.getString("name");
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        double x, y, z;

        y = player.posY + 1;

        x = player.posX + (radius/2 - Helper.RANDOM.nextInt(radius));
        z = player.posZ + (radius/2 - Helper.RANDOM.nextInt(radius));

        Entity entity = EntityList.createEntityByName(dataFromClient.getString("name"), player.getEntityWorld());
        if (entity != null && entity instanceof EntityLivingBase)
        {
            EntityLiving entityliving = (EntityLiving) entity;
            entity.setLocationAndAngles(x, y, z, MathHelper.wrapAngleTo180_float(player.getEntityWorld().rand.nextFloat() * 360.0F), 0.0F);
            entityliving.rotationYawHead = entityliving.rotationYaw;
            entityliving.renderYawOffset = entityliving.rotationYaw;
            entityliving.onSpawnWithEgg(null);
            player.getEntityWorld().spawnEntityInWorld(entity);
            entityliving.playLivingSound();
        }
    }

    @Override
    public void printHelpList(File configFolder)
    {
        File file = new File(configFolder, "EntityList.txt");
        try
        {
            if (file.exists()) file.delete();
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file);

            pw.println("## This is a list of all the entities you can use in the json file.");
            pw.println("## Not all of them will work, some are system things that shouldn't be messed with.");
            pw.println("## This file gets deleted and remade every startup, can be disabled in the config.");

            for (Object key : EntityList.stringToClassMapping.keySet())
            {
                pw.println(key.toString());
            }
            pw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
