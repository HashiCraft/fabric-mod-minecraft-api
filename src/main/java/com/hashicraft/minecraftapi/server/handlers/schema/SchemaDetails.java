package com.hashicraft.minecraftapi.server.handlers.schema;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashicraft.minecraftapi.server.models.Block;
import com.hashicraft.minecraftapi.server.models.SchemaResponse;
import com.hashicraft.minecraftapi.server.util.Util;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3i;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SchemaDetails implements Handler {

  private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public SchemaDetails(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(path = "/v1/schema/details/{id}", methods = HttpMethod.GET, summary = "Get the details of a schema", description = "This method allows you to fetch the details of an applied schema single block for the given id. If no schema exists at the coordinates, a 404 is returned", operationId = "getSchema", tags = {
      "Schema" }, security = {
          @OpenApiSecurity(name = "ApiKeyAuth")
      }, responses = {
          @OpenApiResponse(status = "200", content = { @OpenApiContent(from = SchemaResponse.class) }),
          @OpenApiResponse(status = "404")
      }, pathParams = {
          @OpenApiParam(name = "id", example = "3425219898123", required = true, description = "Schema apply id returned from a POST to /v1/schema"),
      })
  public void handle(Context ctx) throws Exception {
    String id = ctx.pathParam("id");

    LOGGER.info("Schema Details called id:{}", id);

    Path path = Paths.get(String.format("./undo/%s", id));
    if (!path.toFile().exists()) {
      LOGGER.info("No undo operation exist for ID: {}", id);

      ctx.res().sendError(404, String.format("No undo operation exists for ID: %s", id));
    }

    // read the undo file
    ObjectMapper mapper = new ObjectMapper();
    Block[] blocks = mapper.readValue(path.toFile(), Block[].class);

    Vec3i startPos = Util.getStartPosFromBlocks(blocks);
    Vec3i endPos = Util.getEndPosFromBlocks(blocks);

    SchemaResponse resp = new SchemaResponse(startPos, endPos);

    ctx.json(resp);
  }

}
