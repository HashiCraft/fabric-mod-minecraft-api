package com.hashicraft.minecraftapi.server.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.block.BlockState;

public class Util {
  public static String getIdentifierAtPosition(ServerWorld world, BlockPos pos) {

    BlockState state = world.getBlockState(pos);
    if (state == null) {
      return "";
    }

    Identifier id = Registry.BLOCK.getId(state.getBlock());

    return id.toString();
  }
}
