package com.hashicraft.minecraftapi.server.handlers.block;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashicraft.minecraftapi.server.models.Block;
import com.hashicraft.minecraftapi.server.util.Util;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class BlockGET implements Handler {

	private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public BlockGET(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(
      path = "/v1/block/{x}/{y}/{z}",
      methods = HttpMethod.GET,    
      summary = "Get the details of a single block",
      description = "This method allows you to fetch the details of a single block for the given coordinates. If only minecraft:air exists at the coordinates, a 404 is returned",
      operationId = "getSingleBlock",
      tags = {"Block"},
      security = {
        @OpenApiSecurity(name = "ApiKeyAuth")
      },
      responses = {
          @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Block.class)}),
          @OpenApiResponse(status = "404")
      },
      pathParams = {
        @OpenApiParam(name = "x", example = "12", required = true),
        @OpenApiParam(name = "y", example = "13", required = true),
        @OpenApiParam(name = "z", example = "14", required = true)
      }
  )
  public void handle(Context ctx) throws Exception {
      int x = Integer.parseInt(ctx.pathParam("x"));
      int y = Integer.parseInt(ctx.pathParam("y"));
      int z = Integer.parseInt(ctx.pathParam("z"));

      LOGGER.info("Blocks GET called x:{}, y:{}, z:{}",x,y,z);
      Vec3i pos = new Vec3i(x, y, z);
      BlockState state = Util.GetBlockState(pos, world);

      String material = Util.getIdentifierAtPosition(world, pos);
      if (material.contains("minecraft:air") || material.isBlank()) {
        LOGGER.info(String.format("404 block does not exist at x:{}, y:{} z:{}", x,y,z));

        ctx.res().sendError(404, "Block not found");
        return;
      }

      Block block = new Block();
      block.setX(x);
      block.setY(y);
      block.setZ(z);
      block.setMaterial(material);

      // generate and set an id for the string
      String id = Base64.getEncoder().withoutPadding().encodeToString(String.format("%s/%s/%s", x,y,z).getBytes());
      block.setID(id);

      var entries = state.getEntries();
      entries.forEach((k,v) -> {
        if (k.getName().equals("facing")) {
          block.setFacing(v.toString());
        }

        if (k.getName().equals("half")) {
          block.setHalf(v.toString());
        }

        if (k.getName().equals("rotation")) {
          block.setRotation(Integer.parseInt(v.toString()));
        }
      });

      ctx.json(block);
  }

}
