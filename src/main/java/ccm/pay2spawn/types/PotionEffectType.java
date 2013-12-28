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
import ccm.pay2spawn.types.guis.PotionEffectTypeGui;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static ccm.pay2spawn.random.RandomRegistry.RANDOM;
import static ccm.pay2spawn.util.JsonNBTHelper.BYTE;
import static ccm.pay2spawn.util.JsonNBTHelper.INT;

/**
 * Applies potion effect
 *
 * @author Dries007
 */
public class PotionEffectType extends TypeBase
{
    public static final String NODENAME      = "potioneffect";
    public static final String ID_KEY        = "Id";
    public static final String AMPLIFIER_KEY = "Amplifier";
    public static final String DURATION_KEY  = "Duration";

    public static final HashBiMap<String, Integer> POTIONS = HashBiMap.create();
    public static final HashMap<String, String>    typeMap = new HashMap<>();

    static
    {
        typeMap.put(ID_KEY, NBTBase.NBTTypes[BYTE]);
        typeMap.put(AMPLIFIER_KEY, NBTBase.NBTTypes[BYTE]);
        typeMap.put(DURATION_KEY, NBTBase.NBTTypes[INT]);
    }

    @Override
    public String getName()
    {
        return NODENAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        Potion potion = null;
        while (potion == null) potion = Potion.potionTypes[RANDOM.nextInt(Potion.potionTypes.length)];
        return new PotionEffect(potion.getId(), (int) (RANDOM.nextDouble() * 1000)).writeCustomPotionEffectToNBT(new NBTTagCompound());
    }

    @Override
    public void printHelpList(File configFolder)
    {
        File file = new File(Pay2Spawn.getFolder(), "Potion.txt");
        try
        {
            if (file.exists()) file.delete();
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file);

            pw.println("# Potion list file");
            pw.println("# Format:");
            pw.println("# ID: name");

            for (Potion potion : Potion.potionTypes)
            {
                if (potion != null)
                {
                    POTIONS.put(potion.getId() + ": " + potion.getName(), potion.getId());
                    pw.println(potion.getId() + ": " + potion.getName());
                }
            }

            pw.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        player.addPotionEffect(PotionEffect.readCustomPotionEffectFromNBT(dataFromClient));
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new PotionEffectTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();

        for (Potion potion : Potion.potionTypes)
        {
            if (potion != null)
            {
                String name = potion.getName();
                if (name.startsWith("potion.")) name = name.substring("potion.".length());
                nodes.add(new Node(NODENAME, name.replace(".", "_")));
            }
        }
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        PotionEffect effect = PotionEffect.readCustomPotionEffectFromNBT(dataFromClient);
        String name = effect.getEffectName();
        if (name.startsWith("potion.")) name = name.substring("potion.".length());
        return new Node(NODENAME, name.replace(".", "_"));
    }
}
