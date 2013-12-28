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

import com.google.common.base.Joiner;

import java.util.Arrays;

public class Node
{
    public static final Joiner JOINER = Joiner.on(".").skipNulls();
    final String[] parts;

    public Node(String parts)
    {
        this.parts = parts.split("\\.");
    }

    public Node(String... parts)
    {
        this.parts = parts;
    }

    public boolean matches(Node requestNode)
    {
        return this.equals(requestNode) || checkPart(0, requestNode);
    }

    private boolean checkPart(int i, Node requestNode)
    {
        if (i < parts.length || requestNode.parts.length < i) return false;
        if (parts[i].equals("*")) return true;
        if (parts[i].equals(requestNode.parts[i])) return true;
        return checkPart(++i, requestNode);
    }

    @Override
    public String toString()
    {
        return JOINER.join(parts);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return Arrays.equals(parts, node.parts);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(parts);
    }
}
