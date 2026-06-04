package surreal.textiles.compat;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

public enum TextilesCompat {

    ;

    static {
        // since the mod loaded checks are made at class initialization, make sure this doesn't happen too early
        if (!Loader.instance().hasReachedState(LoaderState.PREINITIALIZATION)) {
            throw new IllegalStateException("TextilesCompat loaded too early!");
        }
    }

    public static final String FLUIDLOGGED = "fluidlogged_api";
    public static final boolean FLUIDLOGGED_LOADED = Loader.isModLoaded(FLUIDLOGGED);

}
