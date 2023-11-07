package surreal.textiles.blocks.properties;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * To get name as "axis_<name>" to differentiate names with degree stuff in blockstate JSONs.
 * It looks like it should've normally work but it didn't work here for some reason :raised_eyebrow:
 * */
public class PropertyAxis extends PropertyEnum<EnumFacing.Axis> {

    protected PropertyAxis(String name, Collection<EnumFacing.Axis> allowedValues) {
        super(name, EnumFacing.Axis.class, allowedValues);
    }

    public static PropertyAxis create(String name) {
        List<EnumFacing.Axis> list = new ObjectArrayList<>();
        Collections.addAll(list, EnumFacing.Axis.values());
        return new PropertyAxis(name, list);
    }

    public static PropertyAxis create(String name, EnumFacing.Axis... allowedValues) {
        List<EnumFacing.Axis> list = new ObjectArrayList<>();
        Collections.addAll(list, allowedValues);
        return new PropertyAxis(name, list);
    }

    @Nonnull
    @Override
    public String getName(@Nonnull EnumFacing.Axis value) {
        return "axis_" + super.getName(value);
    }
}
