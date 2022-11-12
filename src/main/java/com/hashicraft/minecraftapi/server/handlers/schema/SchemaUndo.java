package com.hashicraft.minecraftapi.server.handlers.schema;

import com.hashicraft.minecraftapi.server.models.Block;
import com.hashicraft.minecraftapi.server.util.Util;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiSecurity;
import net.minecraft.server.world.ServerWorld;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SchemaUndo implements Handler {

	private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public SchemaUndo(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(
      path = "/schema/undo/{operation id}",
      methods = HttpMethod.DELETE,
      summary = "Undo a create operation",
      description = "Restores the state of the blocks to before the POST operation was called. The operation id can only be used once.",
      operationId = "createMultipleBlocks",
      tags = {"Blocks"},
      responses = {
          @OpenApiResponse(status = "200")
      },
      pathParams = {
        @OpenApiParam(name = "operation id", example = "1234411231535", required = true, description = "Operation id returned from POST /v1/block"),
      },
      security = {
        @OpenApiSecurity(name = "ApiKeyAuth")
      }
  )
  public void handle(Context ctx) throws Exception {

    String id = ctx.pathParam("id");
    LOGGER.info("Undo blocks using id {}", id);

    Path path = Paths.get(String.format("./undo/%s", id));

    // read the undo file
    ObjectMapper mapper = new ObjectMapper();
    Block[] blocks = mapper.readValue(path.toFile(), Block[].class);
  
    try {
      Util.SetBlocks(null, blocks, 0, world);
    } catch (Exception ex) {
      ctx.res().sendError(500, String.format("unable to place blocks %s", ex.toString()));
      return;
    }

    // delete the undo file
    path.toFile().delete();
  }
}
