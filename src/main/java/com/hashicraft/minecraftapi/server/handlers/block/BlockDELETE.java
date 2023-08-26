package com.hashicraft.minecraftapi.server.handlers.block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashicraft.minecraftapi.server.util.Util;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class BlockDELETE implements Handler {

  private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public BlockDELETE(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(path = "/v1/block/{x}/{y}/{z}", methods = HttpMethod.DELETE, summary = "Delete a single block", description = "Deletes a block at the given location, if the block does not exist, an status code 404 is returned", operationId = "deleteSingleBlock", tags = {
      "Block" }, security = {
          @OpenApiSecurity(name = "ApiKeyAuth")
      }, pathParams = {
          @OpenApiParam(name = "x", example = "12", required = true),
          @OpenApiParam(name = "y", example = "13", required = true),
          @OpenApiParam(name = "z", example = "14", required = true)
      }, responses = {
          @OpenApiResponse(status = "200", description = "Block created successfully"),
          @OpenApiResponse(status = "404", description = "Block does not exist")
      })
  public void handle(Context ctx) throws Exception {
    int x = Integer.parseInt(ctx.pathParam("x"));
    int y = Integer.parseInt(ctx.pathParam("y"));
    int z = Integer.parseInt(ctx.pathParam("z"));

    LOGGER.info("Block DELETE called x:{}, y:{}, z:{}", x, y, z);

    Vec3i pos = new Vec3i(x, y, z);
    String material = Util.getIdentifierAtPosition(world, pos);

    // if air no block
    if (material.contains("minecraft:air") || material.isBlank()) {
      LOGGER.info(String.format("404 block does not exist at x:{}, y:{} z:{}", x, y, z));

      ctx.res().sendError(404, "Block not found");
      return;
    }

    boolean didBreak = Util.BreakBlock(pos, world);
    if (!didBreak) {
      LOGGER.error("Unable to delete block {} at {},{},{} the minecraft server refused to perform this operation",
          material, x, y, z);

      ctx.res().sendError(500, "Unable to place block");
    }
  }
}
