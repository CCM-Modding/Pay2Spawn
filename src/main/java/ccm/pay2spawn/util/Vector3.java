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

package ccm.pay2spawn.util;

import net.minecraft.entity.Entity;

public class Vector3
{
    public double x, y, z;

    public Vector3(Entity src, Entity target)
    {
        x = -src.posX + target.posX;
        y = -src.posY + target.posY;
        z = -src.posZ + target.posZ;
    }

    public Vector3 normalize()
    {
        double size = getSize();
        x /= size;
        y /= size;
        z /= size;
        return this;
    }

    private double getSize()
    {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3 setAsVelocity(Entity entity, double multiplier)
    {
        entity.setVelocity(x * multiplier, y * multiplier, z * multiplier);
        return this;
    }
}
