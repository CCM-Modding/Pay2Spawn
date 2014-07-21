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

import ccm.pay2spawn.configurator.Configurator;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.util.Constants;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.shapes.*;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraftforge.common.config.Configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.*;

public class StructureType extends TypeBase
{
    public static final  HashMap<String, String> typeMap         = new HashMap<>();
    private static final String                  NAME            = "structure";

    public static final  String                  SHAPES_KEY     = "shapes";

    public static final  String                  BLOCKDATA_KEY  = "blockData";
    public static final  String                  TEDATA_KEY     = "tileEntityData";

    static
    {
        typeMap.put(SHAPES_KEY, NBTTypes[LIST]);

        typeMap.put(BLOCKDATA_KEY, NBTTypes[LIST]);
        typeMap.put(TEDATA_KEY, NBTTypes[LIST]);
    }

    public static String[] bannedBlocks = {};

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagList shapesList = new NBTTagList();

        {
            // Sphere
            {
                NBTTagCompound shapeNbt = Shapes.storeShape(new Sphere(10).setHollow(true).setReplaceableOnly(true));

                NBTTagList blockDataNbt = new NBTTagList();
                blockDataNbt.appendTag(new NBTTagString("35:0:4"));
                blockDataNbt.appendTag(new NBTTagString("35:5"));
                shapeNbt.setTag(BLOCKDATA_KEY, blockDataNbt);

                shapesList.appendTag(shapeNbt);
            }

            // Box
            {
                NBTTagCompound shapeNbt = Shapes.storeShape(new Box(new PointI(-2, -3, 5), 5, 2, 3).setReplaceableOnly(true));

                NBTTagList blockDataNbt = new NBTTagList();
                blockDataNbt.appendTag(new NBTTagString("98"));
                blockDataNbt.appendTag(new NBTTagString("98:1"));
                blockDataNbt.appendTag(new NBTTagString("98:2"));
                blockDataNbt.appendTag(new NBTTagString("98:3"));
                shapeNbt.setTag(BLOCKDATA_KEY, blockDataNbt);

                shapesList.appendTag(shapeNbt);
            }

            // Cylinder
            {
                NBTTagCompound shapeNbt = Shapes.storeShape(new Cylinder(new PointI(0, 3, 0), 12));

                NBTTagList blockDataNbt = new NBTTagList();
                blockDataNbt.appendTag(new NBTTagString("99:14"));
                blockDataNbt.appendTag(new NBTTagString("100:14:10"));
                shapeNbt.setTag(BLOCKDATA_KEY, blockDataNbt);

                shapesList.appendTag(shapeNbt);
            }

            // Pillar
            {
                NBTTagCompound shapeNbt = Shapes.storeShape(new Pillar(new PointI(-2, 0, -6), 15));

                NBTTagList blockDataNbt = new NBTTagList();
                blockDataNbt.appendTag(new NBTTagString("159:0"));
                blockDataNbt.appendTag(new NBTTagString("159:1"));
                blockDataNbt.appendTag(new NBTTagString("159:2"));
                blockDataNbt.appendTag(new NBTTagString("159:3"));
                blockDataNbt.appendTag(new NBTTagString("159:4"));
                blockDataNbt.appendTag(new NBTTagString("159:5"));
                blockDataNbt.appendTag(new NBTTagString("159:6"));
                blockDataNbt.appendTag(new NBTTagString("159:7"));
                blockDataNbt.appendTag(new NBTTagString("159:8"));
                blockDataNbt.appendTag(new NBTTagString("159:9"));
                blockDataNbt.appendTag(new NBTTagString("159:10"));
                blockDataNbt.appendTag(new NBTTagString("159:11"));
                blockDataNbt.appendTag(new NBTTagString("159:12"));
                blockDataNbt.appendTag(new NBTTagString("159:13"));
                blockDataNbt.appendTag(new NBTTagString("159:14"));
                blockDataNbt.appendTag(new NBTTagString("159:15"));
                shapeNbt.setTag(BLOCKDATA_KEY, blockDataNbt);

                shapesList.appendTag(shapeNbt);
            }

            // Point
            {
                NBTTagCompound shapeNbt = Shapes.storeShape(new PointI(0, 0, 0));

                NBTTagList blockDataNbt = new NBTTagList();
                blockDataNbt.appendTag(new NBTTagString("54"));
                shapeNbt.setTag(BLOCKDATA_KEY, blockDataNbt);

                NBTTagList teDataNbt = new NBTTagList();
                TileEntityChest chest = new TileEntityChest();
                chest.setInventorySlotContents(13, new ItemStack(Items.golden_apple));
                NBTTagCompound chestNbt = new NBTTagCompound();
                chest.writeToNBT(chestNbt);
                teDataNbt.appendTag(chestNbt);
                shapeNbt.setTag(TEDATA_KEY, teDataNbt);

                shapesList.appendTag(shapeNbt);
            }

            // Point
            {
                NBTTagCompound shapeNbt = Shapes.storeShape(new PointI(-1, -2, -1));

                NBTTagList blockDataNbt = new NBTTagList();
                NBTTagList teDataNbt = new NBTTagList();
                for (Object mob : EntityList.entityEggs.keySet())
                {
                    TileEntityMobSpawner mobSpawner = new TileEntityMobSpawner();
                    mobSpawner.func_145881_a().setEntityName(EntityList.getStringFromID((Integer) mob));
                    NBTTagCompound spawnerNbt = new NBTTagCompound();
                    mobSpawner.writeToNBT(spawnerNbt);
                    teDataNbt.appendTag(spawnerNbt);

                    blockDataNbt.appendTag(new NBTTagString("52"));
                }

                shapeNbt.setTag(BLOCKDATA_KEY, blockDataNbt);
                shapeNbt.setTag(TEDATA_KEY, teDataNbt);

                shapesList.appendTag(shapeNbt);
            }
        }

        root.setTag(SHAPES_KEY, shapesList);
        return root;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        NBTTagList list = dataFromClient.getTagList(SHAPES_KEY, COMPOUND);
        for (int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound shapeNbt = list.getCompoundTagAt(i);

            NBTTagList blockDataNbt = shapeNbt.getTagList(BLOCKDATA_KEY, STRING);
            String[] blockData = new String[blockDataNbt.tagCount()];
            for (int j = 0; j < blockDataNbt.tagCount(); j++)
            {
                blockData[j] = blockDataNbt.getStringTagAt(j);
            }

            NBTTagCompound[] teArray = null;
            if (shapeNbt.hasKey(TEDATA_KEY))
            {
                NBTTagList teNbt = shapeNbt.getTagList(TEDATA_KEY, COMPOUND);
                teArray = new NBTTagCompound[teNbt.tagCount()];
                for (int j = 0; j < teNbt.tagCount(); j++)
                    teArray[j] = teNbt.getCompoundTagAt(j);
            }

            Helper.applyShape(Shapes.loadShape(shapeNbt), player, teArray, blockData);
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        //TODO: make gui -_-
        Configurator.instance.callback(rewardID, NAME, data);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();

        nodes.add(new Node(NAME));

        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NAME);
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        return id;
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(Constants.MODID + "_types", "Reward config options");
        configuration.addCustomCategoryComment(Constants.MODID + "_types." + NAME, "Used when spawning structures");
        bannedBlocks = configuration.get(Constants.MODID + "_types." + NAME, "bannedBlocks", bannedBlocks, "Banned blocks, format like this:\nid:metaData => Ban only that meta\nid => Ban all meta of that block").getStringList();
    }
}
