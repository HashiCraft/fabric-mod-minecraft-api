package com.hashicraft.minecraftapi.server.util;

import com.hashicraft.minecraftapi.server.models.Block;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

  public static Logger LOGGER = LoggerFactory.getLogger("server");

  public static String getIdentifierAtPosition(ServerWorld world, BlockPos pos) {

    BlockState state = world.getBlockState(pos);
    if (state == null) {
      return "";
    }

    Identifier id = Registry.BLOCK.getId(state.getBlock());

    return id.toString();
  }

  // calculates the end position given a collection of blocks to be applied to the given
  // start positon
  public static Vec3d getEndPosFromBlocks(Vec3d startPos, Block[] blocks, int rotation) {
    int maxX = 0;  
    int maxY = 0;  
    int maxZ = 0;  

    for(Block b : blocks) {
        if (b.getX() > maxX) {
          maxX = b.getX();
        }
        
        if (b.getY() > maxY) {
          maxY = b.getY();
        }
        
        if (b.getZ() > maxZ) {
          maxZ = b.getZ();
        }
    }

    if (rotation == 90) {
      int tmpX = maxX;
      maxX = maxZ * -1;
      maxZ = tmpX;
    }
    
    if (rotation == 180) {
      maxX = maxX * -1;
      maxZ = maxZ * -1;
    }
    
    if (rotation == 270) {
      int tmpX = maxX;
      maxX = maxZ;
      maxZ = tmpX * -1;
    }

    return new Vec3d(startPos.getX() + maxX, startPos.getY() + maxY, startPos.getZ() + maxZ);
  }

  // gets the blocks at the given coordinates, if local is true the block positions are set to increment from 0
  // if false, the original locations are reutned
  public static Block[] GetBlocks(Vec3d startPos, Vec3d endPos, boolean local, ServerWorld world) {
      ArrayList<Block> blocks = new ArrayList<Block>();

      double startX = startPos.getX();
      double startY = startPos.getY();
      double startZ = startPos.getZ();
      
      double endX = endPos.getX();
      double endY = endPos.getY();
      double endZ = endPos.getZ();

      double sz = startZ;
      double ez = endZ;
      if (endZ < startZ) {
        sz = endZ;
        ez = startZ;
      }

      double sx = startX;
      double ex = endX;
      if (endX < startX) {
        sx = endX;
        ex = startX;
      }

      double sy = startY;
      double ey = endY;
      if (endY < startY) {
        sy = endY;
        ey = startY;
      }

      double yPos = 0;
      double xPos = 0;
      double zPos = 0;

      for(double y=sy; y <= ey; y ++) {
        xPos = 0;

        for(double x=sx; x <= ex; x ++) {
          zPos = 0;

          for(double z=sz; z <= ez; z ++) {
            BlockPos pos = new BlockPos(x,y,z);

            BlockState state = world.getBlockState(pos);
            if(state == null) {
              continue;
            }

            String material = getIdentifierAtPosition(world, pos);

            Block block = new Block();
            // set to the local coordinate not the absolute coordinates as this may be placed at a different
            // location
          
            if(local) {
              block.setX((int)xPos);
              block.setY((int)yPos);
              block.setZ((int)zPos);
            } else {
              block.setX(pos.getX());
              block.setY(pos.getY());
              block.setZ(pos.getZ());
            }

            block.setMaterial(material);

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

            blocks.add(block);
            zPos ++;
          }

          xPos ++;
        }

        yPos ++;
      }

    return blocks.toArray(new Block[blocks.size()]);
  }

  // if startPos is specified the block is placed relative to start pos
  public static void SetBlocks(Vec3d startPos, Block[] blocks, int rotation, ServerWorld world) throws Exception {
    for(Block block : blocks) {
      var item = Registry.BLOCK.get(new Identifier(block.getMaterial()));
      if (item==null) {
        throw new Exception(String.format("unable to create block, material %s does not exist", block.getMaterial()));
      }

      BlockPos pos = new BlockPos(block.getX(),block.getY(),block.getZ());

      if(startPos != null) {
        double blockX = block.getX();
        double blockY = block.getY();
        double blockZ = block.getZ();

        if(rotation == 90) {
          double tmpX = blockX;
          blockX = blockZ * -1;
          blockZ = tmpX;
        }
    
        if (rotation == 180) {
          blockX = blockX * -1;
          blockZ = blockZ * -1;
        }
        
        if (rotation == 270) {
          double tmpX = blockX;
          blockX = blockZ;
          blockZ = tmpX * -1;
        }

        pos = new BlockPos(blockX + startPos.getX(),blockY + startPos.getY(),blockZ + startPos.getZ());
      }

      BlockState state = world.getBlockState(pos);
      String material = Util.getIdentifierAtPosition(world, pos);

      // if there is an existing block that is not air remove it
      if (state != null && !material.equals("minecraft:air")) {
        boolean didBreak = world.breakBlock(pos, false);

        if (!didBreak) {
          throw new Exception(String.format("unable to create block at position, %s, can't break existing block at that location", pos.toString()));
        }
      }

      // do not place air
      if(block.getMaterial().equals("minecraft:air")) {
        continue;
      }

      state = item.getDefaultState();
      try {
        switch(block.getFacing()) {
          case "north":
            switch (rotation) {
              case 0:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.NORTH);
                break;
              case 90:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.EAST);
                break;
              case 180:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.SOUTH);
                break;
              case 270:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.WEST);
                break;
            }
            break;
          case "south":
            switch (rotation) {
              case 0:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.SOUTH);
                break;
              case 90:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.WEST);
                break;
              case 180:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.NORTH);
                break;
              case 270:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.EAST);
                break;
            }
            break;
          case "east":
            switch (rotation) {
              case 0:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.EAST);
                break;
              case 90:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.SOUTH);
                break;
              case 180:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.WEST);
                break;
              case 270:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.NORTH);
                break;
            }
            break;
          case "west":
            switch (rotation) {
              case 0:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.WEST);
                break;
              case 90:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.NORTH);
                break;
              case 180:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.EAST);
                break;
              case 270:
                state = state.with(Properties.HORIZONTAL_FACING, Direction.SOUTH);
                break;
            }
            break;
        }
      } catch(Exception ex) {
        //LOGGER.error("Unable to set direction for block: {}", ex.getMessage());
      }

      switch(block.getHalf()) {
        case "top":
          state = state.with(Properties.BLOCK_HALF, BlockHalf.TOP);
          break;
        case "bottom":
          state = state.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM);
          break;
      }

      if (block.getRotation() > -1) {
        int rot = 0;
        switch(rotation) {
          case 0:
            rot = block.getRotation();
            break;
          case 90:
            rot = block.getRotation() + 4;
            break;
          case 180:
            rot = block.getRotation() + 8;
            break;
          case 270:
            rot = block.getRotation() + 12;
            break;
        }

        if(rot >=16 ) {
          rot = rot - 16;
        }

        LOGGER.info("orig {}, new {}", block.getRotation(), rot);
            
        state = state.with(Properties.ROTATION, rot);
      }

      boolean didSet = world.setBlockState(pos, state);

      if (!didSet) {
        //LOGGER.error("Unable to place block {} at {},{},{}",block.getMaterial(),block.getX(),block.getY(),block.getZ());
        throw new Exception(String.format("unable to create block at position, %s", pos.toString()));
      }
    }
  }
}
