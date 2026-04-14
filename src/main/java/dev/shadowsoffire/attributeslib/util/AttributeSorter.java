package dev.shadowsoffire.attributeslib.util;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeSorter {

    private record AttributeData(String cleanName, boolean hasIcon) {}

    private static final Map<Attribute, AttributeData> CACHE = new ConcurrentHashMap<>();

    public static final Comparator<? super AttributeInstance> ICON_SAFE_COMPARATOR = (attr1, attr2) -> {
        AttributeData data1 = CACHE.computeIfAbsent(attr1.getAttribute(), AttributeSorter::computeData);
        AttributeData data2 = CACHE.computeIfAbsent(attr2.getAttribute(), AttributeSorter::computeData);

        int iconPriority = Boolean.compare(data2.hasIcon(), data1.hasIcon());

        if (iconPriority != 0) {
            return iconPriority;
        }

        return data1.cleanName().compareToIgnoreCase(data2.cleanName());
    };

    private static AttributeData computeData(Attribute attribute) {
        String rawName = I18n.get(attribute.getDescriptionId());

        if (rawName == null || rawName.isEmpty()) {
            return new AttributeData("", false);
        }

        StringBuilder clean = new StringBuilder(rawName.length());
        boolean hasIcon = false;
        boolean skipNext = false;

        for (int i = 0; i < rawName.length(); ) {
            int codePoint = rawName.codePointAt(i);
            int charCount = Character.charCount(codePoint);

            if (skipNext) skipNext = false;
            else if (codePoint == 167) skipNext = true;
            else if (isIconCodePoint(codePoint)) hasIcon = true;
            else clean.appendCodePoint(codePoint);

            i += charCount;
        }

        return new AttributeData(clean.toString().trim(), hasIcon);
    }

    private static boolean isIconCodePoint(int codePoint) {
        return (codePoint >= 0xF900 && codePoint <= 0xFAFF) || // CJK Compatibility Ideographs (Prom 2 compat)
                (codePoint >= 0xE000 && codePoint <= 0xF8FF) || // Private Use Area
                (codePoint >= 0xF0000 && codePoint <= 0xFFFFF) || // Supplementary Private Use Area-A
                (codePoint >= 0x100000 && codePoint <= 0x10FFFD) || // Supplementary Private Use Area-B
                (codePoint >= 0xDC00 && codePoint <= 0xDFFF) || // Low Surrogate Area
                (codePoint >= 0xD800 && codePoint <= 0xDBFF) || // High Surrogate Area
                (codePoint >= 0x1CC00 && codePoint <= 0x1CEBF) || // Symbols for Legacy Computing Supplement (RPG series icons compat)
                (codePoint >= 0x2700 && codePoint <= 0x27BF) || // Dingbats
                (codePoint >= 0xAB00 && codePoint <= 0xAB2F) || // Ethiopic Extended-A (Eldritch End compat)
                (codePoint >= 0xA980 && codePoint <= 0xA9DF); // Javanese (Eldritch End compat)
    }

    public static void clearCache() {
        CACHE.clear();
    }
}