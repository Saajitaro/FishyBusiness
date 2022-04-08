package com.saajitaro.silence;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedInEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

@Mod("silence")
public class Silence
{
	
	//private static final Logger LOGGER = LogManager.getLogger();
    File silence_config_path = new File(FMLPaths.GAMEDIR.get().toString() + "/config/silence/");
	File silence_config_file = new File(FMLPaths.GAMEDIR.get().toString() + "/config/silence/players.json");
	
	public Silence() 
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
	
	@SubscribeEvent
	public void serverLoad(FMLServerStartingEvent event) 
	{
		event.getCommandDispatcher().register(customCommand());
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onLogin(LoggedInEvent event) throws IOException
	{
		makeJSON();
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onChatSend(ClientChatEvent event) throws IOException
	{
		String message_sent = event.getOriginalMessage();
		String[] split_message = message_sent.split("\\s+");
		int num_of_words_in_msg = split_message.length;
		
		if ((message_sent.startsWith("/block add player") || message_sent.startsWith("/block add word")) && num_of_words_in_msg >= 4)
		{
			addWordOrPlayerToJSON(split_message[2] + "s", split_message[3]);
			
		}else if ((message_sent.startsWith("/block remove player") || message_sent.startsWith("/block remove word")) && num_of_words_in_msg >= 4)
		{
			removeWordOrPlayerFromJSON(split_message[2] + "s", split_message[3]);
			
		}else if (message_sent.startsWith("/block show_placeholders"))
		{
			changePlaceholderState();
			
		}else if (message_sent.startsWith("/block get_words"))
		{
			getBlockedWordsAsString();
			
		}else if (message_sent.startsWith("/block get_players"))
		{
			getMutedPlayersAsString();
			
		}else if (message_sent.startsWith("/block show_me_cmds"))
		{
			changeMeMessagesState();
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) throws IOException 
    {
		JsonArray muted_players = getMutedPlayers();
	    Collection<NetworkPlayerInfo> players = Minecraft.getInstance().getConnection().getPlayerInfoMap();
    	String formatted_message = event.getMessage().getFormattedText();
		if (formatted_message.startsWith("* "))
		{
			if (showMeMessages())
			{
				for (JsonElement muted_player : muted_players)
				{
					if (formatted_message.toString().startsWith("* " + muted_player.getAsString()))
					{
						if (showPlaceholder())
						{
							event.setMessage(new StringTextComponent("User Blocked: " + muted_player.getAsString()));
						
						}else 
						{
							event.setCanceled(true);
						}	    					
					}
				}
				
			}else 
			{
				if (showPlaceholder())
				{
					event.setMessage(new StringTextComponent("/me message blocked"));
					
				}else 
				{
					event.setCanceled(true);
				}
			}
		}

    	ITextComponent unformatted_message = event.getMessage();

		for (NetworkPlayerInfo player : players)
		{				
			for (JsonElement muted_player : muted_players)
			{
				String player_id = player.getGameProfile().getId().toString();
				String player_name = player.getGameProfile().getName();
				
				if (unformatted_message.toString().contains(player_id) && player_name.toLowerCase().equals(muted_player.getAsString().toLowerCase()))
				{
					if (showPlaceholder())
					{
						event.setMessage(new StringTextComponent("User Blocked: " + player_name));
					
					}else 
					{
						event.setCanceled(true);
					}	    					
				}
			}
		}
		
		if (!event.isCanceled() && event.getMessage().getFormattedText().equals(formatted_message))
		{
		    for (JsonElement word : getBlockedWords())
			{
				if (event.getMessage().getFormattedText().toLowerCase().contains(word.getAsString().toLowerCase()))
				{
					if (showPlaceholder())
					{
						event.setMessage(new StringTextComponent("Word blocked: " + word.getAsString()));
						
					}else 
					{
						event.setCanceled(true);
					}
				}
			}
		}	    
    }
	
	private LiteralArgumentBuilder<CommandSource> customCommand() 
	{
		return LiteralArgumentBuilder.<CommandSource>literal("block")
			.then(Commands.literal("add")
				.then(Commands.literal("player")
						.then(Commands.argument("player username", StringArgumentType.word()).executes(this::dummy)))
				.then(Commands.literal("word")
						.then(Commands.argument("word", StringArgumentType.word()).executes(this::dummy))))
			
			.then(Commands.literal("remove")
					.then(Commands.literal("player")
							.then(Commands.argument("player username", StringArgumentType.word()).executes(this::dummy)))
					.then(Commands.literal("word")
							.then(Commands.argument("word", StringArgumentType.word()).executes(this::dummy))))
			.then(Commands.literal("show_placeholders").executes(this::dummy))
			.then(Commands.literal("get_words").executes(this::dummy))
			.then(Commands.literal("get_players").executes(this::dummy))
			.then(Commands.literal("show_me_cmds").executes(this::dummy));
	}
	
	private int dummy(CommandContext<CommandSource> ctx) 
	{
		return 0;
	}
	
	public JsonObject getJSON() throws IOException
	{
		List<String> players_json_list = Files.readAllLines(silence_config_file.toPath());
		String players_json_text = "";
		
		for (String line : players_json_list)
		{
			players_json_text += line + "\n";
		}

		JsonParser parser = new JsonParser();
		JsonObject players_json = parser.parse(players_json_text).getAsJsonObject();
		return players_json;
	}
	
	public void makeJSON() throws IOException 
	{
		
		if (!silence_config_file.exists())
		{
			silence_config_path.mkdirs();
			silence_config_file.createNewFile();
			FileWriter writer = new FileWriter(silence_config_file);
			writer.write("{\"players\":[],\"words\":[],\"show_placeholder\":true,\"show_me_messages\":true}");
			writer.close();
		}
		
	}
	
	public void writeToJSON(JsonObject players_json)
	{
		if (silence_config_file.exists())
		{
			silence_config_path.mkdirs();
			
			try 
			{
				silence_config_file.createNewFile();
				FileWriter writer = new FileWriter(silence_config_file);
				writer.write(players_json.toString());
				writer.close();
				
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public void addWordOrPlayerToJSON(String arg1, String arg2) throws IOException
	{
		JsonObject players_json = getJSON();
		players_json.get(arg1).getAsJsonArray().add(arg2);
		writeToJSON(players_json);
		sendPlayerAMessage("\u00A7d\u00A7lAdded: \u00A7r" + arg2);
	}

	public void removeWordOrPlayerFromJSON(String arg1, String arg2) throws IOException 
	{
		JsonObject players_json = getJSON();

		for (int j = 0; j < players_json.get(arg1).getAsJsonArray().size(); j++)
		{						
			if (players_json.get(arg1).getAsJsonArray().get(j).getAsString().equals(arg2))
			{
				players_json.get(arg1).getAsJsonArray().remove(j);
			}
		}
		
		writeToJSON(players_json);
		sendPlayerAMessage("\u00A7d\u00A7lRemoved: \u00A7r" + arg2);
	}
	
	public void sendPlayerAMessage(String message)
	{
		Minecraft.getInstance().player.getEntity().sendMessage(new StringTextComponent(message.trim()));
	}
	
	public void getMutedPlayersAsString() throws IOException
	{
		JsonArray players_array = getMutedPlayers();
		String arrayAsString = "\u00A7b\u00A7lBlocked Players: \u00A7r";
		
		for(int i=0; i<players_array.size(); i++) 
		{
		    String player = players_array.get(i).getAsString();
		    arrayAsString += player + " ";
		}
		
		sendPlayerAMessage(arrayAsString);
	}
	
	public JsonArray getMutedPlayers() throws IOException
	{
		return getJSON().get("players").getAsJsonArray();
	}
	
	public void getBlockedWordsAsString() throws IOException
	{
		JsonArray words_array = getBlockedWords();
		String jsonObjectAsString = "\u00A7b\u00A7lBlocked Words: \u00A7r";
		
		for(int i=0; i<words_array.size(); i++) 
		{
		    JsonElement jsonObject = words_array.get(i);
		    jsonObjectAsString += jsonObject.getAsString() + " ";
		}
		
		sendPlayerAMessage(jsonObjectAsString);	
	}

	public JsonArray getBlockedWords() throws IOException
	{
		return getJSON().get("words").getAsJsonArray();
	}
	
	public void changePlaceholderState() throws IOException
	{
		JsonObject players_json = getJSON();
		players_json.addProperty("show_placeholder", !players_json.get("show_placeholder").getAsBoolean());
		writeToJSON(players_json);
		sendPlayerAMessage("\u00A7b\u00A7lShow placeholders: \u00A7r" + players_json.get("show_placeholder").getAsString());
	}

	public boolean showPlaceholder() throws IOException {
		return getJSON().get("show_placeholder").getAsBoolean();
	}

	public void changeMeMessagesState() throws IOException
	{
		JsonObject players_json = getJSON();
		players_json.addProperty("show_me_messages", !players_json.get("show_me_messages").getAsBoolean());
		writeToJSON(players_json);
		sendPlayerAMessage("\u00A7b\u00A7lShow /me messages: \u00A7r" + players_json.get("show_me_messages").getAsString());
	}

	public boolean showMeMessages() throws IOException {
		return getJSON().get("show_me_messages").getAsBoolean();
	}

}
