package com.hashicraft.minecraftapi.server.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Block {

  private String id;
  private int x;
  private int y;
  private int z;
  private String material;
  private String facing;
  private String half;
  private int rotation = -1;

  public String getID() {
    return this.id;
  }
  
  public void setID(String id) {
    this.id = id;
  }

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

  public String getMaterial() {
    return this.material;
  }

  public void setMaterial(String material) {
    this.material = material;
  }
  

  public String getFacing() {
    if (this.facing == null) {
      return "";
    }
    return this.facing;
  }

  public void setFacing(String facing) {
    this.facing = facing;
  }

  public String getHalf() {
    if (this.half == null) {
      return "";
    }
    return this.half;
  }

  public void setHalf(String half) {
    this.half = half;
  }

  public Block() {
  }
  
  public int getRotation() {
    return this.rotation;
  }

  public void setRotation(int rotation) {
    this.rotation = rotation;
  }
}
