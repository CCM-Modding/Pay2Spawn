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

/**
 * Part of API.
 * Register your implementation!
 * <br>
 * Expected javadoc:
 * <br>
 * [Short explanation + example if needed]
 * Expected syntax: [$random for example]
 * Outcome: [outcome]
 * Works with: [All nbt types this will accept, enforce in #matches.]
 *
 * @author Dries007
 * @see net.minecraft.nbt.NBTBase#NBTTypes
 * @see ccm.pay2spawn.random.RndBoolean RndBoolean for an example
 * @see RandomRegistry#addRandomResolver(IRandomResolver) RandomRegistry.addRandomResolver to register.
 */
public interface IRandomResolver
{
    /**
     * @return a string that matches this random type when used with String.startsWith
     */
    public String getIdentifier();

    /**
     * Gets called when #matches returns true
     *
     * @param type  NBT type
     * @param value The random tag
     * @return the randomised value
     */
    public String solverRandom(int type, String value);

    /**
     * Only return true when you can handle the type and the value matches your (and only your) pattern.
     *
     * @param type  NBT type
     * @param value The random tag
     * @return true to handle this string
     */
    public boolean matches(int type, String value);
}
