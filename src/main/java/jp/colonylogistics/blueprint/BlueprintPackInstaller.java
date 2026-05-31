package jp.colonylogistics.blueprint;

import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Installs the bundled MineColonies/Structurize style pack into the active
 * Minecraft instance's blueprint folder.
 *
 * <p>Phase 17.9.20 intentionally stops using the old development pack id
 * {@code colony_logistics_dev}. The active collaborator style pack is installed
 * as {@code <gameDir>/blueprints/colony_logistics}. Any existing legacy pack is
 * deleted before installation so Structurize cannot keep showing stale building
 * data from the old test pack.</p>
 */
public final class BlueprintPackInstaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlueprintPackInstaller.class);

    private static final String PACK_ID = "colony_logistics";
    private static final String LEGACY_PACK_ID = "colony_logistics_dev";
    private static final String RESOURCE_ROOT = "blueprints/" + PACK_ID;

    private static final String[] PACK_FILES = {
            "README.md",
            "pack.json",
            "icon.png",
            "logistics_office1.blueprint",
            "logistics_office2.blueprint",
            "logistics_office3.blueprint",
            "logistics_office4.blueprint",
            "logistics_office5.blueprint",
            "container_dock1.blueprint",
            "container_dock2.blueprint",
            "container_dock3.blueprint",
            "container_dock4.blueprint",
            "container_dock5.blueprint",
            "trade_terminal1.blueprint",
            "trade_terminal2.blueprint",
            "trade_terminal3.blueprint",
            "trade_terminal4.blueprint",
            "trade_terminal5.blueprint",
            "huts/logistics_office1.blueprint",
            "huts/logistics_office2.blueprint",
            "huts/logistics_office3.blueprint",
            "huts/logistics_office4.blueprint",
            "huts/logistics_office5.blueprint",
            "huts/container_dock1.blueprint",
            "huts/container_dock2.blueprint",
            "huts/container_dock3.blueprint",
            "huts/container_dock4.blueprint",
            "huts/container_dock5.blueprint",
            "huts/trade_terminal1.blueprint",
            "huts/trade_terminal2.blueprint",
            "huts/trade_terminal3.blueprint",
            "huts/trade_terminal4.blueprint",
            "huts/trade_terminal5.blueprint"
    };

    public static void installBundledPack() {
        Path blueprintsRoot = FMLPaths.GAMEDIR.get().resolve("blueprints");
        Path targetRoot = blueprintsRoot.resolve(PACK_ID);
        Path legacyRoot = blueprintsRoot.resolve(LEGACY_PACK_ID);
        try {
            deleteDirectoryIfExists(legacyRoot);

            // Force a clean install of the bundled pack. This avoids stale files
            // when the pack id stays visible in Structurize/MineColonies caches
            // across beta builds, and it guarantees collaborators' blueprint
            // changes replace any previous local copy.
            deleteDirectoryIfExists(targetRoot);
            Files.createDirectories(targetRoot);

            int copied = 0;
            for (String file : PACK_FILES) {
                copyResource(file, targetRoot.resolve(file));
                copied++;
            }
            LOGGER.info("Installed Colony Logistics MineColonies style pack at {} (copied={}, legacyDeleted={}).", targetRoot, copied, Files.notExists(legacyRoot));
        } catch (IOException | RuntimeException ex) {
            LOGGER.error("Failed to install Colony Logistics MineColonies style pack at {}. Build Tool style discovery may fail.", targetRoot, ex);
        }
    }

    private static void copyResource(String relativePath, Path target) throws IOException {
        String resourcePath = RESOURCE_ROOT + "/" + relativePath;
        byte[] sourceBytes;
        try (InputStream in = BlueprintPackInstaller.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Missing bundled blueprint resource: " + resourcePath);
            }
            sourceBytes = in.readAllBytes();
        }

        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.write(tmp, sourceBytes);
        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteDirectoryIfExists(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        LOGGER.info("Deleted legacy or stale Colony Logistics style pack at {}.", root);
    }

    private BlueprintPackInstaller() {}
}
