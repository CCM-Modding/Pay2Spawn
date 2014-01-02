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
public class Point
{
    private double x, y, z;

    public Point(Entity entity)
    {
        x = entity.posX;
        y = entity.posY;
        z = entity.posZ;
    }

    public Point(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point makeNiceForBlock()
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

    public ArrayList<Point> getCircle(int rad)
    {
        ArrayList<Point> points = new ArrayList<>();
        for (int x = -rad; x < rad; x++)
        {
            for (int z = -rad; z < rad; z++)
            {
                Point p = new Point(this.x + x, this.y, this.z + z);
                if (distanceTo(p) < rad) points.add(p);
            }
        }
        System.out.println("getCircle" + rad + ": " + points.size());
        return points;
    }

    public ArrayList<Point> getCylinder(int rad, int height)
    {
        height = height / 2;
        ArrayList<Point> points = new ArrayList<>();
        for (Point p : getCircle(rad))
        {
            for (int dy = -height; dy < height; dy++)
            {
                points.add(new Point(p.x, p.y + dy, p.z));
            }
        }
        System.out.println("getCylinder" + rad + ", " + height + ": " + points.size());
        return points;
    }

    public ArrayList<Point> getSphere(int rad)
    {
        ArrayList<Point> points = new ArrayList<>();
        for (Point p : getCylinder(rad, rad)) if (distanceTo(p) < rad) points.add(p);
        System.out.println("getSphere" + rad + ": " + points.size());
        return points;
    }

    public double distanceTo(Point p)
    {
        return Math.sqrt((diffX(p) * diffX(p)) + (diffY(p) * diffY(p)) + (diffZ(p) * diffZ(p)));
    }

    public double diffX(Point p)
    {
        return this.x - p.x;
    }

    public double diffY(Point p)
    {
        return this.y - p.y;
    }

    public double diffZ(Point p)
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

        Point point = (Point) o;

        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0 && Double.compare(point.z, z) == 0;

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
        for (int y = 0; y < Math.ceil(entity.height); y++) if (world.isBlockOpaqueCube(intX(), intY() + y, intZ())) return false;
        return true;
    }
}
