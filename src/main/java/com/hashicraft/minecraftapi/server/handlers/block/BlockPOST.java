package com.hashicraft.minecraftapi.server.handlers.block;

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

public class BlockPOST implements Handler {

	private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public BlockPOST(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(
      path = "/block",            // only necessary to include when using static method references
      method = HttpMethod.POST,    // only necessary to include when using static method references
      summary = "Create a single block",
      operationId = "createSingleBlock",
      tags = {"Block"},
      requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = Block.class)}),
      responses = {
          @OpenApiResponse(status = "200")
      }
  )
  public void handle(Context ctx) throws Exception {
      Block block = ctx.bodyAsClass(Block.class);

      LOGGER.info("Block POST called x:{}, y:{}, z:{} type:{}",block.getX(),block.getY(),block.getZ(),block.getMaterial());

      var item = Registry.BLOCK.get(new Identifier(block.getMaterial()));
      if (item==null) {
        LOGGER.error("Unable to create block {} material does not exist",block.getMaterial());
        ctx.res.sendError(500,"Unable to create block " + block.getMaterial() + " material does not exist");
        return;
      }

      BlockState state = item.getDefaultState();

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

      switch(block.getHalf()) {
        case "top":
          state = state.with(Properties.BLOCK_HALF, BlockHalf.TOP);
          break;
        case "bottom":
          state = state.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM);
          break;
      }

      state.getEntries().forEach((k,v) -> {
        LOGGER.info("{} {}",k, v.toString());
      });

      BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
      boolean didSet = world.setBlockState(pos, state);

      if (!didSet) {
        LOGGER.error("Unable to place block {} at {},{},{}", block.getMaterial(), block.getX(), block.getY(), block.getZ());
        ctx.res.sendError(500,"Unable to place block");
      }
  }
}
