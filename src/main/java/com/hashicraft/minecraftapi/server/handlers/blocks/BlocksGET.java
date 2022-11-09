package com.hashicraft.minecraftapi.server.handlers.blocks;

import java.util.ArrayList;

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

public class BlocksGET implements Handler {

	private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public BlocksGET(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(
      path = "/blocks",            // only necessary to include when using static method references
      method = HttpMethod.GET,    // only necessary to include when using static method references
      summary = "Get multiple blocks",
      operationId = "getMultipleBlock",
      tags = {"Block"},
      responses = {
          @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Block[].class)})
      }
  )
  public void handle(Context ctx) throws Exception {
      int startX = Integer.parseInt(ctx.pathParam("start_x"));
      int startY = Integer.parseInt(ctx.pathParam("start_y"));
      int startZ = Integer.parseInt(ctx.pathParam("start_z"));

      int endX = Integer.parseInt(ctx.pathParam("end_x"));
      int endY = Integer.parseInt(ctx.pathParam("end_y"));
      int endZ = Integer.parseInt(ctx.pathParam("end_z"));

      LOGGER.info("Blocks GET called start_x:{}, start_y:{}, start_z:{} end_x:{}, end_y:{}, end_z:{}",startX,startY,startZ,endX,endY,endZ);

      ArrayList<Block> blocks = new ArrayList<Block>();

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

      LOGGER.info("Finding blocks start_x:{}, start_y:{}, start_z:{} end_x:{}, end_y:{}, end_z:{}",sx,sy,sz,ex,ey,ez);

      int yPos = 0;
      int xPos = 0;
      int zPos = 0;

      for(int y=sy; y <= ey; y ++) {
        xPos = 0;

        for(int x=sx; x <= ex; x ++) {
          zPos = 0;

          for(int z=sz; z <= ez; z ++) {
            BlockPos pos = new BlockPos(x,y,z);

            BlockState state = world.getBlockState(pos);
            if(state == null) {
              continue;
            }

            String material = Util.getIdentifierAtPosition(world, pos);

            Block block = new Block();
            // set to the local coordinate not the absolute coordinates as this may be placed at a different
            // location

            block.setX(xPos);
            block.setY(yPos);
            block.setZ(zPos);
            block.setMaterial(material);

            var entries = state.getEntries();
            entries.forEach((k,v) -> {
              if (k.getName().equals("facing")) {
                block.setFacing(v.toString());
              }

              if (k.getName().equals("half")) {
                block.setHalf(v.toString());
              }
            });

            blocks.add(block);
            zPos ++;
          }

          xPos ++;
        }

        yPos ++;
      }

      ctx.json(blocks.toArray());
  }

}
