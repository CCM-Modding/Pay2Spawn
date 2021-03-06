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

import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.random.RandomRegistry;
import ccm.pay2spawn.types.guis.LightningTypeGui;
import com.google.gson.JsonObject;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static ccm.pay2spawn.util.Constants.*;

/**
 * Strikes the world within 1 block of the player
 * (randomness to avoid always striking a full 6 heart hit)
 * <p/>
 * No extra data
 *
 * @author Dries007
 */
public class LightningType extends TypeBase
{
    public static final String NODENAME   = "lightning";
    public static final String SPREAD_KEY = "spread";
    public static final String TYPE_KEY   = "type";

    public static final int PLAYER_ENTITY  = 0;
    public static final int NEAREST_ENTITY = 1;
    public static final int RND_ENTITY     = 2;
    public static final int RND_SPOT       = 3;


    public static final HashMap<String, String> typeMap = new HashMap<>();

    static
    {
        typeMap.put(SPREAD_KEY, NBTTypes[INT]);
        typeMap.put(TYPE_KEY, NBTTypes[INT]);
    }

    @Override
    public String getName()
    {
        return NODENAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(SPREAD_KEY, 10);
        nbt.setInteger(TYPE_KEY, RND_ENTITY);
        return nbt;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        if (!dataFromClient.hasKey(SPREAD_KEY)) dataFromClient.setInteger(SPREAD_KEY, 10);
        double spread = dataFromClient.getInteger(SPREAD_KEY);
        double X = player.posX, Y = player.posY - 1, Z = player.posZ;
        if (!dataFromClient.hasKey(TYPE_KEY)) dataFromClient.setInteger(TYPE_KEY, RND_SPOT);

        switch (dataFromClient.getInteger(TYPE_KEY))
        {
            case PLAYER_ENTITY:
            {
                player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
                break;
            }
            case NEAREST_ENTITY:
            {
                AxisAlignedBB AABB = AxisAlignedBB.getAABBPool().getAABB(X - spread, Y - spread, Z - spread, X + spread, Y + spread, Z + spread);
                Entity entity = player.getEntityWorld().findNearestEntityWithinAABB(EntityLiving.class, AABB, player);
                if (entity != null) player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), entity.posX, entity.posY, entity.posZ));
                else player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
                break;
            }
            case RND_SPOT:
            {
                X += (spread - (RANDOM.nextDouble() * spread * 2));
                Z += (spread - (RANDOM.nextDouble() * spread * 2));
                Y += (3 - RANDOM.nextDouble() * 6);
                player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
                break;
            }
            case RND_ENTITY:
            {
                IEntitySelector iEntitySelector = new IEntitySelector()
                {
                    @Override
                    public boolean isEntityApplicable(Entity entity)
                    {
                        return entity instanceof EntityLiving;
                    }
                };
                AxisAlignedBB AABB = AxisAlignedBB.getAABBPool().getAABB(X - spread, Y - spread, Z - spread, X + spread, Y + spread, Z + spread);
                //noinspection unchecked
                List<EntityLiving> entity = player.getEntityWorld().getEntitiesWithinAABBExcludingEntity(player, AABB, iEntitySelector);
                EntityLiving entityLiving = RandomRegistry.getRandomFromSet(entity);
                if (entityLiving != null) player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), entityLiving.posX, entityLiving.posY, entityLiving.posZ));
                else player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
            }
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new LightningTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(new Node(NODENAME, "player"));
        nodes.add(new Node(NODENAME, "nearest"));
        nodes.add(new Node(NODENAME, "rnd_entity"));
        nodes.add(new Node(NODENAME, "rnd_spot"));
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        if (!dataFromClient.hasKey(TYPE_KEY)) dataFromClient.setInteger(TYPE_KEY, RND_SPOT);
        switch (dataFromClient.getInteger(TYPE_KEY))
        {
            case PLAYER_ENTITY:
                return new Node(NODENAME, "player");
            case NEAREST_ENTITY:
                return new Node(NODENAME, "nearest");
            case RND_SPOT:
                return new Node(NODENAME, "rnd_entity");
            case RND_ENTITY:
                return new Node(NODENAME, "rnd_spot");
            default:
                return new Node(NODENAME, "player");
        }
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "target":
                switch (Integer.parseInt(jsonObject.get(TYPE_KEY).getAsString().split(":", 2)[1]))
                {
                    case PLAYER_ENTITY:
                        return "the streamer";
                    case NEAREST_ENTITY:
                        return "the nearest entity";
                    case RND_SPOT:
                        return "a random near spot";
                    case RND_ENTITY:
                        return "a random near entity";
                }
        }
        return id;
    }
}
