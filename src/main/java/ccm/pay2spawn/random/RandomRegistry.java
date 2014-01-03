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

package ccm.pay2spawn.random;

import java.util.Collection;
import java.util.HashMap;

import static ccm.pay2spawn.util.Constants.RANDOM;

/**
 * Handles random tags placed inside the NBT data from the JSON.
 * Gets redone for every reward.
 *
 * @author Dries007
 */
public class RandomRegistry
{
    private static final HashMap<Class<? extends IRandomResolver>, IRandomResolver> RANDOM_RESOLVERS = new HashMap<>();

    /**
     * Register your IRandomResolver here
     *
     * @param resolver the instance of the resolver
     */
    public static void addRandomResolver(IRandomResolver resolver)
    {
        RANDOM_RESOLVERS.put(resolver.getClass(), resolver);
    }

    /**
     * Internal method. Please leave alone.
     *
     * @param type  NBT type
     * @param value The to be randomised string
     * @return the original or a randomised version
     */
    public static String solveRandom(int type, String value)
    {
        for (IRandomResolver resolver : RANDOM_RESOLVERS.values())
        {
            if (resolver.matches(type, value)) return resolver.solverRandom(type, value);
        }
        return value;
    }

    /**
     * Registered at Pre-Init.
     */
    public static void preInit()
    {
        addRandomResolver(new RndBoolean());
        addRandomResolver(new RndColors());
        addRandomResolver(new RndEntity());
        addRandomResolver(new RndListValue());
        addRandomResolver(new RndNumberRange());
        addRandomResolver(new RndSound());
    }

    /**
     * Get a random element from a collection
     *
     * @param collection the collection
     * @param <T>        the type that makes up the collection
     * @return the random element
     */
    public static <T> T getRandomFromSet(Collection<T> collection)
    {
        if (collection.isEmpty()) return null;
        if (collection.size() == 1) //noinspection unchecked
            return (T) collection.toArray()[0];
        int item = RANDOM.nextInt(collection.size());
        int i = 0;
        for (T obj : collection)
        {
            if (i == item) return obj;
            i = i + 1;
        }
        return null;
    }

    public static IRandomResolver getInstanceFromClass(Class<? extends IRandomResolver> aClass)
    {
        return RANDOM_RESOLVERS.get(aClass);
    }
}
