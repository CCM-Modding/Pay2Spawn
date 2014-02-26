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
import ccm.pay2spawn.network.MusicPacket;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.types.guis.MusicTypeGui;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.STRING;

public class MusicType extends TypeBase
{
    public static final  String                  SOUND_KEY = "song";
    private static final String                  NAME      = "music";
    public static final  HashMap<String, String> typeMap   = new HashMap<>();
    public static File musicFolder;

    static
    {
        typeMap.put(SOUND_KEY, NBTBase.NBTTypes[STRING]);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(SOUND_KEY, "Rick Astley - Never Gonna Give You Up.mp3");
        return nbt;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        MusicPacket.send(player, dataFromClient.getString(SOUND_KEY));
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new MusicTypeGui(rewardID, NAME, data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        return new HashSet<>();
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NAME, dataFromClient.getString(SOUND_KEY).split(" ")[0]);
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "song":
                return jsonObject.get(SOUND_KEY).getAsString().replace(typeMap.get(SOUND_KEY) + ":", "");
        }
        return id;
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        musicFolder = new File(Pay2Spawn.getFolder(), "music");
        //noinspection ResultOfMethodCallIgnored
        musicFolder.mkdirs();

        try
        {
            File[] list = new File(getClass().getResource("/p2s/music/").toURI()).listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    return !pathname.getName().endsWith(".txt");
                }
            });

            for (File file : list)
            {
                File target = new File(musicFolder, file.getName());
                if (!target.exists())
                {
                    try
                    {
                        Files.copy(file, target);
                    }
                    catch (IOException | NullPointerException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (NullPointerException | URISyntaxException e)
        {
            e.printStackTrace();
        }
    }
}
