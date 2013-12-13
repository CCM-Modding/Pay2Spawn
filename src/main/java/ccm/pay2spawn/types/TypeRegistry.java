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

import net.minecraftforge.common.Configuration;

import java.util.Collection;
import java.util.HashMap;

public class TypeRegistry
{
    private static HashMap<String, TypeBase> map = new HashMap<>();

    public static void register(TypeBase typeBase)
    {
        if (map.put(typeBase.getName().toLowerCase(), typeBase) != null) throw new IllegalArgumentException("Duplicate type!");
    }

    public static TypeBase getByName(String name)
    {
        return map.get(name.toLowerCase());
    }

    public static void doConfig(Configuration configuration)
    {
        for (TypeBase type : map.values())
        {
            type.doConfig(configuration);
        }
    }

    public static Collection<TypeBase> getAllTypes()
    {
        return map.values();
    }

    public static int getAmount()
    {
        return map.size();
    }
}
