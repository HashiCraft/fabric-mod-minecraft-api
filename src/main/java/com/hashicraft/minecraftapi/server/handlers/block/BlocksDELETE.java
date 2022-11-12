package com.hashicraft.minecraftapi.server.handlers.block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiSecurity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import com.hashicraft.minecraftapi.server.util.Util;

public class BlocksDELETE implements Handler {

	private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public BlocksDELETE(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(
      path = "/v1/block/{start x}/{start y}/{start z}/{end x}/{end y}/{end z}",            // only necessary to include when using static method references
      methods = HttpMethod.DELETE,
      summary = "Delete multiple blocks",
      description = "Deletes the blocks at the given location",
      operationId = "deleteMultipleBlocks",
      tags = {"Block"},
      pathParams = {
        @OpenApiParam(name = "start x", example = "12", required = true),
        @OpenApiParam(name = "start y", example = "13", required = true),
        @OpenApiParam(name = "start z", example = "14", required = true),
        @OpenApiParam(name = "end x", example = "22", required = true),
        @OpenApiParam(name = "end y", example = "15", required = true),
        @OpenApiParam(name = "end z", example = "19", required = true)
      },
      responses = {
          @OpenApiResponse(status = "200")
      },
      security = {
        @OpenApiSecurity(name = "ApiKeyAuth")
      }
  )
  public void handle(Context ctx) throws Exception {
      int startX = Integer.parseInt(ctx.pathParam("start_x"));
      int startY = Integer.parseInt(ctx.pathParam("start_y"));
      int startZ = Integer.parseInt(ctx.pathParam("start_z"));

      int endX = Integer.parseInt(ctx.pathParam("end_x"));
      int endY = Integer.parseInt(ctx.pathParam("end_y"));
      int endZ = Integer.parseInt(ctx.pathParam("end_z"));

      LOGGER.info("Blocks DELETE called start_x:{}, start_y:{}, start_z:{} end_x:{}, end_y:{}, end_z:{}",startX,startY,startZ,endX,endY,endZ);

      int sz = startZ;
      int ez = endZ;
      if (endZ < startZ) {
        sz = endZ;
        ez = startZ;
      }

      int sx = startX;
      int ex = endX;
      if (endX < startX) {
        sx = endX;
        ex = startX;
      }

      int sy = startY;
      int ey = endY;
      if (endY < startY) {
        sy = endY;
        ey = startY;
      }

      for(int y=sy; y <= ey; y ++) {

        for(int x=sx; x <= ex; x ++) {

          for(int z=sz; z <= ez; z ++) {

            BlockPos pos = new BlockPos(x,y,z);
            String material = Util.getIdentifierAtPosition(world, pos);

            // if air no block
            if (material.equals("minecraft:air") || material.isBlank()) {
              continue;
            }

            boolean didBreak = world.breakBlock(pos, false);

            if (!didBreak) {
              LOGGER.error("Unable to delete block {} at {},{},{}", material, x,y,z);
            }

          }

        }

      }
  }
}
