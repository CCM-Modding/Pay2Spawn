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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemType extends TypeBase<ItemStack>
{
    private static final String NAME    = "item";
    private static       String message = "&a[$name donated $amount]&f $spawned given!";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public ItemStack getExample()
    {
        ItemStack is = new ItemStack(Item.appleGold);
        is.setItemName("$name");
        return is;
    }

    @Override
    public NBTTagCompound convertToNBT(ItemStack thing)
    {
        return thing.writeToNBT(new NBTTagCompound());
    }

    @Override
    public ItemStack convertFromNBT(NBTTagCompound nbt)
    {
        return ItemStack.loadItemStackFromNBT(nbt);
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        player.dropPlayerItem(convertFromNBT(dataFromClient)).delayBeforeCanPickup = 0;
    }
}
