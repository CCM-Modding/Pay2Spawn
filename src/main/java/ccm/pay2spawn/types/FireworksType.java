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

import com.google.common.base.Throwables;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.lang.reflect.Field;

public class FireworksType extends TypeBase
{
    @Override
    public String getName()
    {
        return "fireworks";
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
        fireworks.setByte("Flight", (byte) 0);

        NBTTagList explosions = new NBTTagList();
        NBTTagCompound explosion = new NBTTagCompound();
        explosion.setByte("Type", (byte) 0);
        explosion.setByte("Flicker", (byte) 0);
        explosion.setByte("Trail", (byte) 0);
        explosion.setIntArray("Colors", new int[] {14188952, 8073150});
        explosions.appendTag(explosion);
        explosion = new NBTTagCompound();
        explosion.setByte("Type", (byte) 1);
        explosion.setByte("Flicker", (byte) 1);
        explosion.setByte("Trail", (byte) 0);
        explosion.setIntArray("Colors", new int[] {14188952, 8073150});
        explosions.appendTag(explosion);
        //        explosion = new NBTTagCompound();
        //        explosion.setByte("Type", (byte) 2);
        //        explosion.setByte("Flicker", (byte) 0);
        //        explosion.setByte("Trail", (byte) 1);
        //        explosion.setIntArray("Colors", new int[] {14188952, 8073150});
        //        explosions.appendTag(explosion);
        //        explosion = new NBTTagCompound();
        //        explosion.setByte("Type", (byte) 3);
        //        explosion.setByte("Flicker", (byte) 1);
        //        explosion.setByte("Trail", (byte) 1);
        //        explosion.setIntArray("Colors", new int[] {14188952, 8073150});
        //        explosions.appendTag(explosion);
        //        explosion = new NBTTagCompound();
        //        explosion.setByte("Type", (byte) 4);
        //        explosion.setByte("Flicker", (byte) 1);
        //        explosion.setByte("Trail", (byte) 1);
        //        explosion.setIntArray("Colors", new int[] {14188952, 8073150});
        //        explosions.appendTag(explosion);
        fireworks.setTag("Explosions", explosions);
        tag.setCompoundTag("Fireworks", fireworks);
        out.setTagCompound(tag);

        tag = out.writeToNBT(new NBTTagCompound());

        tag.setInteger("RADIUS", 10);
        tag.setInteger("AMOUNT", 10);

        return tag;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        ItemStack itemStack = ItemStack.loadItemStackFromNBT(dataFromClient);
        int i = 0;
        NBTTagCompound nbttagcompound1 = itemStack.getTagCompound().getCompoundTag("Fireworks");
        if (nbttagcompound1 != null) i += nbttagcompound1.getByte("Flight");

        try
        {
            int rad = dataFromClient.getInteger("RADIUS");
            for (double dgr = 0; dgr < 2 * Math.PI; dgr += (2 * Math.PI / dataFromClient.getInteger("AMOUNT")))
            {
                EntityFireworkRocket entityfireworkrocket = new EntityFireworkRocket(player.worldObj, player.posX + rad * Math.cos(dgr), player.posY, player.posZ + rad * Math.sin(dgr), itemStack.copy());
                fireworkAgeField.set(entityfireworkrocket, 1);
                lifetimeField.set(entityfireworkrocket, 10 + 10 * i);
                player.worldObj.spawnEntityInWorld(entityfireworkrocket);
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
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
