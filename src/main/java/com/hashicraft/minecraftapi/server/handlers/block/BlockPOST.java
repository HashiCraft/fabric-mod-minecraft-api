package com.hashicraft.minecraftapi.server.handlers.block;

import com.hashicraft.minecraftapi.server.models.Block;
import com.hashicraft.minecraftapi.server.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class BlockPOST implements Handler {

	private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public BlockPOST(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(
      path = "/v1/block",            // only necessary to include when using static method references
      methods = HttpMethod.POST,    // only necessary to include when using static method references
      summary = "Create a single block",
      description = "Create a single block at the location defined in the request body. If a block exists at the location the existing block is not replaced.",
      operationId = "createSingleBlock",
      tags = {"Block"},
      requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = Block.class)}),
      responses = {
          @OpenApiResponse(status = "200")
      },
      security = {
        @OpenApiSecurity(name = "ApiKeyAuth")
      }
  )
  public void handle(Context ctx) throws Exception {
      Block block = ctx.bodyAsClass(Block.class);

      LOGGER.info("Block POST called x:{}, y:{}, z:{} type:{}",block.getX(),block.getY(),block.getZ(),block.getMaterial());

      BlockPos pos = new BlockPos(block.getX(),block.getY(),block.getZ());
      Util.SetBlock(pos, block, block.getRotation(), world);

      String id = Base64.getEncoder().withoutPadding().encodeToString(String.format("%s/%s/%s", block.getX(), block.getY(),block.getZ()).getBytes());
      block.setID(id);

      ctx.json(block);
  }
}
