package cofh.thermal.locomotion.init.data.providers;

import cofh.lib.init.data.LootTableProviderCoFH;
import cofh.thermal.locomotion.init.data.tables.TLocBlockLootTables;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TLocLootTableProvider extends LootTableProviderCoFH {

    public TLocLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {

        super(output, List.of(
                new SubProviderEntry(TLocBlockLootTables::new, LootContextParamSets.BLOCK)
        ), registries);
    }

}
