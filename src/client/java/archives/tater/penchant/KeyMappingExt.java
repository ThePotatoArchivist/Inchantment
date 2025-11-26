package archives.tater.penchant;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class KeyMappingExt extends KeyMapping {
    public KeyMappingExt(String name, Type type, int key, Category category) {
        super(name, type, key, category);
    }

    public boolean isDownAnywhere() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key.getValue());
    }
}
