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
import ccm.pay2spawn.permissions.BanHelper;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.types.guis.EntityTypeGui;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static ccm.pay2spawn.random.RandomRegistry.RANDOM;
import static ccm.pay2spawn.util.Constants.MODID;
import static ccm.pay2spawn.util.JsonNBTHelper.BYTE;
import static ccm.pay2spawn.util.JsonNBTHelper.STRING;

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
    private static final String NAME = "entity";

    public static final String ENTITYNAME_KEY = "name";
    public static final String AGRO_KEY       = "agro";
    public static final String CUSTOMNAME_KEY = "CustomName";
    public static final String RIDING_KEY     = "Riding";
    public static final String RANDOM_KEY     = "random";

    public static final HashSet<String>         NAMES    = new HashSet<>();
    public static final HashMap<String, String> typeMap  = new HashMap<>();
    public static final String                  NODENAME = NAME;

    private static int radius = 10;

    static
    {
        typeMap.put(ENTITYNAME_KEY, NBTBase.NBTTypes[STRING]);
        typeMap.put(AGRO_KEY, NBTBase.NBTTypes[BYTE]);
        typeMap.put(CUSTOMNAME_KEY, NBTBase.NBTTypes[STRING]);
        typeMap.put(RANDOM_KEY, NBTBase.NBTTypes[BYTE]);
    }

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
        tag.setString(ENTITYNAME_KEY, "$randomEntity");
        tag.setBoolean(AGRO_KEY, true);
        tag.setBoolean(RANDOM_KEY, true);
        tag.setString(CUSTOMNAME_KEY, "$name");

        NBTTagCompound tag2 = new NBTTagCompound();
        tag2.setString(ENTITYNAME_KEY, "$randomEntity");
        tag2.setBoolean(AGRO_KEY, true);
        tag2.setString(CUSTOMNAME_KEY, "$name");

        tag.setCompoundTag(RIDING_KEY, tag2);

        return tag;
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new EntityTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        for (String s : EntityType.NAMES) nodes.add(new Node(NODENAME, s));
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        Entity entity = EntityList.createEntityFromNBT(dataFromClient, player.getEntityWorld());
        return new Node(NODENAME, EntityList.getEntityString(entity));
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        double x, y, z;

        y = player.posY + 1;

        x = player.posX + (radius / 2 - RANDOM.nextInt(radius));
        z = player.posZ + (radius / 2 - RANDOM.nextInt(radius));

        Entity entity = EntityList.createEntityByName(dataFromClient.getString(ENTITYNAME_KEY), player.getEntityWorld());

        if (entity != null)
        {
            if (dataFromClient.getBoolean(AGRO_KEY) && entity instanceof EntityLiving) ((EntityLiving) entity).setAttackTarget(player);
            if (dataFromClient.hasKey(CUSTOMNAME_KEY) && entity instanceof EntityLiving) ((EntityLiving) entity).setCustomNameTag(dataFromClient.getString(CUSTOMNAME_KEY));
            if (dataFromClient.getCompoundTag(RIDING_KEY).getBoolean(RANDOM_KEY) && entity instanceof EntityLiving) ((EntityLiving) entity).onSpawnWithEgg(null);

            entity.setPosition(x, y, z);
            player.getEntityWorld().spawnEntityInWorld(entity);

            Entity entity1 = entity;
            for (NBTTagCompound tag = dataFromClient; tag.hasKey(RIDING_KEY); tag = tag.getCompoundTag(RIDING_KEY))
            {
                Entity entity2 = EntityList.createEntityByName(tag.getCompoundTag(RIDING_KEY).getString(ENTITYNAME_KEY), player.getEntityWorld());

                Node node = this.getPermissionNode(player, tag.getCompoundTag(EntityType.RIDING_KEY));
                if (BanHelper.isBanned(node))
                {
                    player.sendChatToPlayer(ChatMessageComponent.createFromText("This node (" + node + ") is banned.").setColor(EnumChatFormatting.RED));
                    Pay2Spawn.getLogger().warning(player.getCommandSenderName() + " tried using globally banned node " + node + ".");
                    continue;
                }
                if (PermissionsHandler.needPermCheck(player) && !PermissionsHandler.hasPermissionNode(player, node))
                {
                    Pay2Spawn.getLogger().warning(player.getDisplayName() + " doesn't have perm node " + node.toString());
                    continue;
                }

                if (entity2 != null)
                {
                    if (tag.getCompoundTag(RIDING_KEY).getBoolean(AGRO_KEY) && entity2 instanceof EntityLiving) ((EntityLiving) entity2).setAttackTarget(player);
                    if (tag.getCompoundTag(RIDING_KEY).hasKey(CUSTOMNAME_KEY) && entity2 instanceof EntityLiving) ((EntityLiving) entity2).setCustomNameTag(tag.getCompoundTag(RIDING_KEY).getString(CUSTOMNAME_KEY));
                    if (tag.getCompoundTag(RIDING_KEY).getBoolean(RANDOM_KEY) && entity2 instanceof EntityLiving) ((EntityLiving) entity2).onSpawnWithEgg(null);

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

            for (Object key : EntityList.stringToClassMapping.keySet())
            {
                NAMES.add(key.toString());
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
