package com.convallyria.taleofkingdoms.mixin;

import net.minecraft.structure.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(StructureTemplate.PalettedBlockInfoList.class)
public interface PalettedBlockInfoListInvoker {

    @Invoker("<init>")
    static StructureTemplate.PalettedBlockInfoList taleofkingdoms$create(
            List<StructureTemplate.StructureBlockInfo> infos) {
        throw new AssertionError();
    }
}
