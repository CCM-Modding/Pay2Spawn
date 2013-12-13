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

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class FireworksType extends TypeBase<NBTTagCompound>
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

        return out.writeToNBT(new NBTTagCompound());
    }

    @Override
    public NBTTagCompound convertToNBT(NBTTagCompound thing)
    {
        return thing;
    }

    @Override
    public NBTTagCompound convertFromNBT(NBTTagCompound nbt)
    {
        return nbt;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        EntityFireworkRocket entityfireworkrocket = new EntityFireworkRocket(player.worldObj, player.posX, player.posY, player.posZ, ItemStack.loadItemStackFromNBT(dataFromClient));
        player.worldObj.spawnEntityInWorld(entityfireworkrocket);
    }
}
