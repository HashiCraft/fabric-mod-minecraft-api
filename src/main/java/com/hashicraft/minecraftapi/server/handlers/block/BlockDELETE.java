package com.hashicraft.minecraftapi.server.handlers.block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashicraft.minecraftapi.server.util.Util;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class BlockDELETE implements Handler {

	private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public BlockDELETE(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(
      path = "/block",            // only necessary to include when using static method references
      method = HttpMethod.DELETE,
      summary = "Delete a single block",
      operationId = "deleteSingleBlock",
      tags = {"Block"},
      responses = {
          @OpenApiResponse(status = "200")
      }
  )
  public void handle(Context ctx) throws Exception {
      int x = Integer.parseInt(ctx.pathParam("x"));
      int y = Integer.parseInt(ctx.pathParam("y"));
      int z = Integer.parseInt(ctx.pathParam("z"));

      LOGGER.info("Block DELETE called x:{}, y:{}, z:{}",x,y,z);

      BlockPos pos = new BlockPos(x,y,z);
      String material = Util.getIdentifierAtPosition(world, pos);

      // if air no block
      if (material.contains("minecraft:air") || material.isBlank()) {
        ctx.res.sendError(404, "Block not found");
        return;
      }

      boolean didBreak = world.breakBlock(new BlockPos(x,y,z),false);

      if (!didBreak) {
        LOGGER.error("Unable to delete block {} at {},{},{}", material, x,y,z);
        ctx.res.sendError(500,"Unable to place block");
      }
  }
}
