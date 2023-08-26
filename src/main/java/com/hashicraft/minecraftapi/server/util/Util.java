package com.hashicraft.minecraftapi.server.util;

import com.hashicraft.minecraftapi.server.models.Block;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

  private static Semaphore readMutex = new Semaphore(5);
  private static Semaphore writeMutex = new Semaphore(1);
  private static Semaphore deleteMutex = new Semaphore(1);

  public static Logger LOGGER = LoggerFactory.getLogger("server");

  public static String getIdentifierAtPosition(ServerWorld world, BlockPos pos) {
    try {
      readMutex.acquire();
      BlockState state = world.getBlockState(pos);
      if (state == null) {
        return "";
      }

      Identifier id = Registries.BLOCK.getId(state.getBlock());

      return id.toString();
    } catch (InterruptedException ie) {
      LOGGER.error("Thread interuped whine trying to get position {}", ie.toString());
    } finally {
      readMutex.release();
    }

    return "";
  }

  public static String getIdentifierAtPosition(ServerWorld world, Vec3i pos) {
    BlockPos bp = new BlockPos(pos);
    return getIdentifierAtPosition(world, bp);
  }

  public static Vec3i getEndPosFromBlocks(Block[] blocks) {
    return getEndPosFromBlocks(new Vec3i(0, 0, 0), blocks, 0);
  }

  // calculates the end position given a collection of blocks to be applied to the
  // given
  // start positon
  public static Vec3i getEndPosFromBlocks(Vec3i startPos, Block[] blocks, int rotation) {
    int maxX = -9999;
    int maxY = -9999;
    int maxZ = -9999;

    for (Block b : blocks) {
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

    return new Vec3i(startPos.getX() + maxX, startPos.getY() + maxY, startPos.getZ() + maxZ);
  }

  public static Vec3i getStartPosFromBlocks(Block[] blocks) {
    int minX = 9999;
    int minY = 9999;
    int minZ = 9999;

    for (Block b : blocks) {
      if (b.getX() < minX) {
        minX = b.getX();
      }

      if (b.getY() < minY) {
        minY = b.getY();
      }

      if (b.getZ() < minZ) {
        minZ = b.getZ();
      }
    }

    return new Vec3i(minX, minY, minZ);
  }

  // gets the blocks at the given coordinates, if local is true the block
  // positions are set to increment from 0
  // if false, the original locations are reutned
  public static Block[] GetBlocks(Vec3i startPos, Vec3i endPos, boolean local, ServerWorld world) {
    try {
      readMutex.acquire();

      ArrayList<Block> blocks = new ArrayList<Block>();

      int startX = startPos.getX();
      int startY = startPos.getY();
      int startZ = startPos.getZ();

      int endX = endPos.getX();
      int endY = endPos.getY();
      int endZ = endPos.getZ();

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

      int yPos = 0;
      int xPos = 0;
      int zPos = 0;

      for (int y = sy; y <= ey; y++) {
        xPos = 0;

        for (int x = sx; x <= ex; x++) {
          zPos = 0;

          for (int z = sz; z <= ez; z++) {
            BlockPos pos = new BlockPos(x, y, z);

            BlockState state = world.getBlockState(pos);
            if (state == null) {
              continue;
            }

            String material = getIdentifierAtPosition(world, pos);

            Block block = new Block();
            // set to the local coordinate not the absolute coordinates as this may be
            // placed at a different
            // location

            if (local) {
              block.setX((int) xPos);
              block.setY((int) yPos);
              block.setZ((int) zPos);
            } else {
              block.setX(pos.getX());
              block.setY(pos.getY());
              block.setZ(pos.getZ());
            }

            block.setMaterial(material);

            var entries = state.getEntries();
            entries.forEach((k, v) -> {
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
            zPos++;
          }

          xPos++;
        }

        yPos++;
      }

      return blocks.toArray(new Block[blocks.size()]);

    } catch (InterruptedException ie) {
      LOGGER.error("Thread interuped whine trying to get position {}", ie.toString());
    } finally {
      readMutex.release();
    }

    return new Block[0];
  }

  public static BlockState GetBlockState(Vec3i pos, ServerWorld world) {
    try {
      readMutex.acquire();
      return world.getBlockState(new BlockPos(pos));

    } catch (InterruptedException ie) {
      LOGGER.error("Thread interuped whine trying to get position {}", ie.toString());
    } finally {
      readMutex.release();
    }

    return null;
  }

  public static boolean BreakBlock(Vec3i pos, ServerWorld world) {
    try {
      deleteMutex.acquire();

      boolean didBreak = world.breakBlock(new BlockPos(pos), false);
      return didBreak;

    } catch (InterruptedException ie) {
      LOGGER.error("Thread interuped whine trying to get position {}", ie.toString());
    } finally {
      deleteMutex.release();
    }

    return false;
  }

  // if startPos is specified the block is placed relative to start pos
  public static void SetBlocks(Vec3i startPos, Block[] blocks, int rotation, ServerWorld world) throws Exception {
    for (Block block : blocks) {

      BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());

      if (startPos != null) {
        int blockX = block.getX();
        int blockY = block.getY();
        int blockZ = block.getZ();

        if (rotation == 90) {
          int tmpX = blockX;
          blockX = blockZ * -1;
          blockZ = tmpX;
        }

        if (rotation == 180) {
          blockX = blockX * -1;
          blockZ = blockZ * -1;
        }

        if (rotation == 270) {
          int tmpX = blockX;
          blockX = blockZ;
          blockZ = tmpX * -1;
        }

        pos = new BlockPos(blockX + startPos.getX(), blockY + startPos.getY(), blockZ + startPos.getZ());
      }

      BlockState state = world.getBlockState(pos);
      String material = Util.getIdentifierAtPosition(world, pos);

      // if there is an existing block that is not air remove it
      if (state != null && !material.equals("minecraft:air")) {
        boolean didBreak = world.breakBlock(pos, false);

        if (!didBreak) {
          throw new Exception(String.format(
              "unable to create block at position, %s, can't break existing block at that location", pos.toString()));
        }
      }

      // do not place air
      if (block.getMaterial().equals("minecraft:air")) {
        continue;
      }

      // create the block
      SetBlock(pos, block, rotation, world);
    }
  }

  public static void SetBlock(BlockPos pos, Block block, int rotation, ServerWorld world) throws Exception {
    try {
      writeMutex.acquire();

      var item = Registries.BLOCK.get(new Identifier(block.getMaterial()));
      if (item == null) {
        throw new Exception(String.format("unable to create block, material %s does not exist", block.getMaterial()));
      }

      BlockState state = item.getDefaultState();
      try {
        switch (block.getFacing()) {
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
      } catch (Exception ex) {
        // LOGGER.error("Unable to set direction for block: {}", ex.getMessage());
      }

      switch (block.getHalf()) {
        case "top":
          state = state.with(Properties.BLOCK_HALF, BlockHalf.TOP);
          break;
        case "bottom":
          state = state.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM);
          break;
      }

      if (block.getRotation() > -1) {
        int rot = 0;
        switch (rotation) {
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

        if (rot >= 16) {
          rot = rot - 16;
        }

        state = state.with(Properties.ROTATION, rot);
      }

      boolean didSet = world.setBlockState(pos, state);

      if (!didSet) {
        // LOGGER.error("Unable to place block {} at
        // {},{},{}",block.getMaterial(),block.getX(),block.getY(),block.getZ());
        throw new Exception(String.format("unable to create block at position, %s", pos.toString()));
      }
    } catch (InterruptedException ie) {
      LOGGER.error("Thread interuped whine trying to get position {}", ie.toString());
    } finally {
      writeMutex.release();
    }
  }
}
