package com.hashicraft.minecraftapi.server.handlers.schema;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashicraft.minecraftapi.server.models.Block;
import com.hashicraft.minecraftapi.server.util.Util;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiSecurity;
import jakarta.servlet.ServletOutputStream;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3i;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SchemaGET implements Handler {

  private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public SchemaGET(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(path = "/v1/schema/{start x}/{start y}/{start z}/{end x}/{end y}/{end z}", // only necessary to include when
                                                                                      // using static method references
      methods = HttpMethod.GET, // only necessary to include when using static method references
      summary = "Create a schema at the given location", description = "Returns an array of blocks between the start and end coordinates in the path, data is returned as a zip file containing a single entry schema.json.", operationId = "getSchema", tags = {
          "Schema" }, responses = {
              @OpenApiResponse(status = "200", content = {
                  @OpenApiContent(from = Block[].class) }, description = "Data is returned as a zipped file containing a single entry")
          }, security = {
              @OpenApiSecurity(name = "ApiKeyAuth")
          }, pathParams = {
              @OpenApiParam(name = "start x", example = "12", required = true),
              @OpenApiParam(name = "start y", example = "13", required = true),
              @OpenApiParam(name = "start z", example = "14", required = true),
              @OpenApiParam(name = "end x", example = "22", required = true),
              @OpenApiParam(name = "end y", example = "15", required = true),
              @OpenApiParam(name = "end z", example = "19", required = true)
          })
  public void handle(Context ctx) throws Exception {
    int startX = Integer.parseInt(ctx.pathParam("start_x"));
    int startY = Integer.parseInt(ctx.pathParam("start_y"));
    int startZ = Integer.parseInt(ctx.pathParam("start_z"));

    int endX = Integer.parseInt(ctx.pathParam("end_x"));
    int endY = Integer.parseInt(ctx.pathParam("end_y"));
    int endZ = Integer.parseInt(ctx.pathParam("end_z"));

    Vec3i startPos = new Vec3i(startX, startY, startZ);
    Vec3i endPos = new Vec3i(endX, endY, endZ);

    LOGGER.info("Blocks GET called start_x:{}, start_y:{}, start_z:{} end_x:{}, end_y:{}, end_z:{}", startX, startY,
        startZ, endX, endY, endZ);

    Block[] blocks = Util.GetBlocks(startPos, endPos, true, world);

    // zip the json
    ObjectMapper mapper = new ObjectMapper();
    byte[] data = mapper.writeValueAsBytes(blocks);

    ctx.res().setContentType("application/zip");
    zipToStream(ctx.outputStream(), "schema.json", data);
  }

  private void zipToStream(ServletOutputStream stream, String file, byte[] data) throws IOException {
    ZipOutputStream zos = new ZipOutputStream(stream);
    try {
      zos.putNextEntry(new ZipEntry(file));
      zos.write(data);
      zos.closeEntry();
    } finally {
      zos.close();
    }
  }

}
