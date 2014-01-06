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
import ccm.pay2spawn.types.guis.FireworksTypeGui;
import ccm.pay2spawn.util.Helper;
import com.google.common.base.Throwables;
import com.google.gson.JsonObject;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.*;

public class FireworksType extends TypeBase
{
    public static final String NODENAME = "fireworks";

    public static final String FLIGHT_KEY  = "Flight";
    public static final String TYPE_KEY    = "Type";
    public static final String FLICKER_KEY = "Flicker";
    public static final String TRAIL_KEY   = "Trail";
    public static final String COLORS_KEY  = "Colors";

    public static final String EXPLOSIONS_KEY = "Explosions";
    public static final String FIREWORKS_KEY  = "Fireworks";

    public static final String RIDETHISMOB_KEY = "RideThisMob";

    public static final String RADIUS_KEY = "RADIUS";
    public static final String AMOUNT_KEY = "AMOUNT";

    public static final HashMap<String, String> typeMap = new HashMap<>();

    static
    {
        typeMap.put(FLIGHT_KEY, NBTBase.NBTTypes[BYTE]);
        typeMap.put(TYPE_KEY, NBTBase.NBTTypes[BYTE]);
        typeMap.put(FLICKER_KEY, NBTBase.NBTTypes[BYTE]);
        typeMap.put(TRAIL_KEY, NBTBase.NBTTypes[BYTE]);
        typeMap.put(COLORS_KEY, NBTBase.NBTTypes[INT_ARRAY]);

        typeMap.put(RIDETHISMOB_KEY, NBTBase.NBTTypes[BYTE]);
        typeMap.put(RADIUS_KEY, NBTBase.NBTTypes[INT]);
        typeMap.put(AMOUNT_KEY, NBTBase.NBTTypes[INT]);
    }

    @Override
    public String getName()
    {
        return NODENAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        /**
         * YOU CAN'T TOUCH THIS.
         * No srsly. Touch it and you rebuild it from scratch!
         */
        ItemStack out = new ItemStack(Item.firework);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound fireworks = new NBTTagCompound();
        fireworks.setByte(FLIGHT_KEY, (byte) 0);

        NBTTagList explosions = new NBTTagList();
        NBTTagCompound explosion = new NBTTagCompound();
        explosion.setByte(TYPE_KEY, (byte) 0);
        explosion.setByte(FLICKER_KEY, (byte) 0);
        explosion.setByte(TRAIL_KEY, (byte) 0);
        explosion.setIntArray(COLORS_KEY, new int[] {14188952, 8073150});
        explosions.appendTag(explosion);
        explosion = new NBTTagCompound();
        explosion.setByte(TYPE_KEY, (byte) 1);
        explosion.setByte(FLICKER_KEY, (byte) 1);
        explosion.setByte(TRAIL_KEY, (byte) 0);
        explosion.setIntArray(COLORS_KEY, new int[] {14188952, 8073150});
        explosions.appendTag(explosion);
        fireworks.setTag(EXPLOSIONS_KEY, explosions);
        tag.setCompoundTag(FIREWORKS_KEY, fireworks);
        out.setTagCompound(tag);

        tag = out.writeToNBT(new NBTTagCompound());

        tag.setInteger(RADIUS_KEY, 10);
        tag.setInteger(AMOUNT_KEY, 10);

        return tag;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        ItemStack itemStack = ItemStack.loadItemStackFromNBT(dataFromClient);
        int flight = 0;
        NBTTagCompound nbttagcompound1 = itemStack.getTagCompound().getCompoundTag(FIREWORKS_KEY);
        if (nbttagcompound1 != null) flight += nbttagcompound1.getByte(FLIGHT_KEY);

        try
        {
            int rndFirework = RANDOM.nextInt(dataFromClient.getInteger(AMOUNT_KEY));
            int rad = dataFromClient.getInteger(RADIUS_KEY);
            double angle = 2 * Math.PI / dataFromClient.getInteger(AMOUNT_KEY);
            for (int i = 0; i < dataFromClient.getInteger(AMOUNT_KEY); i++)
            {
                EntityFireworkRocket entityfireworkrocket = new EntityFireworkRocket(player.worldObj, player.posX + rad * Math.cos(angle * i), player.posY, player.posZ + rad * Math.sin(angle * i), itemStack.copy());
                fireworkAgeField.set(entityfireworkrocket, 1);
                lifetimeField.set(entityfireworkrocket, 10 + 10 * flight);
                player.worldObj.spawnEntityInWorld(entityfireworkrocket);
                if (i == rndFirework && dataFromClient.hasKey(RIDETHISMOB_KEY) && dataFromClient.getBoolean(RIDETHISMOB_KEY)) player.mountEntity(entityfireworkrocket);
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new FireworksTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(new Node(NODENAME));
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NODENAME);
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        return id;
    }

    private static final Field fireworkAgeField = getHackField(0);
    private static final Field lifetimeField    = getHackField(1);

    private static Field getHackField(int id)
    {
        try
        {
            Field f = EntityFireworkRocket.class.getDeclaredFields()[id];
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
