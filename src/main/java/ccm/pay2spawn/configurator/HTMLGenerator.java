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

package ccm.pay2spawn.configurator;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.checkers.TwitchChecker;
import ccm.pay2spawn.misc.Reward;
import ccm.pay2spawn.types.TypeRegistry;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ccm.pay2spawn.util.Constants.CURRENCY_FORMATTER;

public class HTMLGenerator
{
    static final String  LOOP_START = "<!-- BEGIN REWARDS -->";
    static final String  LOOP_END   = "<!-- END REWARDS -->";
    static final Pattern VAR        = Pattern.compile("\\$\\{([\\w.]*?)\\}");
    public static  File htmlFolder;
    public static  File templateFolder;
    private static File templateIndex;

    public static void init() throws IOException
    {
        htmlFolder = new File(Pay2Spawn.getFolder(), "html");
        templateFolder = new File(htmlFolder, "templates");
        //noinspection ResultOfMethodCallIgnored
        templateFolder.mkdirs();

        templateIndex = new File(templateFolder, "index.html");
        if (!templateIndex.exists())
        {
            InputStream link = (HTMLGenerator.class.getResourceAsStream("/p2sTemplates/index.html"));
            Files.copy(link, templateIndex.getAbsoluteFile().toPath());
        }
        TypeRegistry.copyTemplates();
    }

    public static void generate() throws IOException
    {
        ArrayList<Reward> sortedRewards = new ArrayList<>();
        sortedRewards.addAll(Pay2Spawn.getRewardsDB().getRewards());
        Collections.sort(sortedRewards, new Comparator<Reward>()
        {
            @Override
            public int compare(Reward o1, Reward o2)
            {
                return (int) (o1.getAmount() - o2.getAmount());
            }
        });

        File output = new File(htmlFolder, "index.html");
        String text = readFile(templateIndex);
        int begin = text.indexOf(LOOP_START);
        int end = text.indexOf(LOOP_END);

        FileUtils.writeStringToFile(output, replace(text.substring(0, begin)), false);

        String loop = text.substring(begin + LOOP_START.length(), end);
        for (Reward reward : sortedRewards)
        {
            Pay2Spawn.getLogger().info("Adding " + reward + " to html file.");
            FileUtils.writeStringToFile(output, replace(loop, reward), true);
        }

        FileUtils.writeStringToFile(output, text.substring(end + LOOP_END.length(), text.length()), true);
    }

    private static String replace(String text) throws IOException
    {
        return replace(text, null);
    }

    private static String replace(String text, Reward reward) throws IOException
    {
        while (true)
        {
            Matcher matcher = VAR.matcher(text);
            if (!matcher.find()) break;
            text = text.replace(matcher.group(), get(matcher.group(1), reward));
        }
        return text;
    }

    private static String get(String group, Reward reward) throws IOException
    {
        String[] parts = group.split("\\.");
        switch (parts[0])
        {
            case "channel":
                return TwitchChecker.INSTANCE.getChannel();
            case "reward":
                switch (parts[1])
                {
                    case "name":
                        return reward.getName();
                    case "amount":
                        return CURRENCY_FORMATTER.format(reward.getAmount());
                    case "countdown":
                        return reward.getCountdown().toString();
                    case "message":
                        return reward.getMessage();
                    case "types":
                        return reward.getTypes();
                    case "uid":
                        return Integer.toHexString(reward.hashCode());
                    case "rewards":
                        return reward.getHTML();
                }
        }
        return group;
    }

    public static String readFile(File file) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(file.toURI()));
        return Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
    }
}
