package com.example.examplemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class onPlayerJoin {
	private static final Logger LOGGER = LogManager.getLogger();
	@SubscribeEvent
    public void onPlayerJoins(final EntityJoinWorldEvent event)
    {
    	LOGGER.info("player join");
    }
}