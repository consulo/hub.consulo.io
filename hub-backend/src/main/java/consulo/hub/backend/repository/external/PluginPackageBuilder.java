package consulo.hub.backend.repository.external;

import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Builds native Linux packages (.deb, .pkg.tar.gz) from .consulo-plugin ZIP artifacts.
 * Returns null when the plugin artifact is not available locally (externally hosted).
 * <p>
 * Unix permissions are preserved from the source ZIP entries via {@link ZipArchiveEntry#getUnixMode()}.
 *
 * @author VISTALL
 */
public final class PluginPackageBuilder {
    private static final String PLUGIN_INSTALL_PREFIX = "usr/share/consulo-plugins/";
    private static final String PLATFORM_INSTALL_PATH = "usr/share/consulo/";

    private PluginPackageBuilder() {
    }

    // ---- Plugin packages ----

    @Nullable
    public static byte[] buildDeb(@Nonnull PluginNode node) throws IOException {
        Path artifact = resolveArtifact(node);
        if (artifact == null) return null;

        byte[] controlTarGz = buildControlTarGz(node);
        byte[] dataTarGz = buildDataTarGz(artifact, node.id);
        byte[] debianBinary = "2.0\n".getBytes(StandardCharsets.US_ASCII);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("!<arch>\n".getBytes(StandardCharsets.US_ASCII));
        writeArEntry(out, "debian-binary", debianBinary);
        writeArEntry(out, "control.tar.gz", controlTarGz);
        writeArEntry(out, "data.tar.gz", dataTarGz);
        return out.toByteArray();
    }

    @Nullable
    public static byte[] buildPkg(@Nonnull PluginNode node) throws IOException {
        Path artifact = resolveArtifact(node);
        if (artifact == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GZIPOutputStream(baos))) {
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            byte[] pkgInfoBytes = buildPkgInfo(node).getBytes(StandardCharsets.UTF_8);
            TarArchiveEntry pkgInfoEntry = new TarArchiveEntry(".PKGINFO");
            pkgInfoEntry.setSize(pkgInfoBytes.length);
            tar.putArchiveEntry(pkgInfoEntry);
            tar.write(pkgInfoBytes);
            tar.closeArchiveEntry();

            String installPath = PLUGIN_INSTALL_PREFIX + node.id + "/";
            appendZipEntries(tar, artifact, installPath);
        }
        return baos.toByteArray();
    }

    // ---- Platform packages ----

    @Nullable
    public static byte[] buildPlatformDeb(@Nonnull PluginNode node,
                                           @Nonnull PackageRepositoryUtil.LinuxPlatformInfo info) throws IOException {
        Path artifact = resolveArtifact(node);
        if (artifact == null) return null;

        byte[] controlTarGz = buildPlatformControlTarGz(node, info);
        byte[] dataTarGz = buildPlatformDataTarGz(artifact);
        byte[] debianBinary = "2.0\n".getBytes(StandardCharsets.US_ASCII);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("!<arch>\n".getBytes(StandardCharsets.US_ASCII));
        writeArEntry(out, "debian-binary", debianBinary);
        writeArEntry(out, "control.tar.gz", controlTarGz);
        writeArEntry(out, "data.tar.gz", dataTarGz);
        return out.toByteArray();
    }

    @Nullable
    public static byte[] buildPlatformPkg(@Nonnull PluginNode node,
                                           @Nonnull PackageRepositoryUtil.LinuxPlatformInfo info) throws IOException {
        Path artifact = resolveArtifact(node);
        if (artifact == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GZIPOutputStream(baos))) {
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            byte[] pkgInfoBytes = buildPlatformPkgInfo(node, info).getBytes(StandardCharsets.UTF_8);
            TarArchiveEntry pkgInfoEntry = new TarArchiveEntry(".PKGINFO");
            pkgInfoEntry.setSize(pkgInfoBytes.length);
            tar.putArchiveEntry(pkgInfoEntry);
            tar.write(pkgInfoBytes);
            tar.closeArchiveEntry();

            appendPlatformEntries(tar, artifact);
        }
        return baos.toByteArray();
    }

    // ---- Private helpers ----

    @Nullable
    @SuppressWarnings("deprecation")
    private static Path resolveArtifact(@Nonnull PluginNode node) {
        if (node.targetPath != null && Files.isRegularFile(node.targetPath)) {
            return node.targetPath;
        }
        if (node.targetFile != null && node.targetFile.isFile()) {
            return node.targetFile.toPath();
        }
        return null;
    }

    private static byte[] buildControlTarGz(@Nonnull PluginNode node) throws IOException {
        byte[] controlBytes = buildControlFile(node).getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GZIPOutputStream(baos))) {
            TarArchiveEntry dotDir = new TarArchiveEntry("./");
            tar.putArchiveEntry(dotDir);
            tar.closeArchiveEntry();

            TarArchiveEntry controlEntry = new TarArchiveEntry("./control");
            controlEntry.setSize(controlBytes.length);
            tar.putArchiveEntry(controlEntry);
            tar.write(controlBytes);
            tar.closeArchiveEntry();
        }
        return baos.toByteArray();
    }

    private static byte[] buildDataTarGz(@Nonnull Path artifact, @Nonnull String pluginId) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GZIPOutputStream(baos))) {
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            appendZipEntries(tar, artifact, "./" + PLUGIN_INSTALL_PREFIX + pluginId + "/");
        }
        return baos.toByteArray();
    }

    private static byte[] buildPlatformControlTarGz(@Nonnull PluginNode node,
                                                      @Nonnull PackageRepositoryUtil.LinuxPlatformInfo info) throws IOException {
        byte[] controlBytes = buildPlatformControlFile(node, info).getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GZIPOutputStream(baos))) {
            TarArchiveEntry dotDir = new TarArchiveEntry("./");
            tar.putArchiveEntry(dotDir);
            tar.closeArchiveEntry();

            TarArchiveEntry controlEntry = new TarArchiveEntry("./control");
            controlEntry.setSize(controlBytes.length);
            tar.putArchiveEntry(controlEntry);
            tar.write(controlBytes);
            tar.closeArchiveEntry();
        }
        return baos.toByteArray();
    }

    private static byte[] buildPlatformDataTarGz(@Nonnull Path artifact) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GZIPOutputStream(baos))) {
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            appendPlatformEntries(tar, artifact);
        }
        return baos.toByteArray();
    }

    /**
     * Appends all ZIP entries to the TAR, placing them under {@code tarPrefix}, and
     * preserving Unix permissions from {@link ZipArchiveEntry#getUnixMode()}.
     */
    private static void appendZipEntries(@Nonnull TarArchiveOutputStream tar,
                                          @Nonnull Path artifact,
                                          @Nonnull String tarPrefix) throws IOException {
        try (ZipArchiveInputStream zip = new ZipArchiveInputStream(Files.newInputStream(artifact))) {
            ZipArchiveEntry ze;
            while ((ze = zip.getNextEntry()) != null) {
                TarArchiveEntry tarEntry = new TarArchiveEntry(tarPrefix + ze.getName());
                copyMode(ze, tarEntry);
                if (ze.isDirectory()) {
                    tar.putArchiveEntry(tarEntry);
                    tar.closeArchiveEntry();
                }
                else {
                    byte[] data = zip.readAllBytes();
                    tarEntry.setSize(data.length);
                    tar.putArchiveEntry(tarEntry);
                    tar.write(data);
                    tar.closeArchiveEntry();
                }
            }
        }
    }

    /**
     * Reads a {@code .tar.gz} platform distribution, strips the top-level directory
     * (e.g. {@code Consulo/}), and writes entries to {@value PLATFORM_INSTALL_PATH}.
     * Permissions are copied directly from the source tar entry.
     */
    private static void appendPlatformEntries(@Nonnull TarArchiveOutputStream tar,
                                               @Nonnull Path artifact) throws IOException {
        try (TarArchiveInputStream src = new TarArchiveInputStream(
                new GZIPInputStream(Files.newInputStream(artifact)))) {
            TarArchiveEntry se;
            while ((se = src.getNextEntry()) != null) {
                String name = se.getName();
                int slash = name.indexOf('/');
                if (slash < 0) continue;            // unexpected top-level file
                String rest = name.substring(slash + 1);
                if (rest.isEmpty()) continue;        // top-level directory itself

                TarArchiveEntry tarEntry = new TarArchiveEntry(PLATFORM_INSTALL_PATH + rest);
                tarEntry.setMode(se.getMode());     // preserve rwxr-xr-x, rw-r--r--, etc.
                if (se.isDirectory()) {
                    tar.putArchiveEntry(tarEntry);
                    tar.closeArchiveEntry();
                }
                else {
                    byte[] data = src.readAllBytes();
                    tarEntry.setSize(data.length);
                    tar.putArchiveEntry(tarEntry);
                    tar.write(data);
                    tar.closeArchiveEntry();
                }
            }
        }
    }

    /**
     * Copies Unix permissions from a ZIP entry to a TAR entry.
     * Falls back to {@code 0755} for directories and {@code 0644} for files when mode is unset.
     */
    private static void copyMode(@Nonnull ZipArchiveEntry ze, @Nonnull TarArchiveEntry tarEntry) {
        int mode = ze.getUnixMode();
        if (mode != 0) {
            tarEntry.setMode(mode);
        }
        else {
            tarEntry.setMode(ze.isDirectory() ? 755 : 644);
        }
    }

    private static String buildControlFile(@Nonnull PluginNode node) {
        String pkgName = "consulo-plugin-" + node.id.toLowerCase();
        long sizeKb = node.length != null ? node.length / 1024 : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("Package: ").append(pkgName).append('\n');
        sb.append("Version: ").append(node.version).append('\n');
        sb.append("Architecture: all\n");
        sb.append("Maintainer: ").append(node.vendor != null ? node.vendor : "Consulo")
            .append(" <noreply@consulo.io>\n");
        sb.append("Installed-Size: ").append(sizeKb).append('\n');

        StringJoiner sj = new StringJoiner(", ");
        sj.add("consulo-with-jdk | consulo-without-jdk");
        if (node.dependencies != null) {
            for (String dep : node.dependencies) {
                sj.add("consulo-plugin-" + dep.toLowerCase());
            }
        }
        sb.append("Depends: ").append(sj).append('\n');

        String desc = node.description != null && !node.description.isBlank()
            ? node.description.trim().lines().findFirst().orElse(node.id)
            : (node.name != null ? node.name : node.id);
        sb.append("Description: ").append(desc).append('\n');
        return sb.toString();
    }

    private static String buildPlatformControlFile(@Nonnull PluginNode node,
                                                    @Nonnull PackageRepositoryUtil.LinuxPlatformInfo info) {
        boolean withJdk = info.pkgName().equals("consulo-with-jdk");
        String desc = withJdk ? "Consulo IDE with bundled JDK"
                              : "Consulo IDE without bundled JDK (requires system Java 21+)";
        long sizeKb = node.length != null ? node.length / 1024 : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("Package: ").append(info.pkgName()).append('\n');
        sb.append("Version: ").append(node.version).append('\n');
        sb.append("Architecture: ").append(info.debArch()).append('\n');
        sb.append("Maintainer: Consulo <noreply@consulo.io>\n");
        sb.append("Installed-Size: ").append(sizeKb).append('\n');
        sb.append("Provides: consulo\n");
        sb.append("Description: ").append(desc).append('\n');
        return sb.toString();
    }

    private static String buildPkgInfo(@Nonnull PluginNode node) {
        String pkgName = "consulo-plugin-" + node.id.toLowerCase();
        long buildDate = node.date != null ? node.date / 1000 : 0;
        long size = node.length != null ? node.length : 0;

        String desc = node.description != null && !node.description.isBlank()
            ? node.description.trim().lines().findFirst().orElse(node.id)
            : (node.name != null ? node.name : node.id);

        StringBuilder sb = new StringBuilder();
        sb.append("# Generated by consulo-hub\n");
        sb.append("pkgname = ").append(pkgName).append('\n');
        sb.append("pkgbase = ").append(pkgName).append('\n');
        sb.append("pkgver = ").append(node.version).append("-1\n");
        sb.append("pkgdesc = ").append(desc).append('\n');
        if (node.url != null && !node.url.isEmpty()) {
            sb.append("url = ").append(node.url).append('\n');
        }
        sb.append("builddate = ").append(buildDate).append('\n');
        sb.append("packager = Consulo <noreply@consulo.io>\n");
        sb.append("size = ").append(size).append('\n');
        sb.append("arch = any\n");
        sb.append("depend = consulo\n");
        if (node.dependencies != null) {
            for (String dep : node.dependencies) {
                sb.append("depend = consulo-plugin-").append(dep.toLowerCase()).append('\n');
            }
        }
        return sb.toString();
    }

    private static String buildPlatformPkgInfo(@Nonnull PluginNode node,
                                                @Nonnull PackageRepositoryUtil.LinuxPlatformInfo info) {
        boolean withJdk = info.pkgName().equals("consulo-with-jdk");
        String desc = withJdk ? "Consulo IDE with bundled JDK"
                              : "Consulo IDE without bundled JDK (requires system Java 21+)";
        long buildDate = node.date != null ? node.date / 1000 : 0;
        long size = node.length != null ? node.length : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("# Generated by consulo-hub\n");
        sb.append("pkgname = ").append(info.pkgName()).append('\n');
        sb.append("pkgbase = ").append(info.pkgName()).append('\n');
        sb.append("pkgver = ").append(node.version).append("-1\n");
        sb.append("pkgdesc = ").append(desc).append('\n');
        sb.append("builddate = ").append(buildDate).append('\n');
        sb.append("packager = Consulo <noreply@consulo.io>\n");
        sb.append("size = ").append(size).append('\n');
        sb.append("arch = ").append(info.pacmanArch()).append('\n');
        sb.append("provides = consulo\n");
        return sb.toString();
    }

    private static void writeArEntry(@Nonnull OutputStream out, @Nonnull String name, byte[] data) throws IOException {
        byte[] header = new byte[60];
        Arrays.fill(header, (byte) ' ');

        byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 16));

        byte[] mtime = "0".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(mtime, 0, header, 16, mtime.length);

        byte[] mode = "100644".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(mode, 0, header, 40, mode.length);

        byte[] sizeBytes = String.valueOf(data.length).getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(sizeBytes, 0, header, 48, sizeBytes.length);

        header[58] = '`';
        header[59] = '\n';

        out.write(header);
        out.write(data);
        if (data.length % 2 != 0) {
            out.write('\n');
        }
    }
}
