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

import ccm.pay2spawn.configurator.HTMLGenerator;
import ccm.pay2spawn.permissions.Node;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for reward types
 *
 * @author Dries007
 */
public abstract class TypeBase
{
    public static final Pattern VAR = Pattern.compile("\\$\\{([\\w.]*?)\\}");

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
    public abstract void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData);

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

    public void copyTemplateFile(File destinationFolder) throws IOException
    {
        File template = new File(destinationFolder, getName() + ".html");
        if (!template.exists())
        {
            InputStream link = (getClass().getResourceAsStream("/p2sTemplates/" + getName() + ".html"));
            Files.copy(link, template.getAbsoluteFile().toPath());
        }
    }

    public String getHTML(JsonObject data) throws IOException
    {
        String text = HTMLGenerator.readFile(getTermlateFile());
        while (true)
        {
            Matcher matcher = VAR.matcher(text);
            if (!matcher.find()) break;
            text = text.replace(matcher.group(), replaceInTemplate(matcher.group(1), data));
        }
        return text;
    }

    public File getTermlateFile()
    {
        return new File(HTMLGenerator.templateFolder, getName() + ".html");
    }

    public abstract String replaceInTemplate(String id, JsonObject jsonObject);
}
