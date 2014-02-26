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

package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.types.MusicType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import javazoom.jl.decoder.JavaLayerException;
import net.minecraft.entity.player.EntityPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import static ccm.pay2spawn.util.Constants.CHANNEL_MUSIC;

public class MusicPacket
{
    public static void send(EntityPlayer player, String name)
    {
        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_MUSIC, name.getBytes()), (Player) player);
    }

    public static void get(byte[] data)
    {
        final String fileName = new String(data);
        File file = new File(MusicType.musicFolder, fileName);

        if (file.exists() && file.isFile()) play(file);
        else
        {
            if (!file.isDirectory()) file = file.getParentFile();

            File[] files = file.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.startsWith(fileName);
                }
            });

            if (files.length == 1)
            {
                play(files[0]);
            }
            else
            {
                Pay2Spawn.getLogger().warning("Multiple matches with music:");
                for (File file1 : files) Pay2Spawn.getLogger().warning(file1.getName());
            }
        }
    }

    private static void play(final File file)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    new javazoom.jl.player.Player(new FileInputStream(file)).play();
                }
                catch (JavaLayerException | FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }, "Pay2Spawn music thread").start();
    }
}
