package com.hashicraft.minecraftapi.server;

import com.hashicraft.minecraftapi.server.handlers.block.BlockDELETE;
import com.hashicraft.minecraftapi.server.handlers.block.BlockGET;
import com.hashicraft.minecraftapi.server.handlers.block.BlockPOST;
import com.hashicraft.minecraftapi.server.handlers.blocks.BlocksGET;
import com.hashicraft.minecraftapi.server.handlers.blocks.BlocksPOST;
import com.hashicraft.minecraftapi.server.handlers.blocks.BlocksDELETE;
import com.hashicraft.minecraftapi.server.handlers.health.HealthGET;
import com.mojang.authlib.yggdrasil.response.ErrorResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;
import net.minecraft.server.MinecraftServer;

public class Server {
  private Javalin app;
	public final Logger LOGGER = LoggerFactory.getLogger("server");

  public Server() {
    app = Javalin.create(config -> {
      config.registerPlugin(getConfiguredOpenApiPlugin());
      config.defaultContentType = "application/json";
      config.maxRequestSize = 10000000l;
    });
  }

  private static OpenApiPlugin getConfiguredOpenApiPlugin() {
    Info info = new Info().version("1.0").description("User API");
    OpenApiOptions options = new OpenApiOptions(info)
      .activateAnnotationScanningFor("io.javalin.example.java")
      .path("/swagger-docs") // endpoint for OpenAPI json
      .swagger(new SwaggerOptions("/swagger-ui")) // endpoint for swagger-ui
      .reDoc(new ReDocOptions("/redoc")) // endpoint for redoc
      .defaultDocumentation(doc -> {
        doc.json("500", ErrorResponse.class);
        doc.json("503", ErrorResponse.class);
      });
    return new OpenApiPlugin(options);
  }

  public void start(MinecraftServer server) {
    this.app.start(8080);
    LOGGER.info("Starting server");

    this.app.get("/block/{x}/{y}/{z}", new BlockGET(server.getOverworld()));
    this.app.post("/block", new BlockPOST(server.getOverworld()));
    this.app.delete("/block/{x}/{y}/{z}", new BlockDELETE(server.getOverworld()));

    this.app.get("/blocks/{start_x}/{start_y}/{start_z}/{end_x}/{end_y}/{end_z}", new BlocksGET(server.getOverworld()));
    this.app.post("/blocks/{x}/{y}/{z}", new BlocksPOST(server.getOverworld()));
    this.app.delete("/blocks/{start_x}/{start_y}/{start_z}/{end_x}/{end_y}/{end_z}", new BlocksDELETE(server.getOverworld()));

    this.app.get("/health", new HealthGET(server.getOverworld()));
  }

}
