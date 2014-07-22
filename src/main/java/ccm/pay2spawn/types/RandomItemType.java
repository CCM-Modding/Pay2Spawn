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
import ccm.pay2spawn.random.RandomRegistry;
import ccm.pay2spawn.types.guis.RandomItemTypeGui;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static ccm.pay2spawn.util.Constants.NBTTypes;
import static ccm.pay2spawn.util.Constants.STRING;

public class RandomItemType extends TypeBase
{
    public static final String                  NAME_KEY    = "Name";
    public static final String                  DISPLAY_KEY = "display";
    public static final String                  TAG_KEY     = "tag";
    public static final HashMap<String, String> typeMap     = new HashMap<>();

    static
    {
        typeMap.put(NAME_KEY, NBTTypes[STRING]);
    }

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
        display.setString(NAME_KEY, "$name");
        tag.setTag(DISPLAY_KEY, display);
        root.setTag(TAG_KEY, tag);

        return root;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        try
        {
            ItemStack is = ItemStack.loadItemStackFromNBT(dataFromClient);
            EntityItem entity = player.dropPlayerItemWithRandomChoice(is, false);
            entity.delayBeforeCanPickup = 0;
            entity.func_145797_a(player.getCommandSenderName());
        }
        catch (Exception e)
        {
            Pay2Spawn.getLogger().warn("ItemStack could not be spawned. Does the item exists? JSON: " + JsonNBTHelper.parseNBT(dataFromClient));
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new RandomItemTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        return new ArrayList<>();
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        ItemStack is;
        do
        {
            is = pickRandomItemStack();
        } while (is == null || is.getItem() == null);

        NBTTagCompound nbtTagCompound = is.writeToNBT(new NBTTagCompound());
        for (Object o : dataFromClient.func_150296_c())
        {
            nbtTagCompound.setTag(o.toString(), dataFromClient.getTag(o.toString()));
        }
        is.readFromNBT(nbtTagCompound);
        is.writeToNBT(dataFromClient);
        String name = is.getUnlocalizedName();
        return new Node(ItemType.NAME, name.replace(".", "_"));
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        return id;
    }

    public ItemStack pickRandomItemStack()
    {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        for (Object itemName : Item.itemRegistry.getKeys())
        {
            itemStacks.add(new ItemStack((Item) Item.itemRegistry.getObject(itemName)));
        }
        for (Object blockName : Block.blockRegistry.getKeys())
        {
            itemStacks.add(new ItemStack(Block.getBlockFromName(blockName.toString())));
        }

        return RandomRegistry.getRandomFromSet(itemStacks);
    }
}
