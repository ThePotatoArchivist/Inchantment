package archives.tater.penchant.definition;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.definition.PenchantFallbackParams.Formula;
import archives.tater.penchant.definition.PenchantFallbackParams.NumberSource;

import net.fabricmc.loader.api.FabricLoader;

import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;

public class PenchantFallbackParamsManager extends SimplePreparableReloadListener<Optional<PenchantFallbackParams>> {

    public static final Identifier ID = Penchant.id("fallbacks");
    public static final Identifier FILE_PATH = Penchant.id("fallbacks.json");
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("penchant/fallbacks.json");

    private static final Logger LOGGER = LoggerFactory.getLogger(Penchant.MOD_ID + "_fallback_config");
    private static final Gson BUILDER = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    private static final PenchantFallbackParams DEFAULT_CONFIG = new PenchantFallbackParams(
            new Formula(NumberSource.ANVIL_COST),
            new Formula(NumberSource.MIN_COST_BASE, 2, -5),
            new Formula(NumberSource.MAX_COST_BASE),
            new Formula(NumberSource.MAX_COST_INCREASE)
    );

    private static final PenchantFallbackParams CONFIG = readOrCreateConfig();

    private PenchantFallbackParams params = CONFIG;

    public PenchantFallbackParams getParams() {
        return params;
    }

    @Override
    protected Optional<PenchantFallbackParams> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return manager.getResource(PenchantFallbackParamsManager.FILE_PATH).flatMap(resource -> {
            try (var reader = resource.openAsReader()) {
                return read(reader);
            } catch (IOException e) {
                return Optional.empty();
            }
        });
    }

    @Override
    protected void apply(Optional<PenchantFallbackParams> preparations, ResourceManager manager, ProfilerFiller profiler) {
        params = preparations.orElse(CONFIG);
    }

    private static Optional<PenchantFallbackParams> read(BufferedReader reader) {
        return PenchantFallbackParams.CODEC.parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader))
                .ifError(error -> LOGGER.error("Error reading Penchant fallback data: {}", error.message()))
                .result();
    }

    private static PenchantFallbackParams readOrCreateConfig() {
        try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
            return read(reader).orElseThrow();
        } catch (NoSuchFileException e) {
            LOGGER.info("Creating empty fallback config");
            try {
                Files.createDirectories(CONFIG_PATH.getParent());
                DataProvider.saveStable(CachedOutput.NO_CACHE, PenchantFallbackParams.CODEC, DEFAULT_CONFIG, CONFIG_PATH);
            } catch (IOException ex) {
                LOGGER.error("Could not write fallback config", ex);
            }
            return DEFAULT_CONFIG;
        } catch (IOException e) {
            LOGGER.error("Could not read fallback config", e);
            return DEFAULT_CONFIG;
        }
    }
}
