package com.hashicraft.minecraftapi.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashicraft.minecraftapi.server.handlers.block.BlockDELETE;
import com.hashicraft.minecraftapi.server.handlers.block.BlockGET;
import com.hashicraft.minecraftapi.server.handlers.block.BlockPOST;
import com.hashicraft.minecraftapi.server.handlers.blocks.BlocksDELETE;
import com.hashicraft.minecraftapi.server.handlers.blocks.BlocksGET;
import com.hashicraft.minecraftapi.server.handlers.blocks.BlocksPOST;
import com.hashicraft.minecraftapi.server.handlers.blocks.BlocksUndo;
import com.hashicraft.minecraftapi.server.handlers.health.HealthGET;

import io.javalin.Javalin;
import io.javalin.openapi.ApiKeyAuth;
import io.javalin.openapi.OpenApiInfo;
import io.javalin.openapi.plugin.OpenApiConfiguration;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.SecurityConfiguration;
import io.javalin.openapi.plugin.redoc.ReDocConfiguration;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import net.minecraft.server.MinecraftServer;

public class Server {
  private Javalin app;
  public final Logger LOGGER = LoggerFactory.getLogger("server");

  private String AUTH_HEADER = "X-API-Key";
  private String apiKey = "supertopsecret";

  public Server() {

    String apiKeyEnv = System.getenv("API_KEY");
    if (apiKeyEnv != null && !apiKeyEnv.isEmpty()) {
      apiKey = apiKeyEnv;
    }

    app = Javalin.create(config -> {
      config.plugins.register(getConfiguredOpenApiPlugin());
      config.plugins.register(getConfiguredReDocPlugin());
      config.http.defaultContentType = "application/json";
      config.http.maxRequestSize = 10000000l;
    });

    app.before(ctx -> {
      LOGGER.info("Checking auth for {}", ctx.req().getPathInfo());

      // skip auth check for health and redoc paths
      if (
        ctx.req().getPathInfo().startsWith("/v1") &&
        !ctx.req().getPathInfo().contentEquals("/v1/health")
      ) { 
        String authHeader = ctx.req().getHeader(AUTH_HEADER);
        if (
          authHeader == null || !authHeader.contentEquals(this.apiKey)) {
          LOGGER.info("not authorized apikey: {}", authHeader);
          ctx.res().sendError(401);
        }
      }
    });
  }

  private static OpenApiPlugin getConfiguredOpenApiPlugin() {
    OpenApiInfo info = new OpenApiInfo();
    info.setTitle("Minecraft Block API");
    info.setVersion("1.0");
    info.setDescription("RESTFul API that allows the manipulation of blocks in Minecraft");

    OpenApiConfiguration options = new OpenApiConfiguration();
    ApiKeyAuth auth = new ApiKeyAuth();
    
    options.setInfo(info);
    options.setSecurity(new SecurityConfiguration().withSecurityScheme("ApiKeyAuth", auth));

    return new OpenApiPlugin(options);
  }

  private static ReDocPlugin getConfiguredReDocPlugin() {
    ReDocConfiguration reDocConfiguration = new ReDocConfiguration();
    reDocConfiguration.setUiPath("/redoc"); // by default it's /redoc

    return new ReDocPlugin(reDocConfiguration);
  }

  public void start(MinecraftServer server) {
    this.app.start(9090);
    LOGGER.info("Starting server");

    this.app.get("/v1/block/{x}/{y}/{z}", new BlockGET(server.getOverworld()));
    this.app.post("/v1/block", new BlockPOST(server.getOverworld()));
    this.app.delete("/v1/block/{x}/{y}/{z}", new BlockDELETE(server.getOverworld()));

    this.app.get("/v1/blocks/{start_x}/{start_y}/{start_z}/{end_x}/{end_y}/{end_z}", new BlocksGET(server.getOverworld()));
    this.app.post("/v1/blocks/{x}/{y}/{z}/{rotation}", new BlocksPOST(server.getOverworld()));
    this.app.delete("/v1/blocks/{start_x}/{start_y}/{start_z}/{end_x}/{end_y}/{end_z}",
        new BlocksDELETE(server.getOverworld()));
    this.app.put("/v1/blocks/undo/{id}", new BlocksUndo(server.getOverworld()));

    this.app.get("/v1/health", new HealthGET(server.getOverworld()));
  }

}
