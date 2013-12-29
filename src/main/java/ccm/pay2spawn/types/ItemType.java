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
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.types.guis.ItemTypeGui;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Spawn an itemstack
 * Can handle all custom NBT data
 *
 * @author Dries007
 */
public class ItemType extends TypeBase
{
    public static final String NAME = "item";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        ItemStack is = new ItemStack(Item.appleGold);
        is.setItemName("$name");
        return is.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        try
        {
            ItemStack is = ItemStack.loadItemStackFromNBT(dataFromClient);
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
        new ItemTypeGui(rewardID, getName(), data, null);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        for (Item item : Item.itemsList)
        {
            if (item == null) continue;

//            if (item.getHasSubtypes())
//            {
//                HashSet<String> names = new HashSet<>();
//                for (short s = 0; s < Short.MAX_VALUE; s++)
//                {
//                    try
//                    {
//                        ItemStack is = new ItemStack(item, 1, s);
//                        if (!names.contains(is.getUnlocalizedName()))
//                        {
//                            names.add(is.getUnlocalizedName());
//                            itemStacks.add(is);
//                        }
//                    }
//                    catch (Exception e)
//                    {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            else
//            {
                try
                {
                    itemStacks.add(new ItemStack(item));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
//            }
        }

        HashSet<Node> nodes = new HashSet<>();
        for (ItemStack itemStack : itemStacks)
        {
            String name = itemStack.getUnlocalizedName();
            if (name == null) continue;
            if (name.startsWith("item.")) name = name.substring("item.".length());
            if (name.startsWith("tile.")) name = name.substring("tile.".length());
            nodes.add(new Node(ItemType.NAME, name.replace(".", "_")));
        }

        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        ItemStack itemStack = ItemStack.loadItemStackFromNBT(dataFromClient);
        String name = itemStack.getUnlocalizedName();
        if (name.startsWith("item.")) name = name.substring("item.".length());
        if (name.startsWith("tile.")) name = name.substring("tile.".length());
        return new Node(NAME, name.replace(".", "_"));
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "stacksize":
                return jsonObject.get("Count").getAsString().replace("BYTE:", "");
            case "itemname":
                ItemStack is = ItemStack.loadItemStackFromNBT(JsonNBTHelper.parseJSON(jsonObject));
                return is.getItem().getItemDisplayName(is);
        }
        return id;
    }
}
