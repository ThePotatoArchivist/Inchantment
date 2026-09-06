package archives.tater.penchant.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class PenchantUtil {
    private PenchantUtil() {}

    public static <T> Stream<Holder<T>> streamOrdered(HolderLookup<T> registry, TagKey<T> orderTag) {
        return registry.get(orderTag).map(holderSet -> Stream.concat(
                holderSet.stream(),
                registry.listElements()
                        .filter(holder -> !holderSet.contains(holder))
        )).orElseGet(() -> registry.listElements().map(Function.identity()));
    }

    public static List<String> toFlatListRemoveStyle(Component text) {
        var result = new ArrayList<String>();
        text.visit((_, contents) -> {
            if (!contents.isEmpty())
                result.add(contents);
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    public static boolean containsIgnoreStyle(Component outer, Component check) {
        var outerFlat = toFlatListRemoveStyle(outer);
        var checkFlat = toFlatListRemoveStyle(check);
        return Collections.indexOfSubList(outerFlat, checkFlat) != -1;
    }
}
