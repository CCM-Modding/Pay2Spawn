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
import ccm.pay2spawn.permissions.BanList;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.types.guis.CustomEntityTypeGui;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.Configuration;

import java.util.Collection;
import java.util.HashSet;

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
    public void openNewGui(int rewardID, JsonObject data)
    {
        new CustomEntityTypeGui(rewardID, getName(), data, null);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        for (String s : EntityType.NAMES) nodes.add(new Node(EntityType.NODENAME, s));
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        Entity entity = EntityList.createEntityFromNBT(dataFromClient, player.getEntityWorld());
        return new Node(EntityType.NODENAME, EntityList.getEntityString(entity));
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound tag = new NBTTagCompound();
        Entity entity = EntityList.createEntityByName("Wolf", null);
        entity.writeMountToNBT(tag);
        tag.setBoolean(EntityType.AGRO_KEY, true);
        return tag;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        Entity entity = EntityList.createEntityFromNBT(dataFromClient, player.getEntityWorld());
        double x, y, z;

        y = player.posY + 1;

        x = player.posX + (radius / 2 - RANDOM.nextInt(radius));
        z = player.posZ + (radius / 2 - RANDOM.nextInt(radius));

        if (entity != null)
        {
            if (dataFromClient.getBoolean(EntityType.AGRO_KEY) && entity instanceof EntityLiving) ((EntityLiving) entity).setAttackTarget(player);
            entity.setPosition(x, y, z);
            player.worldObj.spawnEntityInWorld(entity);

            Entity entity1 = entity;
            for (NBTTagCompound tag = dataFromClient; tag.hasKey(EntityType.RIDING_KEY); tag = tag.getCompoundTag(EntityType.RIDING_KEY))
            {
                Entity entity2 = EntityList.createEntityFromNBT(tag.getCompoundTag(EntityType.RIDING_KEY), player.getEntityWorld());

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
                    if (tag.getCompoundTag(EntityType.RIDING_KEY).getBoolean(EntityType.AGRO_KEY) && entity2 instanceof EntityLiving) ((EntityLiving) entity2).setAttackTarget(player);

                    entity2.setPosition(x, y, z);
                    player.worldObj.spawnEntityInWorld(entity2);
                    entity1.mountEntity(entity2);
                }

                entity1 = entity2;
            }
        }
    }
}
