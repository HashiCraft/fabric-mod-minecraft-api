package com.hashicraft.minecraftapi.server.handlers.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import net.minecraft.server.world.ServerWorld;

public class HealthGET implements Handler {

	private final Logger LOGGER = LoggerFactory.getLogger("server");
  private final ServerWorld world;

  public HealthGET(ServerWorld world) {
    this.world = world;
  }

  @OpenApi(
      path = "/health",            // only necessary to include when using static method references
      method = HttpMethod.GET,    // only necessary to include when using static method references
      summary = "Check health of server",
      operationId = "checkHealth",
      tags = {"Health"},
      responses = {
          @OpenApiResponse(status = "200")
      }
  )
  public void handle(Context ctx) throws Exception {
    LOGGER.info("Health GET called");

  }
}
