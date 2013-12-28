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
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;

import java.io.File;
import java.util.Collection;

/**
 * Base class for reward types
 *
 * @author Dries007
 */
public abstract class TypeBase
{
    /**
     * Used in JSON file
     *
     * @return the name in lover case only please.
     */
    public abstract String getName();

    /**
     * May or may not be random
     *
     * @return an example, NBT so it can be stored in the JSON
     */
    public abstract NBTTagCompound getExample();

    /**
     * Spawn the reward, only called server side.
     *
     * @param player         The player the reward comes from
     * @param dataFromClient the nbt from the JSON file, fully usable
     */
    public abstract void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient);

    /**
     * Extra method for custom configuration
     * Called pre-preInit
     *
     * @param configuration The configuration you should use
     */
    public void doConfig(Configuration configuration)
    {}

    /**
     * Use to print out useful files (aka entity and sound lists or help files)
     * Called post-preInit
     *
     * @param configFolder Make a file in here
     */
    public void printHelpList(File configFolder)
    {}

    public abstract void openNewGui(int rewardID, JsonObject data);

    public abstract Collection<Node> getPermissionNodes();

    public abstract Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient);
}
