package com.hashicraft.minecraftapi.server;

import com.hashicraft.minecraftapi.server.models.Block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class Server {
  private Javalin app;
	public final Logger LOGGER = LoggerFactory.getLogger("server");

  public Server() {
    app = Javalin.create();
  }

  public void start(MinecraftServer server) {
    this.app.start(8080);
    LOGGER.info("Starting server");

    // get the details of a block
    this.app.get("/blocks/{x}/{y}/{z}", ctx -> {
      int x = Integer.parseInt(ctx.pathParam("x"));
      int y = Integer.parseInt(ctx.pathParam("y"));
      int z = Integer.parseInt(ctx.pathParam("z"));

      LOGGER.info("Blocks GET called x:{}, y:{}, z:{}",x,y,z);

      ServerWorld world = server.getOverworld();
      BlockPos pos = new BlockPos(x,y,z);

      BlockState state = world.getBlockState(pos);

      if (state == null) {
        ctx.res.sendError(404, "Block not found");
        return;
      }

      Block block = new Block();
      block.setX(x);
      block.setY(y);
      block.setZ(z);
      block.setType(state.getBlock().getRegistryEntry().registryKey().getValue().toString());

      ctx.json(block);
    });

    // create a new block
    this.app.post("/blocks",ctx -> {
      Block block = ctx.bodyAsClass(Block.class);

      LOGGER.info("Blocks POST called x:{}, y:{}, z:{} type:{}",block.getX(),block.getY(),block.getZ(),block.getType());

      ServerWorld world = server.getOverworld();
      var item = Registry.BLOCK.get(new Identifier(block.getType()));
      if (item==null) {
        ctx.res.sendError(500,"Unable to create block " + block.getType());
        return;
      }

      BlockPos pos = new BlockPos(block.getX(),block.getY(),block.getZ());
      boolean didSet = world.setBlockState(pos,item.getDefaultState());

      if (!didSet) {
        LOGGER.error("Unable to place block {} at {},{},{}",block.getType(),block.getX(),block.getY(),block.getZ());
        ctx.res.sendError(500,"Unable to place block");
      }
    });

    this.app.delete("/blocks/{x}/{y}/{z}",ctx -> {
      int x = Integer.parseInt(ctx.pathParam("x"));
      int y = Integer.parseInt(ctx.pathParam("y"));
      int z = Integer.parseInt(ctx.pathParam("z"));

      LOGGER.info("Blocks GET called x:{}, y:{}, z:{}",x,y,z);

      ServerWorld world = server.getOverworld();
      BlockPos pos = new BlockPos(x,y,z);

      BlockState state = world.getBlockState(pos);

      if (state == null) {
        ctx.res.sendError(404, "Block not found");
        return;
      }

      boolean didBreak = world.breakBlock(new BlockPos(x,y,z),false);

      if (!didBreak) {
        LOGGER.error("Unable to delete block {} at {},{},{}",state.getBlock().getRegistryEntry().registryKey().getValue().toString(), x,y,z);
        ctx.res.sendError(500,"Unable to place block");
      }
    });
  }

}
