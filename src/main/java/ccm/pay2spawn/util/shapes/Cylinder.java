package ccm.pay2spawn.util.shapes;

import ccm.pay2spawn.types.guis.StructureTypeGui;
import ccm.pay2spawn.types.guis.shapes.CylinderGui;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.INT;
import static ccm.pay2spawn.util.Constants.NBTTypes;

public class Cylinder extends AbstractShape
{
    public static final String RADIUS_KEY = "radius";
    public static final String HEIGHT_KEY = "height";

    static
    {
        typeMap.put(RADIUS_KEY, NBTTypes[INT]);
        typeMap.put(HEIGHT_KEY, NBTTypes[INT]);
    }

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
        compound.setInteger(RADIUS_KEY, radius);
        compound.setInteger(HEIGHT_KEY, height);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        super.fromNBT(compound);
        this.radius = compound.getInteger(RADIUS_KEY);
        this.height = compound.getInteger(HEIGHT_KEY);
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
    public void openGui(int i, JsonObject jsonObject, StructureTypeGui instance)
    {
        new CylinderGui(i, jsonObject, instance, typeMap);
    }

    @Override
    public void render(Tessellator tess)
    {
        //TODO
    }

    @Override
    public IShape cloneShape()
    {
        return new Cylinder(center, radius, height);
    }
}
