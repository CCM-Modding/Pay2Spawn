///*
// * The MIT License (MIT)
// *
// * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy of
// * this software and associated documentation files (the "Software"), to deal in
// * the Software without restriction, including without limitation the rights to
// * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
// * the Software, and to permit persons to whom the Software is furnished to do so,
// * subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
// * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// */
//
//package ccm.pay2spawn.types;
//
//import ccm.pay2spawn.Pay2Spawn;
//import ccm.pay2spawn.permissions.Node;
//import ccm.pay2spawn.random.RandomRegistry;
//import ccm.pay2spawn.types.guis.SoundTypeGui;
//import ccm.pay2spawn.util.Helper;
//import com.google.common.base.Throwables;
//import com.google.gson.JsonObject;
//import cpw.mods.fml.common.FMLCommonHandler;
//import net.minecraft.client.Minecraft;
//
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemRecord;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.server.MinecraftServer;
//
//import net.minecraftforge.common.config.Configuration;
//import net.minecraftforge.common.MinecraftForge;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.lang.reflect.Field;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//
//import static ccm.pay2spawn.util.Constants.*;
//
///**
// * Play a sound based on name
// * Can change pitch and volume relative to 1
// * Also supplies data to sound randomizer
// *
// * @author Dries007
// */
//public class SoundType extends TypeBase
//{
//    public static final String NAME          = "sound";
//    public static final String SOUNDNAME_KEY = "soundName";
//    public static final String VOLUME_KEY    = "volume";
//    public static final String PITCH_KEY     = "pitch";
//    public static final String PLAYTOALL_KEY = "playToAll";
//
//    public static final HashSet<String>         sounds    = new HashSet<>();
//    public static final HashSet<String>         streaming = new HashSet<>();
//    public static final HashSet<String>         all       = new HashSet<>();
//    public static final HashMap<String, String> typeMap   = new HashMap<>();
//
//    static
//    {
//        typeMap.put(SOUNDNAME_KEY, NBTTypes[STRING]);
//        typeMap.put(VOLUME_KEY, NBTTypes[FLOAT]);
//        typeMap.put(PITCH_KEY, NBTTypes[FLOAT]);
//        typeMap.put(PLAYTOALL_KEY, NBTTypes[BYTE]);
//    }
//
//    {
//        MinecraftForge.EVENT_BUS.register(this);
//    }
//
//    @Override
//    public String getName()
//    {
//        return NAME;
//    }
//
//    @Override
//    public NBTTagCompound getExample()
//    {
//        NBTTagCompound nbt = new NBTTagCompound();
//
//        if (!sounds.isEmpty())
//        {
//            nbt.setString(SOUNDNAME_KEY, RandomRegistry.getRandomFromSet(sounds));
//            nbt.setFloat(VOLUME_KEY, 1f);
//            nbt.setFloat(PITCH_KEY, 1f);
//            nbt.setBoolean(PLAYTOALL_KEY, false);
//        }
//
//        return nbt;
//    }
//
//    @Override
//    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
//    {
//        if (dataFromClient.hasKey(PLAYTOALL_KEY) && dataFromClient.getBoolean(PLAYTOALL_KEY)) for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) play((EntityPlayer) o, dataFromClient);
//        else play(player, dataFromClient);
//    }
//
//    private void play(EntityPlayer player, NBTTagCompound dataFromClient)
//    {
//        if (sounds.contains(dataFromClient.getString(SOUNDNAME_KEY))) player.getEntityWorld().playSoundAtEntity(player, dataFromClient.getString(SOUNDNAME_KEY), dataFromClient.getFloat(VOLUME_KEY), dataFromClient.getFloat(PITCH_KEY));
//        else if (streaming.contains(dataFromClient.getString(SOUNDNAME_KEY))) player.getEntityWorld().playAuxSFXAtEntity(null, 1005, (int) player.posX, (int) player.posY - 1, (int) player.posZ, Item.getIdFromItem(ItemRecord.getRecord(dataFromClient.getString(SOUNDNAME_KEY))));
//        else Helper.sendChatToPlayer(player, "[P2S] Unknown sound.");
//    }
//
//    @Override
//    public void doConfig(Configuration configuration)
//    {
//        if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) return;
//        try
//        {
//            for (Object key : ((Map) nameToSoundPoolEntriesMappingField.get(Minecraft.getMinecraft().sndManager.soundPoolSounds)).keySet())
//            {
//                all.add(key.toString());
//                sounds.add(key.toString());
//            }
//
//            for (Object key : ((Map) nameToSoundPoolEntriesMappingField.get(Minecraft.getMinecraft().sndManager.soundPoolStreaming)).keySet())
//            {
//                all.add(key.toString());
//                streaming.add(key.toString());
//            }
//        }
//        catch (IllegalAccessException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void printHelpList(File configFolder)
//    {
//        File file = new File(Pay2Spawn.getFolder(), "SoundsList.txt");
//        try
//        {
//            if (file.exists()) file.delete();
//            file.createNewFile();
//            PrintWriter pw = new PrintWriter(file);
//
//            pw.println("## This is a list of all the sounds you can use in the json file.");
//            pw.println("## Not all of them will work, some are system things that shouldn't be messed with.");
//            pw.println("## This file gets deleted and remade every startup, can be disabled in the config.");
//            pw.println("# Sounds: ");
//            for (String s : sounds) pw.println(s);
//            pw.println("# Streaming: ");
//            for (String s : streaming) pw.println(s);
//
//            pw.close();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void openNewGui(int rewardID, JsonObject data)
//    {
//        new SoundTypeGui(rewardID, getName(), data, typeMap);
//    }
//
//    @Override
//    public Collection<Node> getPermissionNodes()
//    {
//        HashSet<Node> nodes = new HashSet<>();
//        nodes.add(new Node(NAME));
//        return nodes;
//    }
//
//    @Override
//    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
//    {
//        return new Node(NAME);
//    }
//
//    @Override
//    public String replaceInTemplate(String id, JsonObject jsonObject)
//    {
//        switch (id)
//        {
//            case "name":
//                return jsonObject.get(SOUNDNAME_KEY).getAsString().replace(typeMap.get(SOUNDNAME_KEY), "");
//        }
//        return id;
//    }
//}
