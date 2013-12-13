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

import ccm.pay2spawn.Pay2Spawn;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundPool;
import net.minecraft.client.audio.SoundPoolEntry;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class SoundType extends TypeBase<NBTTagCompound>
{
    public SoundType()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getName()
    {
        return "sound";
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setBoolean("ingameSound", true);
        nbt.setString("soundName", "");
        nbt.setFloat("volume", 1f);
        nbt.setFloat("pitch", 1f);

        return nbt;
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
        if (dataFromClient.getBoolean("ingameSound"))
        {
            player.getEntityWorld().playSoundAtEntity(player, dataFromClient.getString("soundName"), dataFromClient.getFloat("volume"), dataFromClient.getFloat("pitch"));
        }
        else
        {
            //player.getEntityWorld().playSoundAtEntity(player);
        }
    }

    @ForgeSubscribe(priority = EventPriority.LOWEST)
    public void soundLoadEvent(SoundLoadEvent event)
    {
        File file = new File(Pay2Spawn.getFolder(), "SoundsList.txt");
        try
        {
            if (file.exists()) file.delete();
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file);

            pw.println("## This is a list of all the sounds you can use in the json file.");
            pw.println("## Not all of them will work, some are system things that shouldn't be messed with.");
            pw.println("## This file gets deleted and remade every startup, can be disabled in the config.");

            for (Object key : ((Map)ReflectionHelper.getPrivateValue(SoundPool.class, event.manager.soundPoolMusic, "nameToSoundPoolEntriesMapping")).keySet())
            {
                pw.println(key);
            }
            for (Object key : ((Map)ReflectionHelper.getPrivateValue(SoundPool.class, event.manager.soundPoolMusic, "nameToSoundPoolEntriesMapping")).keySet())
            {
                pw.println(key);
            }
            for (Object key : ((Map)ReflectionHelper.getPrivateValue(SoundPool.class, event.manager.soundPoolMusic, "nameToSoundPoolEntriesMapping")).keySet())
            {
                pw.println(key);
            }

            pw.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
