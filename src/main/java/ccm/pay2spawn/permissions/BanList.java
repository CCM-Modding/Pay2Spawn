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

package ccm.pay2spawn.permissions;

import ccm.pay2spawn.Pay2Spawn;
import com.google.common.base.Strings;

import java.io.*;
import java.util.HashSet;

public class BanList
{
    public static final String[] BAD_CMD = {"stop", "op", "deop", "ban", "ban-ip", "pardon", "pardon-ip", "save-on", "save-off", "save-all"};
    HashSet<Node> nodes = new HashSet<>();

    public void save()
    {
        try
        {
            File file = getFile();
            if (!file.exists()) //noinspection ResultOfMethodCallIgnored
                file.createNewFile();

            PrintWriter pw = new PrintWriter(file);

            pw.println("## Any and all nodes in this list are globally banned.");
            pw.println("## 1 node per line.");
            pw.println("## Nodes can end in .* to indicate a wildcard.");

            for (Node node : nodes) pw.println(node.toString());

            pw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void load() throws IOException
    {
        nodes.clear();
        File file = getFile();
        if (file.exists())
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            for(String line; (line = br.readLine()) != null; )
            {
                line = line.trim();
                if (!Strings.isNullOrEmpty(line) && !line.startsWith("#")) nodes.add(new Node(line));
            }
        }
        else
        {
            try
            {
                file.createNewFile();

                PrintWriter pw = new PrintWriter(file);

                pw.println("## Any and all nodes in this list are globally banned.");
                pw.println("## 1 node per line.");
                pw.println("## Nodes can end in .* to indicate a wildcard.");

                for (String cmd : BAD_CMD) pw.println("command." + cmd);

                pw.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public File getFile()
    {
        return new File(Pay2Spawn.getFolder(), "BanList.txt");
    }

    public boolean contains(Node node)
    {
        for (Node bannedNode : nodes)
            if (bannedNode.matches(node))
                return true;
        return false;
    }
}
