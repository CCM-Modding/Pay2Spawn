package ccm.pay2spawn.util.shapes;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashSet;

public class Sphere extends AbstractShape
{
    int radius;

    public Sphere(int radius)
    {
        super();
        this.radius = radius;
    }

    public Sphere(PointI center, int radius)
    {
        super(center);
        this.radius = radius;
    }

    Sphere()
    {
        super();
    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = super.toNBT();
        compound.setInteger("radius", radius);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        super.fromNBT(compound);
        radius = compound.getInteger("radius");
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
                for (int y = -radius; y <= radius; y++)
                {
                    PointI p = new PointI(center.x + x, center.y + y, center.z + z);
                    if (center.distanceTo(p) <= radius) points.add(p);
                }
            }
        }

        if (hollow) points.removeAll(new Sphere(center, radius - 1).getPoints());

        return points;
    }

    @Override
    public IShape clone()
    {
        return new Sphere(center, radius);
    }
}
