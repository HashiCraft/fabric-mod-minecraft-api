package com.hashicraft.minecraftapi.server.models;

public class Block {

  private int x;
  private int y;
  private int z;
  private String type;

  public int getX() {
    return this.x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return this.y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getZ() {
    return this.z;
  }

  public void setZ(int z) {
    this.z = z;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type= type;
  }

  public Block() {
  }
}
