package com.hashicraft.minecraftapi.server.handlers.blocks;

import com.hashicraft.minecraftapi.server.models.Block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

public class BlocksPOST implements Handler {

	private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public BlocksPOST(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(
      path = "/blocks",            // only necessary to include when using static method references
      method = HttpMethod.POST,    // only necessary to include when using static method references
      summary = "Create multiple blocks",
      operationId = "createMultipleBlocks",
      tags = {"Block"},
      requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = Block[].class)}),
      responses = {
          @OpenApiResponse(status = "200")
      }
  )
  public void handle(Context ctx) throws Exception {
    Block[] blocks = ctx.bodyAsClass(Block[].class);

    int x = Integer.parseInt(ctx.pathParam("x"));
    int y = Integer.parseInt(ctx.pathParam("y"));
    int z = Integer.parseInt(ctx.pathParam("z"));

    LOGGER.info("Blocks POST called x:{}, y:{}, z:{}",x,y,z);

    for(Block block : blocks) {
      var item = Registry.BLOCK.get(new Identifier(block.getMaterial()));
      if (item==null) {
        LOGGER.error("Unable to create block {} material does not exist",block.getMaterial());
        ctx.res.sendError(500,"Unable to create block " + block.getMaterial() + " material does not exist");
        return;
      }

      BlockPos pos = new BlockPos(block.getX() + x,block.getY() + y,block.getZ() + z);
      LOGGER.info("Place block x:{}, y:{}, z:{} material: {}",pos.getX(),pos.getY(),pos.getZ(),block.getMaterial());

      BlockState state = world.getBlockState(pos);
      String material = state.getBlock().getRegistryEntry().registryKey().getValue().toString();

      // if there is an existing block that is not air remove it
      if (state != null && !material.equals("minecraft:air")) {
        boolean didBreak = world.breakBlock(pos, false);

        if (!didBreak) {
          LOGGER.error("Unable to delete block {} at {},{},{}",state.getBlock().getRegistryEntry().registryKey().getValue().toString(), x,y,z);
          ctx.res.sendError(500,"Unable to place block");
        }
      }

      // do not place air
      if(block.getMaterial().equals("minecraft:air")) {
        continue;
      }

      state = item.getDefaultState();

      try {
        switch(block.getFacing()) {
          case "north":
            state = state.with(Properties.HORIZONTAL_FACING, Direction.NORTH);
            break;
          case "south":
            state = state.with(Properties.HORIZONTAL_FACING, Direction.SOUTH);
            break;
          case "east":
            state = state.with(Properties.HORIZONTAL_FACING, Direction.EAST);
            break;
          case "west":
            state = state.with(Properties.HORIZONTAL_FACING, Direction.WEST);
            break;
        }
      } catch(Exception ex) {
        LOGGER.error("Unable to set direction for block: {}", ex.getMessage());
      }

      switch(block.getHalf()) {
        case "top":
          state = state.with(Properties.BLOCK_HALF, BlockHalf.TOP);
          break;
        case "bottom":
          state = state.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM);
          break;
      }

      boolean didSet = world.setBlockState(pos, state);

      if (!didSet) {
        LOGGER.error("Unable to place block {} at {},{},{}",block.getMaterial(),block.getX(),block.getY(),block.getZ());
        ctx.res.sendError(500,"Unable to place block");
      }
    }
  }
}
