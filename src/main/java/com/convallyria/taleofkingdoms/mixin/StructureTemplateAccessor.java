package com.convallyria.taleofkingdoms.mixin;

import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructureTemplate.class)
public interface StructureTemplateAccessor {

    @Accessor("blockInfoLists")
    List<StructureTemplate.PalettedBlockInfoList> taleofkingdoms$getBlockInfoLists();

    @Mutable
    @Accessor("blockInfoLists")
    void taleofkingdoms$setBlockInfoLists(List<StructureTemplate.PalettedBlockInfoList> lists);

    @Accessor("entities")
    List<StructureTemplate.StructureEntityInfo> taleofkingdoms$getEntities();

    @Mutable
    @Accessor("entities")
    void taleofkingdoms$setEntities(List<StructureTemplate.StructureEntityInfo> entities);

    @Mutable
    @Accessor("size")
    void taleofkingdoms$setSize(Vec3i size);
}
