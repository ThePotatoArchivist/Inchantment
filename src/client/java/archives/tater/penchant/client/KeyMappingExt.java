package archives.tater.penchant.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;

public class KeyMappingExt extends KeyMapping {
    public KeyMappingExt(String name, Type type, int key, Category category) {
        super(name, type, key, category);
    }

    public boolean isDownAnywhere() {
        return InputConstants.isKeyDown(key.getValue());
    }
}
