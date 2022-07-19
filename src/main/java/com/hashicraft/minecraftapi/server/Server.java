package com.hashicraft.minecraftapi.server;

import com.hashicraft.minecraftapi.server.handlers.BlocksDELETE;
import com.hashicraft.minecraftapi.server.handlers.BlocksGET;
import com.hashicraft.minecraftapi.server.handlers.BlocksPOST;
import com.mojang.authlib.yggdrasil.response.ErrorResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;
import net.minecraft.server.MinecraftServer;

import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;

public class Server {
  private Javalin app;
	public final Logger LOGGER = LoggerFactory.getLogger("server");

  public Server() {
    app = Javalin.create(config -> {
      config.registerPlugin(getConfiguredOpenApiPlugin());
      config.defaultContentType = "application/json";
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

    // get the details of a block
    BlocksGET blocksGet = new BlocksGET(server.getOverworld());
    this.app.get("/blocks/{x}/{y}/{z}",  blocksGet);

    // create a new block
    BlocksPOST blocksPOST = new BlocksPOST(server.getOverworld());
    this.app.post("/blocks",blocksPOST);

    BlocksDELETE blocksDELETE = new BlocksDELETE(server.getOverworld());
    this.app.delete("/blocks/{x}/{y}/{z}",blocksDELETE);
  }

}
