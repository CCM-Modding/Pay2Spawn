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

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static ccm.pay2spawn.random.RandomRegistry.RANDOM;
import static ccm.pay2spawn.util.Constants.MODID;

/**
 * A simple entity spawner, can handle:
 * - agroing mobs
 * - custom name tags
 * - entities riding entities to infinity
 *
 * @author Dries007
 */
public class EntityType extends TypeBase
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
    public NBTTagCompound getExample()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("name", "$randomEntity");
        tag.setBoolean("agro", true);
        tag.setString("CustomName", "$name");

        NBTTagCompound tag2 = new NBTTagCompound();
        tag2.setString("name", "$randomEntity");
        tag2.setBoolean("agro", true);
        tag2.setString("CustomName", "$name");

        tag.setCompoundTag("Riding", tag2);

        return tag;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        double x, y, z;

        y = player.posY + 1;

        x = player.posX + (radius / 2 - RANDOM.nextInt(radius));
        z = player.posZ + (radius / 2 - RANDOM.nextInt(radius));

        Entity entity = EntityList.createEntityByName(dataFromClient.getString("name"), player.getEntityWorld());

        if (entity != null)
        {
            if (dataFromClient.getBoolean("agro") && entity instanceof EntityLiving) ((EntityLiving) entity).setAttackTarget(player);
            if (dataFromClient.hasKey("CustomName") && entity instanceof EntityLiving) ((EntityLiving) entity).setCustomNameTag(dataFromClient.getString("CustomName"));
            if (dataFromClient.getCompoundTag("Riding").getBoolean("random") && entity instanceof EntityLiving) ((EntityLiving) entity).onSpawnWithEgg(null);

            entity.setPosition(x, y, z);
            player.getEntityWorld().spawnEntityInWorld(entity);

            Entity entity1 = entity;
            for (NBTTagCompound tag = dataFromClient; tag.hasKey("Riding"); tag = tag.getCompoundTag("Riding"))
            {
                Entity entity2 = EntityList.createEntityByName(tag.getCompoundTag("Riding").getString("name"), player.getEntityWorld());

                if (entity2 != null)
                {
                    if (tag.getCompoundTag("Riding").getBoolean("agro") && entity2 instanceof EntityLiving) ((EntityLiving) entity2).setAttackTarget(player);
                    if (tag.getCompoundTag("Riding").hasKey("CustomName") && entity2 instanceof EntityLiving) ((EntityLiving) entity2).setCustomNameTag(tag.getCompoundTag("Riding").getString("CustomName"));
                    if (tag.getCompoundTag("Riding").getBoolean("random") && entity2 instanceof EntityLiving) ((EntityLiving) entity2).onSpawnWithEgg(null);

                    entity2.setPosition(x, y, z);
                    player.worldObj.spawnEntityInWorld(entity2);
                    entity1.mountEntity(entity2);
                }

                entity1 = entity2;
            }
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

            for (Object key : EntityList.stringToClassMapping.keySet()) pw.println(key.toString());
            pw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {

    }
}
