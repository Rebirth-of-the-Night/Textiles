package surreal.textiles.client.models;

import com.github.bsideup.jabel.Desugar;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.IClip;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;
import surreal.textiles.Textiles;
import surreal.textiles.blocks.BlockSpindle;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class BlockSpindleModel implements IModel {

    private static final List<ResourceLocation> BASE_TEXTURES = new ArrayList<>();

    static {
        for (final BlockSpindle.Type type : BlockSpindle.Type.VALUES) {
            final String prefix = "blocks/fabric_" + type.getName();
            BASE_TEXTURES.add(new ResourceLocation(Textiles.MODID, prefix + "_sides"));
            BASE_TEXTURES.add(new ResourceLocation(Textiles.MODID, prefix + "_ends"));
            BASE_TEXTURES.add(new ResourceLocation(Textiles.MODID, prefix + "_extras"));
        }
    }

    public static class Loader implements ICustomModelLoader {

        private static final JsonParser JSON_PARSER = new JsonParser();

        private IResourceManager resourceManager;

        @Override
        public void onResourceManagerReload(final IResourceManager resourceManager) {
            this.resourceManager = resourceManager;
        }

        @Override
        public boolean accepts(final ResourceLocation modelLocation) {
            return modelLocation.getNamespace().equals(Textiles.MODID) && modelLocation.getPath().endsWith(".spindle");
        }

        @Override
        public IModel loadModel(final ResourceLocation modelLocation) {
            try {
                final IResource resource = resourceManager.getResource(getResourcePath(modelLocation));
                final JsonObject modelDto;
                try (final Reader modelIn = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                    final JsonElement dto = JSON_PARSER.parse(modelIn);
                    if (!dto.isJsonObject()) {
                        throw new IllegalStateException("Spindle model is not a JSON object");
                    }
                    modelDto = (JsonObject) dto;
                }
                final SubModel baseModel;
                if (modelDto.has("base")) {
                    final ResourceLocation baseLoc = new ResourceLocation(
                            JsonUtils.getString(modelDto.get("base"), "base"));
                    baseModel = new SubModel(baseLoc, ModelLoaderRegistry.getModel(baseLoc));
                } else {
                    baseModel = null;
                }
                final SubModel[] boltModels;
                if (modelDto.has("bolts")) {
                    final JsonArray boltsDto = JsonUtils.getJsonArray(modelDto.get("bolts"), "bolts");
                    boltModels = new SubModel[boltsDto.size()];
                    for (int i = 0; i < boltModels.length; i++) {
                        final ResourceLocation boltLoc = new ResourceLocation(
                                JsonUtils.getString(boltsDto.get(i), "bolts." + i));
                        boltModels[i] = new SubModel(boltLoc, ModelLoaderRegistry.getModel(boltLoc));
                    }
                } else {
                    boltModels = new SubModel[0];
                }
                return new BlockSpindleModel(baseModel, boltModels);
            } catch (final Exception e) {
                throw new IllegalStateException("Error while loading spindle model: " + modelLocation, e);
            }
        }

        private ResourceLocation getResourcePath(final ResourceLocation loc) {
            final String path = loc.getPath();
            return new ResourceLocation(
                    loc.getNamespace(),
                    path.startsWith("models/") ? (path + ".json") : ("models/block/" + path + ".json"));
        }

    }

    private final IModel parent;
    @Nullable
    private final SubModel baseModel;
    private final SubModel[] boltModels;

    private BlockSpindleModel(@Nullable final SubModel baseModel, final SubModel[] boltModels) {
        if (baseModel == null && boltModels.length == 0) {
            throw new IllegalArgumentException("Spindle model must have at least one submodel!");
        }
        this.parent = baseModel != null ? baseModel.model : boltModels[0].model;
        this.baseModel = baseModel;
        this.boltModels = boltModels;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return BASE_TEXTURES;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        final List<ResourceLocation> deps = new ArrayList<>();
        if (baseModel != null) {
            deps.add(baseModel.location);
        }
        for (final SubModel model : boltModels) {
            deps.add(model.location);
        }
        return deps;
    }

    @Override
    public IModelState getDefaultState() {
        return parent.getDefaultState();
    }

    @Override
    public Optional<? extends IClip> getClip(final String name) {
        return parent.getClip(name);
    }

    @Override
    public IModel process(final ImmutableMap<String, String> customData) {
        final SubModel[] newBoltModels = new SubModel[boltModels.length];
        for (int i = 0; i < boltModels.length; i++) {
            final SubModel model = boltModels[i];
            newBoltModels[i] = new SubModel(model.location, model.model.process(customData));
        }
        return new BlockSpindleModel(
                baseModel == null ? null : new SubModel(baseModel.location, baseModel.model.process(customData)),
                newBoltModels);
    }

    @Override
    public IModel smoothLighting(final boolean value) {
        final SubModel[] newBoltModels = new SubModel[boltModels.length];
        for (int i = 0; i < boltModels.length; i++) {
            final SubModel model = boltModels[i];
            newBoltModels[i] = new SubModel(model.location, model.model.smoothLighting(value));
        }
        return new BlockSpindleModel(
                baseModel == null ? null : new SubModel(baseModel.location, baseModel.model.smoothLighting(value)),
                newBoltModels);
    }

    @Override
    public IModel gui3d(final boolean value) {
        final SubModel[] newBoltModels = new SubModel[boltModels.length];
        for (int i = 0; i < boltModels.length; i++) {
            final SubModel model = boltModels[i];
            newBoltModels[i] = new SubModel(model.location, model.model.gui3d(value));
        }
        return new BlockSpindleModel(
                baseModel == null ? null : new SubModel(baseModel.location, baseModel.model.gui3d(value)),
                newBoltModels);
    }

    @Override
    public IModel uvlock(final boolean value) {
        final SubModel[] newBoltModels = new SubModel[boltModels.length];
        for (int i = 0; i < boltModels.length; i++) {
            final SubModel model = boltModels[i];
            newBoltModels[i] = new SubModel(model.location, model.model.uvlock(value));
        }
        return new BlockSpindleModel(
                baseModel == null ? null : new SubModel(baseModel.location, baseModel.model.uvlock(value)),
                newBoltModels);
    }

    @Override
    public IModel retexture(final ImmutableMap<String, String> textures) {
        final SubModel[] newBoltModels = new SubModel[boltModels.length];
        for (int i = 0; i < boltModels.length; i++) {
            final SubModel model = boltModels[i];
            newBoltModels[i] = new SubModel(model.location, model.model.retexture(textures));
        }
        return new BlockSpindleModel(
                baseModel == null ? null : new SubModel(baseModel.location, baseModel.model.retexture(textures)),
                newBoltModels);
    }

    @Override
    public IBakedModel bake(final IModelState state, final VertexFormat format,
                            final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        final IBakedModel base = baseModel != null ? baseModel.model.bake(state, format, bakedTextureGetter) : null;
        final IBakedModel[][] bolts = new IBakedModel[boltModels.length][BlockSpindle.Type.VALUES.size()];
        for (int i = 0; i < bolts.length; i++) {
            final IBakedModel[] boltModelTable = bolts[i];
            for (final BlockSpindle.Type type : BlockSpindle.Type.VALUES) {
                boltModelTable[type.ordinal()] = boltModels[i].model
                        .retexture(ImmutableMap.of(
                                "sides", "textiles:blocks/fabric_" + type.getName() + "_sides",
                                "ends", "textiles:blocks/fabric_" + type.getName() + "_ends",
                                "extras", "textiles:blocks/fabric_" + type.getName() + "_extras"))
                        .bake(state, format, bakedTextureGetter);
            }
        }
        return new StateHandler(base, bolts).getModelForType(BlockSpindle.Type.PLAIN);
    }

    @Desugar
    private record SubModel(ResourceLocation location, IModel model) {}

    private static class StateHandler extends ItemOverrideList {

        @Nullable
        private final IBakedModel baseModel;
        private final IBakedModel[][] boltModels;
        private final Map<BlockSpindle.Type, IBakedModel> cache = new EnumMap<>(BlockSpindle.Type.class);

        public StateHandler(@Nullable final IBakedModel baseModel, final IBakedModel[][] boltModels) {
            super(Collections.emptyList());
            this.baseModel = baseModel;
            this.boltModels = boltModels;
        }

        @Override
        public IBakedModel handleItemState(final IBakedModel originalModel, final ItemStack stack,
                                           @Nullable final World world, @Nullable final EntityLivingBase entity) {
            return getModelForType(BlockSpindle.Type.fromMeta(stack.getMetadata()));

        }

        private IBakedModel getModelForType(final BlockSpindle.Type type) {
            IBakedModel model = cache.get(type);
            if (model != null) return model;
            model = new Baked(baseModel, boltModels, type.ordinal(), this);
            cache.put(type, model);
            return model;
        }

    }

    private static class Baked implements IBakedModel {

        private final IBakedModel parent;
        @Nullable
        private final IBakedModel baseModel;
        private final IBakedModel[][] boltModels;
        private final int defaultTypeIndex;
        private final StateHandler stateHandler;

        private Baked(@Nullable final IBakedModel baseModel, final IBakedModel[][] boltModels,
                      final int defaultTypeIndex, final StateHandler stateHandler) {
            this.parent = baseModel != null ? baseModel : boltModels[0][defaultTypeIndex];
            this.baseModel = baseModel;
            this.boltModels = boltModels;
            this.defaultTypeIndex = defaultTypeIndex;
            this.stateHandler = stateHandler;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable final IBlockState state, @Nullable final EnumFacing side,
                                        final long rand) {
            final List<BakedQuad> quads = new ArrayList<>();
            if (baseModel != null) {
                quads.addAll(baseModel.getQuads(state, side, rand));
            }
            if (state == null || !(state.getBlock() instanceof BlockSpindle)) {
                for (final IBakedModel[] boltModelTable : boltModels) {
                    quads.addAll(boltModelTable[defaultTypeIndex].getQuads(state, side, rand));
                }
            } else {
                final List<BlockSpindle.Type> spindles = ((IExtendedBlockState) state).getValue(BlockSpindle.SPINDLES);
                final int iMax = Math.min(spindles.size(), boltModels.length);
                int i = 0;
                for (; i < iMax; i++) {
                    quads.addAll(boltModels[i][spindles.get(i).ordinal()].getQuads(state, side, rand));
                }
                for (; i < boltModels.length; i++) {
                    quads.addAll(boltModels[i][defaultTypeIndex].getQuads(state, side, rand));
                }
            }
            return quads;
        }

        @Override
        public boolean isAmbientOcclusion() {
            return parent.isAmbientOcclusion();
        }

        @Override
        public boolean isAmbientOcclusion(final IBlockState state) {
            return parent.isAmbientOcclusion(state);
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(final ItemCameraTransforms.TransformType cameraTransformType) {
            return parent.handlePerspective(cameraTransformType);
        }

        @Override
        public boolean isGui3d() {
            return parent.isGui3d();
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return parent.getParticleTexture();
        }

        @Override
        public ItemOverrideList getOverrides() {
            return stateHandler;
        }

    }

}
