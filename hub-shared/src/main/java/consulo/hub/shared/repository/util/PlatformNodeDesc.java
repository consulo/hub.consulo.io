package consulo.hub.shared.repository.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 22/05/2023
 */
public record PlatformNodeDesc(String id, String name, Set<String> oldIds, String ext) {
    private static Map<String, PlatformNodeDesc> ourNodes = new HashMap<>();
    private static Map<String, PlatformNodeDesc> ourOldMapping = new HashMap<>();

    static {
        add(new PlatformNodeDesc("consulo.dist.linux.no.jre", "Platform (Linux, without JRE)", Set.of("consulo-linux-no-jre"), "tar.gz"));
        add(new PlatformNodeDesc("consulo.dist.linux", "Platform (Linux, with JRE x86)", Set.of("consulo-linux"), "tar.gz"));
        add(new PlatformNodeDesc("consulo.dist.linux64", "Platform (Linux, with JRE x64)", Set.of("consulo-linux64"), "tar.gz"));
        add(new PlatformNodeDesc("consulo.dist.linux.aarch64", "Platform (Linux, with JRE ARM64)", Set.of("consulo.dist.linuxA64"), "tar.gz"));
        add(new PlatformNodeDesc("consulo.dist.linux.riscv64", "Platform (Linux, with JRE RISCV64)", Set.of(), "tar.gz"));
        add(new PlatformNodeDesc("consulo.dist.linux.loong64", "Platform (Linux, with JRE LOONGARCH64)", Set.of(), "tar.gz"));

        add(new PlatformNodeDesc("consulo.dist.mac64.no.jre", "Platform (macOS x64, without JRE)", Set.of("consulo-mac-no-jre"), "tar.gz"));
        add(new PlatformNodeDesc("consulo.dist.mac64", "Platform (macOS x64, with JRE)", Set.of("consulo-mac64"), "tar.gz"));

        add(new PlatformNodeDesc("consulo.dist.macA64.no.jre", "Platform (macOS ARM64, without JRE)", Set.of(), "tar.gz"));
        add(new PlatformNodeDesc("consulo.dist.macA64", "Platform (macOS ARM64, with JRE)", Set.of(), "tar.gz"));

        add(new PlatformNodeDesc("consulo.dist.windows.no.jre", "Platform (Windows, without JRE)", Set.of("consulo-win-no-jre"), "tar.gz"));
        add(new PlatformNodeDesc("consulo.dist.windows.no.jre.zip", "Platform (Windows, without JRE, zip archive)", Set.of("consulo-win-no-jre-zip"), "zip"));

        add(new PlatformNodeDesc("consulo.dist.windows", "Platform (Windows, with JRE x86)", Set.of("consulo-win"), "tar.gz"));
        add(new PlatformNodeDesc("consulo.dist.windows.zip", "Platform (Windows, zip)", Set.of("consulo-win-zip"), "zip"));

        add(new PlatformNodeDesc("consulo.dist.windows64", "Platform (Windows, with JRE x64)", Set.of("consulo-win64"), "tar.gz"));
        add(new PlatformNodeDesc("consulo.dist.windows64.installer", "Platform (Windows, with JRE x64, installer)", Set.of(), "exe"));
        add(new PlatformNodeDesc("consulo.dist.windows64.zip", "Platform (Windows, with JRE x64, zip)", Set.of("consulo-win64-zip"), "zip"));

        add(new PlatformNodeDesc("consulo.dist.windows.aarch64", "Platform (Windows, with JRE ARM64)", Set.of("consulo-winA64", "consulo.dist.windowsA64"), "tar.gz"));
        add(new PlatformNodeDesc("consulo.dist.windows.aarch64.zip", "Platform (Windows, with JRE ARM64, zip)", Set.of("consulo-winA64-zip", "consulo.dist.windowsA64.zip"), "zip"));
    }

    public static PlatformNodeDesc getNode(String id) {
        return ourNodes.get(id);
    }

    public static Collection<PlatformNodeDesc> values() {
        return ourNodes.values();
    }

    public static PlatformNodeDesc findByOldId(String oldId) {
        return ourOldMapping.get(oldId);
    }

    private static void add(PlatformNodeDesc node) {
        if (ourNodes.put(node.id(), node) != null) {
            throw new IllegalArgumentException("Duplicate " + node.id());
        }

        for (String oldId : node.oldIds()) {
            ourOldMapping.put(oldId, node);
        }
    }
}
