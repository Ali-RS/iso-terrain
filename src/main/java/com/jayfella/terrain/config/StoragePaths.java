package com.jayfella.terrain.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by James on 28/04/2017.
 */
public class StoragePaths {

    public static Path SAVEGAME_DIR = Paths.get("./savegames");

    public static void create() {

        if (!SAVEGAME_DIR.toFile().exists()) {
            SAVEGAME_DIR.toFile().mkdirs();
        }

    }
}
