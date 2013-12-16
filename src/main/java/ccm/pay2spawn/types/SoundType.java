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
import com.google.common.base.Throwables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundPool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;

/**
 * Play a sound based on name
 * Can change pitch and volume relative to 1
 * Also supplies data to sound randomizer
 *
 * @author Dries007
 */
public class SoundType extends TypeBase
{
    public static final HashSet<String> music     = new HashSet<>();
    public static final HashSet<String> sounds    = new HashSet<>();
    public static final HashSet<String> streaming = new HashSet<>();
    public static final HashSet<String> all       = new HashSet<>();

    @Override
    public String getName()
    {
        return "sound";
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setString("soundName", "mob.creeper.say");
        nbt.setFloat("volume", 1f);
        nbt.setFloat("pitch", 1f);

        return nbt;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        player.getEntityWorld().playSoundAtEntity(player, dataFromClient.getString("soundName"), dataFromClient.getFloat("volume"), dataFromClient.getFloat("pitch"));
    }

    @Override
    public void doConfig(Configuration config)
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
            pw.println("# Music: ");
            for (Object key : ((Map) nameToSoundPoolEntriesMappingField.get(Minecraft.getMinecraft().sndManager.soundPoolMusic)).keySet())
            {
                all.add(key.toString());
                music.add(key.toString());
                pw.println(key);
            }
            pw.println("# Sounds: ");
            for (Object key : ((Map) nameToSoundPoolEntriesMappingField.get(Minecraft.getMinecraft().sndManager.soundPoolSounds)).keySet())
            {
                all.add(key.toString());
                sounds.add(key.toString());
                pw.println(key);
            }
            pw.println("# Streaming: ");
            for (Object key : ((Map) nameToSoundPoolEntriesMappingField.get(Minecraft.getMinecraft().sndManager.soundPoolStreaming)).keySet())
            {
                all.add(key.toString());
                streaming.add(key.toString());
                pw.println(key);
            }

            pw.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Them cheaty ways...
     */
    private static final Field nameToSoundPoolEntriesMappingField = getHackField();

    private static Field getHackField()
    {
        try
        {
            Field f = SoundPool.class.getDeclaredFields()[1];
            f.setAccessible(true);
            return f;
        }
        catch (Throwable t)
        {
            Throwables.propagate(t);
        }
        return null;
    }
}
