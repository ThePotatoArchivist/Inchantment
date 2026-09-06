package archives.tater.penchant.client;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange;

import java.nio.file.Path;

public class PenchantClientConfig implements WrappedConfig.Section {

    @Comment("Whether enchantment progress should always be shown regardless of keypress")
    public boolean alwaysShowTooltipProgress = false;

    @Comment("If the hint for showing enchantment progress with keybind should be shown")
    @Comment("Does nothing if Always Show Progress is enabled")
    public boolean showTooltipKeyHint = true;

    @Comment("Width of enchantment progress bar")
    @IntegerRange(min = 4, max = 128)
    public int barWidth = 32;

    public static class Wrapper extends WrappedConfig {
        public PenchantClientConfig client = new PenchantClientConfig();
    }

    public static PenchantClientConfig createToml(Path configPath, String familyId, String id) {
        return Wrapper.createToml(configPath, familyId, id, Wrapper.class).client;
    }
}
