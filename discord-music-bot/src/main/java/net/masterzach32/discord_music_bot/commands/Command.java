package net.masterzach32.discord_music_bot.commands;

import java.util.ArrayList;
import java.util.List;

import net.masterzach32.discord_music_bot.App;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class Command {
	
	private String name, identifier, info;
	private CommandEvent event;
	private int permLevel;
	
	public static final List<Command> commands = new ArrayList<Command>();

	public Command(String name, String identifier, String info, int permLevel, CommandEvent event) {
		this.name = name;
		this.identifier = identifier;
		this.info = info;
		this.permLevel = permLevel;
		this.event = event;
		commands.add(this);
	}
	
	public String getName() {
		return name;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getInfo() {
		return info;
	}
	
	public int getPermissionLevel() {
		return permLevel;
	}
	
	public String execute(IMessage message, String[] params) {
		try {
			return event.execute(message, params);
		} catch (RateLimitException | MissingPermissionsException | DiscordException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String executeCommand(IMessage message, String identifier, String[] params) {
		Command c = null;
		
		for(Command command : commands)
			if(command.identifier.equals(identifier))
				c = command;
		
		if(c == null)
			return "No command found for `" + App.guilds.getGuild(message.getGuild()).getCommandPrefix() + identifier + "`";
		
		if(c.permLevel > 0) {
			boolean hasPerms = false;
			List<IRole> userRoles = message.getAuthor().getRolesForGuild(message.getChannel().getGuild());
			for(IRole role : userRoles)
				if(c.permLevel == 2 && message.getAuthor().getID().equals("97341976214511616") || c.permLevel == 1 && role.getName().equals("Bot Commander"))
					hasPerms = true;
			
			if(!hasPerms)
				return "**You do not have permission to use this command.**";
			return c.execute(message, params);
		}
		return c.execute(message, params);
	}
	
	public static String listAllCommands(IGuild guild) {
		String str = "```";
		for(Command command : commands)
			str += "" + App.guilds.getGuild(guild).getCommandPrefix() + command.identifier /*+ "\t\t" + command.name + "\t\t" + command.info*/ + "\n";
		return str + "```";
	}
}