package com.hashicraft.minecraftapi.server.handlers.schema;

import com.hashicraft.minecraftapi.server.models.Block;
import com.hashicraft.minecraftapi.server.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiSecurity;
import jakarta.servlet.ServletInputStream;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SchemaPOST implements Handler {

	private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public SchemaPOST(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(
      path = "/v1/schema/{x}/{y}/{z}/{rotation}",            // only necessary to include when using static method references
      methods = HttpMethod.POST,    // only necessary to include when using static method references
      summary = "Create multiple blocks",
      description = "Creates the blocks at the given location, specifying a rotation of 90,180, or 270, will rotate the given blocks at the origin. Blocks should be provided as zip file containing a single entry. This endpoint returns an operation id that can be used to undo the block placement and restore the original blocks.",
      operationId = "createMultipleBlocks",
      tags = {"Schema"},
      requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = Block[].class)}),
      responses = {
          @OpenApiResponse(status = "200", description = "blocks placed successfully", content = {@OpenApiContent(from = String.class)})
      },
      pathParams = {
        @OpenApiParam(name = "x", example = "12", required = true),
        @OpenApiParam(name = "y", example = "13", required = true),
        @OpenApiParam(name = "z", example = "14", required = true),
        @OpenApiParam(name = "rotation", example = "90", required = true, description = "rotates the block position about the origin")
      },
      security = {
        @OpenApiSecurity(name = "ApiKeyAuth")
      }
  )
  public void handle(Context ctx) throws Exception {

    int x = Integer.parseInt(ctx.pathParam("x"));
    int y = Integer.parseInt(ctx.pathParam("y"));
    int z = Integer.parseInt(ctx.pathParam("z"));
    int rotation = Integer.parseInt(ctx.pathParam("rotation"));

    LOGGER.info("Blocks POST called x:{}, y:{}, z:{}, rotation:{}",x,y,z,rotation);

    // the body will be a zip file we need to decode
    ServletInputStream is = ctx.req().getInputStream();
    ZipInputStream zis = new ZipInputStream(is);
    ZipEntry ze = zis.getNextEntry();

    if (ze == null) {
      ctx.res().sendError(400, "Invalid Zip file");
      return;
    }

    byte[] data = zis.readAllBytes();
    ObjectMapper mapper = new ObjectMapper();
    Block[] blocks = mapper.readValue(data, Block[].class);

    zis.closeEntry();
    zis.close();

    // get the end pos for the blocks, we need to copy the existing blocks 
    // into an undo file so that this process can be reversed
    Vec3d startPos = new Vec3d(x, y, z);
    Vec3d endPos = Util.getEndPosFromBlocks(startPos, blocks, rotation);

    LOGGER.info("fetch blocks {}, {}", startPos, endPos);

    Block[] existingBlocks = Util.GetBlocks(startPos, endPos, false, world);
    byte[] existingData = mapper.writeValueAsBytes(existingBlocks);

    // create a uniqueish id for the operation
    // the user can use this to undo the write
    String createID = String.valueOf(new java.util.Date().getTime());

    // create the output directory for the undo files
    File outputDir = new File("./undo");
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }

    FileOutputStream myfile = new FileOutputStream(String.format("./undo/%s", createID));
    myfile.write(existingData);
    myfile.close();

    try {
      Util.SetBlocks(startPos, blocks, rotation, world);
    } catch (Exception ex) {
      ctx.res().sendError(500, String.format("unable to place blocks %s", ex.toString()));
      return;
    }

    // send the creation id back to the client so they can use it to undo
    ctx.result(createID);
  }
}
