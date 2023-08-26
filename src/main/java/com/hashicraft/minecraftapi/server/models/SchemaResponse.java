package com.hashicraft.minecraftapi.server.models;

import net.minecraft.util.math.Vec3i;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SchemaResponse {

  private int startX;
  private int startY;
  private int startZ;

  private int endX;
  private int endY;
  private int endZ;

  public SchemaResponse(Vec3i start, Vec3i end) {
    this.startX = (int) start.getX();
    this.startY = (int) start.getY();
    this.startZ = (int) start.getZ();

    this.endX = (int) end.getX();
    this.endY = (int) end.getY();
    this.endZ = (int) end.getZ();
  }

  public int getStartX() {
    return this.startX;
  }

  public void setStartX(int x) {
    this.startX = x;
  }

  public int getStartY() {
    return this.startY;
  }

  public void setStartY(int y) {
    this.startY = y;
  }

  public int getStartZ() {
    return this.startZ;
  }

  public void setStartZ(int z) {
    this.startZ = z;
  }

  public int getEndX() {
    return this.endX;
  }

  public void setEndX(int x) {
    this.endX = x;
  }

  public int getEndY() {
    return this.endY;
  }

  public void setEndY(int y) {
    this.endY = y;
  }

  public int getEndZ() {
    return this.endZ;
  }

  public void setEndZ(int z) {
    this.endZ = z;
  }
}
