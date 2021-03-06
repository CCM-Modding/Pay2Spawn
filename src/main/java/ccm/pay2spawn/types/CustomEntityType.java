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
import ccm.pay2spawn.types.guis.CustomEntityTypeGui;
import ccm.pay2spawn.util.Constants;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.PointD;
import ccm.pay2spawn.util.Vector3;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static ccm.pay2spawn.types.EntityType.*;

/**
 * A reward for complex custom entities
 * (aka custom nbt based ones)
 *
 * @author Dries007
 */
public class CustomEntityType extends TypeBase
{
    private static final String NAME = "customeentity";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new CustomEntityTypeGui(rewardID, getName(), data, EntityType.typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        for (String s : NAMES) nodes.add(new Node(NODENAME, s));
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NODENAME, EntityList.getEntityString(EntityList.createEntityFromNBT(dataFromClient, player.getEntityWorld())));
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "entity":
                StringBuilder sb = new StringBuilder();
                sb.append(jsonObject.get("id").getAsString().replace("STRING:", ""));
                while (jsonObject.has(RIDING_KEY))
                {
                    jsonObject = jsonObject.getAsJsonObject(RIDING_KEY);
                    sb.append(" riding a ").append(jsonObject.get("id").getAsString().replace("STRING:", ""));
                }
                return sb.toString();
        }
        return id;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound tag = new NBTTagCompound();
        Entity entity = EntityList.createEntityByName("Wolf", null);
        entity.writeMountToNBT(tag);
        tag.setBoolean(AGRO_KEY, true);
        return tag;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        if (!dataFromClient.hasKey(SPAWNRADIUS_KEY)) dataFromClient.setInteger(SPAWNRADIUS_KEY, 10);
        ArrayList<PointD> pointDs = new PointD(player).getCylinder(dataFromClient.getInteger(SPAWNRADIUS_KEY), 6);
        NBTTagCompound p2sTag = new NBTTagCompound();
        p2sTag.setString("Type", getName());
        if (rewardData.hasKey("name")) p2sTag.setString("Reward", rewardData.getString("name"));

        int count = 0;
        if (!dataFromClient.hasKey(AMOUNT_KEY)) dataFromClient.setInteger(AMOUNT_KEY, 1);
        for (int i = 0; i < dataFromClient.getInteger(AMOUNT_KEY); i++)
        {
            Entity entity = EntityList.createEntityFromNBT(dataFromClient, player.getEntityWorld());

            if (entity != null)
            {
                count++;
                if (getSpawnLimit() != -1 && count > getSpawnLimit()) break;

                entity.setPosition(player.posX, player.posY, player.posZ);
                Helper.rndSpawnPoint(pointDs, entity);

                if (dataFromClient.getBoolean(AGRO_KEY) && entity instanceof EntityLiving) ((EntityLiving) entity).setAttackTarget(player);

                entity.getEntityData().setTag(Constants.NAME, p2sTag.copy());
                player.worldObj.spawnEntityInWorld(entity);

                Entity entity1 = entity;
                for (NBTTagCompound tag = dataFromClient; tag.hasKey(RIDING_KEY); tag = tag.getCompoundTag(RIDING_KEY))
                {
                    Entity entity2 = EntityList.createEntityFromNBT(tag.getCompoundTag(RIDING_KEY), player.getEntityWorld());

                    Node node = this.getPermissionNode(player, tag.getCompoundTag(RIDING_KEY));
                    if (BanHelper.isBanned(node))
                    {
                        Helper.sendChatToPlayer(player, "This node (" + node + ") is banned.", EnumChatFormatting.RED);
                        Pay2Spawn.getLogger().warn(player.getCommandSenderName() + " tried using globally banned node " + node + ".");
                        continue;
                    }
                    if (PermissionsHandler.needPermCheck(player) && !PermissionsHandler.hasPermissionNode(player, node))
                    {
                        Pay2Spawn.getLogger().warn(player.getDisplayName() + " doesn't have perm node " + node.toString());
                        continue;
                    }

                    if (entity2 != null)
                    {
                        count++;
                        if (getSpawnLimit() != -1 && count > getSpawnLimit()) break;

                        if (tag.getCompoundTag(RIDING_KEY).getBoolean(AGRO_KEY) && entity2 instanceof EntityLiving) ((EntityLiving) entity2).setAttackTarget(player);

                        entity2.setPosition(entity.posX, entity.posY, entity.posZ);
                        entity2.getEntityData().setTag(Constants.NAME, p2sTag.copy());
                        player.worldObj.spawnEntityInWorld(entity2);
                        entity1.mountEntity(entity2);
                        if (tag.getCompoundTag(RIDING_KEY).hasKey(RIDETHISMOB_KEY) && tag.getCompoundTag(RIDING_KEY).getBoolean(RIDETHISMOB_KEY)) player.mountEntity(entity2);
                    }

                    entity1 = entity2;
                }
                if (dataFromClient.hasKey(RIDETHISMOB_KEY) && dataFromClient.getBoolean(RIDETHISMOB_KEY)) player.mountEntity(entity);
                if (dataFromClient.hasKey(THROWTOWARDSPLAYER_KEY) && dataFromClient.getBoolean(THROWTOWARDSPLAYER_KEY))
                {
                    new Vector3(entity, player).normalize().setAsVelocity(entity, 2);
                }
            }
        }
    }
}
