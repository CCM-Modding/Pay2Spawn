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
import ccm.pay2spawn.types.guis.DropItemTypeGui;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.INT;

public class DropItemType extends TypeBase
{
    public static final String TYPE_KEY = "type";
    public static final String NODENAME = "dropitems";

    public static final int HOLDING_1   = 0;
    public static final int HOLDING_ALL = 1;
    public static final int ALL         = 2;
    public static final int ARMOR       = 3;

    public static final HashMap<String, String> typeMap = new HashMap<>();

    static
    {
        typeMap.put(TYPE_KEY, NBTBase.NBTTypes[INT]);
    }

    @Override
    public String getName()
    {
        return NODENAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TYPE_KEY, ALL);
        return compound;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        switch (dataFromClient.getInteger(TYPE_KEY))
        {
            case HOLDING_1:
                player.dropOneItem(false);
                break;
            case HOLDING_ALL:
                player.dropOneItem(true);
                break;
            case ALL:
                player.inventory.dropAllItems();
                break;
            case ARMOR:
                for (int i = 0; i < player.inventory.armorInventory.length; ++i)
                {
                    if (player.inventory.armorInventory[i] != null)
                    {
                        player.dropPlayerItemWithRandomChoice(player.inventory.armorInventory[i], true);
                        player.inventory.armorInventory[i] = null;
                    }
                }
                break;
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new DropItemTypeGui(rewardID, getName(), data, typeMap);
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
        switch (id)
        {
            case "type":
                int i = Integer.getInteger(jsonObject.get(TYPE_KEY).getAsString().replace("INT:", ""));
                switch (i)
                {
                    case HOLDING_1:
                        return "one of the selected items";
                    case HOLDING_ALL:
                        return "all of the selected items";
                    case ALL:
                        return "all items";
                    case ARMOR:
                        return "all the armor worn";
                }
        }
        return id;
    }
}
