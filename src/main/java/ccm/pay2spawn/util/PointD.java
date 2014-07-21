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
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * Thanks FE :p
 *
 * @author Dries007
 */
public class PointD
{
    private double x, y, z;

    public PointD(Entity entity)
    {
        x = entity.posX;
        y = entity.posY;
        z = entity.posZ;
    }

    public PointD(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PointD()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public PointD makeNiceForBlock()
    {
        x = intX() + 0.5;
        y = intY();
        z = intZ() + 0.5;

        return this;
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getZ()
    {
        return z;
    }

    public void setZ(double z)
    {
        this.z = z;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public ArrayList<PointD> getCircle(int rad)
    {
        ArrayList<PointD> pointDs = new ArrayList<>();
        for (int x = -rad; x < rad; x++)
        {
            for (int z = -rad; z < rad; z++)
            {
                PointD p = new PointD(this.x + x, this.y, this.z + z);
                if (distanceTo(p) < rad) pointDs.add(p);
            }
        }
        return pointDs;
    }

    public ArrayList<PointD> getCylinder(int rad, int height)
    {
        height = height / 2;
        ArrayList<PointD> pointDs = new ArrayList<>();
        for (PointD p : getCircle(rad))
        {
            for (int dy = -height; dy < height; dy++)
            {
                pointDs.add(new PointD(p.x, p.y + dy, p.z));
            }
        }
        return pointDs;
    }

    public ArrayList<PointD> getSphere(int rad)
    {
        ArrayList<PointD> pointDs = new ArrayList<>();
        for (PointD p : getCylinder(rad, rad)) if (distanceTo(p) < rad) pointDs.add(p);
        return pointDs;
    }

    public double distanceTo(PointD p)
    {
        return Math.sqrt((diffX(p) * diffX(p)) + (diffY(p) * diffY(p)) + (diffZ(p) * diffZ(p)));
    }

    public double diffX(PointD p)
    {
        return this.x - p.x;
    }

    public double diffY(PointD p)
    {
        return this.y - p.y;
    }

    public double diffZ(PointD p)
    {
        return this.z - p.z;
    }

    public boolean isValid()
    {
        return this.y >= 0;
    }

    @Override
    public String toString()
    {
        return "[" + x + ";" + y + ";" + z + "]";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PointD pointD = (PointD) o;

        return Double.compare(pointD.x, x) == 0 && Double.compare(pointD.y, y) == 0 && Double.compare(pointD.z, z) == 0;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public int intX()
    {
        return (int) x;
    }

    public int intY()
    {
        return (int) y;
    }

    public int intZ()
    {
        return (int) z;
    }

    public void setPosition(Entity entity)
    {
        entity.setPosition(intX() + 0.5, intY() + 0.5, intZ() + 0.5);
    }

    public boolean canSpawn(Entity entity)
    {
        World world = entity.worldObj;
        for (int y = 0; y < Math.ceil(entity.height); y++) if (world.isBlockNormalCubeDefault(intX(), intY() + y, intZ(), false)) return false;
        return true;
    }
}
