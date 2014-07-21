package ccm.pay2spawn.util.shapes;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashSet;

public class Cylinder extends AbstractShape
{
    int radius, height = 0;

    public Cylinder(int radius)
    {
        super();
        this.radius = radius;
    }

    public Cylinder(int radius, int height)
    {
        super();
        this.radius = radius;
        this.height = height;
    }

    public Cylinder(PointI center, int radius)
    {
        super(center);
        this.radius = radius;
    }

    public Cylinder(PointI center, int radius, int height)
    {
        super(center);
        this.radius = radius;
        this.height = height;
    }

    Cylinder()
    {
        super();
    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = super.toNBT();
        compound.setInteger("radius", radius);
        compound.setInteger("height", height);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        super.fromNBT(compound);
        this.radius = compound.getInteger("radius");
        this.height = compound.getInteger("height");
        return this;
    }

    @Override
    public Collection<PointI> getPoints()
    {
        HashSet<PointI> points = new HashSet<>();

        for (int x = -radius; x <= radius; x++)
        {
            for (int z = -radius; z <= radius; z++)
            {
                for (int y = -height; y <= height; y++)
                {
                    PointI p = new PointI(center.x + x, center.y, center.z + z);
                    if (center.distanceTo(p) <= radius) points.add(p.addY(y));
                }
                if (height == 0)
                {
                    PointI p = new PointI(center.x + x, center.y, center.z + z);
                    if (center.distanceTo(p) <= radius) points.add(p);
                }
            }
        }

        if (hollow) points.removeAll(new Cylinder(center, radius - 1, height - 1).getPoints());

        return points;
    }

    @Override
    public IShape clone()
    {
        return new Cylinder(center, radius, height);
    }
}
