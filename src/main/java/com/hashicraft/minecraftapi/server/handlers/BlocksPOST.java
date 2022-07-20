package com.hashicraft.minecraftapi.server.handlers;

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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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

      LOGGER.info("Blocks POST called x:{}, y:{}, z:{} type:{}",block.getX(),block.getY(),block.getZ(),block.getMaterial());

      var item = Registry.BLOCK.get(new Identifier(block.getMaterial()));
      if (item==null) {
        ctx.res.sendError(500,"Unable to create block " + block.getMaterial());
        return;
      }

      BlockPos pos = new BlockPos(block.getX(),block.getY(),block.getZ());
      boolean didSet = world.setBlockState(pos,item.getDefaultState());

      if (!didSet) {
        LOGGER.error("Unable to place block {} at {},{},{}",block.getMaterial(),block.getX(),block.getY(),block.getZ());
        ctx.res.sendError(500,"Unable to place block");
      }
  }
}
