package com.hashicraft.minecraftapi.server.handlers.block;

import com.hashicraft.minecraftapi.server.models.Block;
import com.hashicraft.minecraftapi.server.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class BlockGET implements Handler {

	private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public BlockGET(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(
      path = "/blocks",            // only necessary to include when using static method references
      method = HttpMethod.GET,    // only necessary to include when using static method references
      summary = "Get a single block",
      operationId = "getSingleBlock",
      tags = {"Block"},
      responses = {
          @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Block.class)})
      }
  )
  public void handle(Context ctx) throws Exception {
      int x = Integer.parseInt(ctx.pathParam("x"));
      int y = Integer.parseInt(ctx.pathParam("y"));
      int z = Integer.parseInt(ctx.pathParam("z"));

      LOGGER.info("Blocks GET called x:{}, y:{}, z:{}",x,y,z);

      BlockPos pos = new BlockPos(x,y,z);
      BlockState state = world.getBlockState(pos);

      String material = Util.getIdentifierAtPosition(world, pos);

      if (material.contains("minecraft:air") || material.isBlank()) {
        ctx.res.sendError(404, "Block not found");
        return;
      }

      Block block = new Block();
      block.setX(x);
      block.setY(y);
      block.setZ(z);
      block.setMaterial(material);

      var entries = state.getEntries();
      entries.forEach((k,v) -> {
        LOGGER.info("{} {}",k, v.toString());
        if (k.getName().equals("facing")) {
          block.setFacing(v.toString());
        }

        if (k.getName().equals("half")) {
          block.setHalf(v.toString());
        }
      });

      ctx.json(block);
  }

}
