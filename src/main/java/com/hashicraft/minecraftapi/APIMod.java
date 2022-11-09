package com.hashicraft.minecraftapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashicraft.minecraftapi.server.Server;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec3d;
import static net.minecraft.server.command.CommandManager.*;

public class APIMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("modid");
  public static final Server apiServer = new Server();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

    // Register the postion command
    CommandRegistrationCallback.EVENT.register((dispatcher, environment) -> 
        dispatcher.register(
        literal("position").executes(context -> {
          Vec3d pos = context.getSource().getPosition();
          context.getSource().sendFeedback(  
            new LiteralText(  
              String.format("your position is: %d, %d, %d", Math.round(pos.getX()), Math.round(pos.getY()), Math.round(pos.getZ())))
            , false);
          return 1;
      }))
    );

	  ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) ->   {
      // start the server
      apiServer.start(server);
		});
	}
}
