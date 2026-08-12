package archives.tater.penchant.definition;

import archives.tater.penchant.Penchant;

import net.fabricmc.loader.api.FabricLoader;

import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.util.Optional;

public class PenchantFallbackParameterManager extends SimplePreparableReloadListener<Optional<PenchantFallbackParameters>> {

    public static final Identifier ID = Penchant.id("fallbacks");
    public static final Identifier FILE_PATH = Penchant.id("fallbacks.json");

    public static final PenchantFallbackConfig CONFIG = PenchantFallbackConfig.createToml(
            FabricLoader.getInstance().getConfigDir(),
            Penchant.MOD_ID,
            "fallbacks",
            PenchantFallbackConfig.class
    );

    private PenchantFallbackParameters parameters = CONFIG;

    public PenchantFallbackParameters getParameters() {
        return parameters;
    }

    @Override
    protected Optional<PenchantFallbackParameters> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return manager.getResource(PenchantFallbackParameterManager.FILE_PATH).flatMap(resource -> {
            try (var reader = resource.openAsReader()) {
                return PenchantFallbackData.CODEC.parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader))
                        .ifError(error -> Penchant.LOGGER.error("Error reading Penchant fallback data: {}", error.message()))
                        .result();
            } catch (IOException e) {
                return Optional.empty();
            }
        });
    }

    @Override
    protected void apply(Optional<PenchantFallbackParameters> preparations, ResourceManager manager, ProfilerFiller profiler) {
        parameters = preparations.orElse(PenchantFallbackParameterManager.CONFIG);
    }
}
