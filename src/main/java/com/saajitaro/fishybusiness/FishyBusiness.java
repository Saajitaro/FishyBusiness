package com.saajitaro.fishybusiness;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.time.LocalDateTime;

import org.apache.commons.io.*;

@Mod("fishybusiness")
public class FishyBusiness 
{
	File perfect_config_folder_dir = new File(FMLPaths.GAMEDIR.get().toString() + "/config/fishybusiness/perfect config/");
	File perfect_config_file = new File(FMLPaths.GAMEDIR.get().toString() + "/config/fishybusiness/perfect config/mineminenomi-common.toml");
	String current_config_path_str = FMLPaths.GAMEDIR.get().toString() + "/config/mineminenomi-common.toml";
	File log_file = new File(FMLPaths.GAMEDIR.get().toString() + "/logs/fishybusiness.log");
	MinecraftServer source = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
	Logger MCLOGGER = LogManager.getLogger("mc-logger");

    public FishyBusiness() 
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
	
    public void checkConfig() throws IOException
    {
    	try
    	{    	
	    	if (perfect_config_file.exists())
	    	{
	    		MCLOGGER.info("Perfect Mine Mine no Mi config file found.");
	    		if (perfect_config_file.length() != 0)
	    		{
	    			MCLOGGER.info("Perfect config file not empty (good)... Make sure it actually contains valid config content.");
			    	if (!FileUtils.contentEquals(perfect_config_file, new File(current_config_path_str)))
			    	{
			    		
		    			FileUtils.copyFile(perfect_config_file, new File(current_config_path_str));
		    			log_file.createNewFile();
		    			String player_names = "";
		    			for (String name : ServerLifecycleHooks.getCurrentServer().getOnlinePlayerNames())
		    			{
		    				player_names += name + " ";
		    			}
		    			FileUtils.write(log_file, "[" + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()) + "]: Config was corrected. Online: " + player_names + "\n", (String) null, true);
		    			
		    			MCLOGGER.error("Something's not right with the current Mine Mine no Mi config. It has been corrected and logged in ./logs/fishybusiness.log");
			    		
			    		source.getCommandManager().handleCommand(source.getCommandSource(), "stop");
			    	}
			    	else
			    	{
			    		MCLOGGER.info("Mine Mine no Mi config file seems to be correct.");
			    	}
	    		}
	    		else
	    		{
	    			MCLOGGER.warn("Your perfect Mine Mine no Mi config file seems to be empty... Put the whole Mine Mine no Mi config in it with the values you want.");
	    		}
	    	}
	    	else
	    	{
	    		perfect_config_folder_dir.mkdirs();
	    		MCLOGGER.warn("You haven't setup the perfect Mine Mine no Mi config file. Put the perfect config file in ./config/fishybusiness/perfect config/");
	    	}
    	}
    	catch(Exception e)
    	{    		
    		MCLOGGER.error("Exception: " + e.getMessage());
    		throw e;
    	}    	
    	
    }
    
    @SubscribeEvent
    public void onPlayerLeaves(PlayerLoggedOutEvent event) throws IOException
    {
    	checkConfig();
    }
    
	@SubscribeEvent
    public void onPlayerJoins(PlayerLoggedInEvent event) throws IOException
    {
		checkConfig();
    }
}
