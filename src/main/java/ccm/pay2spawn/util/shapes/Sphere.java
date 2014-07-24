package ccm.pay2spawn.util.shapes;

import ccm.pay2spawn.types.guis.StructureTypeGui;
import ccm.pay2spawn.types.guis.shapes.SphereGui;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.INT;
import static ccm.pay2spawn.util.Constants.NBTTypes;

public class Sphere extends AbstractShape
{
    public static final String RADIUS_KEY = "radius";

    static
    {
        typeMap.put(RADIUS_KEY, NBTTypes[INT]);
    }

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
        compound.setInteger(RADIUS_KEY, radius);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        super.fromNBT(compound);
        radius = compound.getInteger(RADIUS_KEY);
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
    public void openGui(int i, JsonObject jsonObject, StructureTypeGui instance)
    {
        new SphereGui(i, jsonObject, instance, typeMap);
    }

    @Override
    public void render(Tessellator tess)
    {

    }

    @Override
    public IShape cloneShape()
    {
        return new Sphere(center, radius);
    }
}
