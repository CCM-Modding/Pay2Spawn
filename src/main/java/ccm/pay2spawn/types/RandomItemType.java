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
import ccm.pay2spawn.random.RandomRegistry;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.gson.JsonObject;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;

public class RandomItemType extends TypeBase
{
    @Override
    public String getName()
    {
        return "randomItem";
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound display = new NBTTagCompound();
        display.setString("Name", "$name");
        tag.setCompoundTag("display", display);
        root.setCompoundTag("tag", tag);

        return root;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        try
        {
            NBTTagCompound nbtTagCompound = pickRandomItemStack().writeToNBT(new NBTTagCompound());

            ItemStack is = pickRandomItemStack();
            for (Object o : dataFromClient.getTags())
            {
                NBTBase tag = (NBTBase) o;
                nbtTagCompound.setTag(tag.getName(), tag);
            }
            is.readFromNBT(nbtTagCompound);
            player.dropPlayerItem(is).delayBeforeCanPickup = 0;
        }
        catch (Exception e)
        {
            Pay2Spawn.getLogger().warning("ItemStack could not be spawned. Does the item exists? JSON: " + JsonNBTHelper.parseNBT(dataFromClient));
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {

    }

    public ItemStack pickRandomItemStack()
    {
        ArrayList itemStacks = new ArrayList();
        for (Item item : Item.itemsList)
        {
            if (item == null) continue;

            item.getSubItems(item.itemID, CreativeTabs.tabAllSearch, itemStacks);
        }

        return (ItemStack) RandomRegistry.getRandomFromSet(itemStacks);
    }
}
