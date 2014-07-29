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
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.INT;
import static ccm.pay2spawn.util.Constants.NBTTypes;

/**
 * Spawn an itemstack
 * Can handle all custom NBT data
 *
 * @author Dries007
 */
public class ItemType extends TypeBase
{
    public static final String NAME = "item";

    public static final String                  SLOT_KEY = "SLOT";
    public static final HashMap<String, String> typeMap  = new HashMap<>();

    static
    {
        typeMap.put(SLOT_KEY, NBTTypes[INT]);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        ItemStack is = new ItemStack((Item) Item.itemRegistry.getObject("minecraft:golden_apple"));
        is.setStackDisplayName("$name");
        return is.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        try
        {
            int id = dataFromClient.hasKey(SLOT_KEY) ? dataFromClient.getInteger(SLOT_KEY) : -1;
            if (id != -1 && player.inventory.getStackInSlot(id) == null)
            {
                player.inventory.setInventorySlotContents(id, ItemStack.loadItemStackFromNBT(dataFromClient));
            }
            else
            {
                EntityItem entityitem = player.dropPlayerItemWithRandomChoice(ItemStack.loadItemStackFromNBT(dataFromClient), false);
                entityitem.delayBeforeCanPickup = 0;
                entityitem.func_145797_a(player.getCommandSenderName());
            }
        }
        catch (Exception e)
        {
            Pay2Spawn.getLogger().warn("ItemStack could not be spawned. Does the item exists? JSON: " + JsonNBTHelper.parseNBT(dataFromClient));
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new ItemTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        for (Object itemName : Item.itemRegistry.getKeys())
        {
            nodes.add(new Node(ItemType.NAME, itemName.toString().replace(".", "_")));
        }
        for (Object itemName : Block.blockRegistry.getKeys())
        {
            nodes.add(new Node(ItemType.NAME, itemName.toString().replace(".", "_")));
        }
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NAME, ItemStack.loadItemStackFromNBT(dataFromClient).getUnlocalizedName().replace(".", "_"));
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
                return is.getItem().getItemStackDisplayName(is);
        }
        return id;
    }

    @Override
    public void printHelpList(File configFolder)
    {
        File file = new File(configFolder, "Enchantment.txt");
        try
        {
            if (file.exists()) file.delete();
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file);

            pw.println("Enchantment list file");

            ArrayList<String> ids = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> minlvl = new ArrayList<>();
            ArrayList<String> maxlvl = new ArrayList<>();
            for (Enchantment enchantment : Enchantment.enchantmentsList)
            {
                if (enchantment != null)
                {
                    ids.add(enchantment.effectId + "");
                    names.add(enchantment.getTranslatedName(enchantment.getMinLevel()));
                    minlvl.add(enchantment.getMinLevel() + "");
                    maxlvl.add(enchantment.getMaxLevel() + "");
                }
            }
            pw.print(Helper.makeTable(new Helper.TableData("ID", ids), new Helper.TableData("name", names), new Helper.TableData("minLvl", minlvl), new Helper.TableData("maxLvl", maxlvl)));

            pw.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
