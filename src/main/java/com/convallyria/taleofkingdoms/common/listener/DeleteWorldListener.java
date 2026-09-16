package com.convallyria.taleofkingdoms.common.listener;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.event.WorldDeleteCallback;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeleteWorldListener extends Listener {

    public DeleteWorldListener() {
        WorldDeleteCallback.EVENT.register(worldName -> {
            final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            if (api == null) {
                TaleOfKingdoms.LOGGER.warn("Unable to delete world as api is null.");
                return;
            }

            Path file = new File(api.getDataFolder() + "worlds/" + worldName + ConquestInstance.FILE_TYPE).toPath();
            Path backup = file.resolveSibling(file.getFileName() + ".bak");
            try {
                Files.deleteIfExists(file);
                Files.deleteIfExists(backup);
            } catch (IOException error) {
                TaleOfKingdoms.LOGGER.error("Unable to delete conquest data for {}", worldName, error);
            }
        });
    }
}
