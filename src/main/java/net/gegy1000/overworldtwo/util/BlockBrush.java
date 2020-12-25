package net.gegy1000.overworldtwo.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.template.AlwaysTrueRuleTest;
import net.minecraft.world.gen.feature.template.RuleTest;

import java.util.Random;

public final class BlockBrush {
    public final BlockState block;
    public final RuleTest replace;
    public final int flags;

    public BlockBrush(BlockState block, RuleTest replace, int flags) {
        this.block = block;
        this.replace = replace;
        this.flags = flags;
    }

    public static BlockBrush of(BlockState block) {
        return new BlockBrush(block, AlwaysTrueRuleTest.INSTANCE, 0b10);
    }

    public static BlockBrush ofWhere(BlockState block, RuleTest replace) {
        return new BlockBrush(block, replace, 0b10);
    }

    public boolean test(IWorld world, Random random, BlockPos pos) {
        return this.replace.test(world.getBlockState(pos), random);
    }
}
