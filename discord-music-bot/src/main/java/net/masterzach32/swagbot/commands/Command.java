package net.masterzach32.swagbot.commands;

import java.util.ArrayList;
import java.util.List;

import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.utils.Constants;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class Command implements Comparable<Command> {
	
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
	
	public void execute(IMessage message, String[] params) {
		try {
			event.execute(message, params);
		} catch (RateLimitException | MissingPermissionsException | DiscordException e) {
			e.printStackTrace();
		}
	}
	
	public static void executeCommand(IMessage message, String identifier, String[] params) throws RateLimitException, DiscordException, MissingPermissionsException {
		Command c = null;
		
		for(Command command : commands)
			if(command.identifier.equals(identifier))
				c = command;
		
		if(c == null)
			return;
		
		else if(c.permLevel > 0) {
			boolean hasPerms = false;
			List<IRole> userRoles = message.getAuthor().getRolesForGuild(message.getChannel().getGuild());
			for(IRole role : userRoles)
				if(c.permLevel == 2 && message.getAuthor().getID().equals("97341976214511616") || c.permLevel == 1 && role.getName().equals("Bot Commander"))
					hasPerms = true;
			
			if(!hasPerms)
				new MessageBuilder(App.client).withContent("**You do not have permission to use this command.**").withChannel(message.getChannel()).build();
			else 
				c.execute(message, params);
		}
		else
			c.execute(message, params);
	}
	
	public static void listAllCommands(IUser user) {
		String str = "Commands for **SwagBot**:\n\n```";
		for(Command command : commands)
			str += "\t" + Constants.DEFAULT_COMMAND_PREFIX + command.identifier + /*"\t\t" + command.name + "\t\t" + command.info +*/ "\n";
		str += "```\n\n";
		str += "**Note**: Command prefixes may be different per guild!";
		str += "\n\n";
		str += "**Permissions**:\n0: Everyone can use these commands.\n1: Only users with a role named \"Bot Commander\" can use these commands\n2: Only contributers on GitHub can use these commands.";
		str += "\n\n";
		str += "To view more information for a command, use `" + Constants.DEFAULT_COMMAND_PREFIX + "help <command>`";
		str += "\n\n";
		str += "Check out the development for SwagBot at:\nhttps://github.com/Masterzach32/SwagBot";
		str += "\n\n";
		str += "Check out my creator's website:\nhttp://masterzach32.net";
		str += "\n\n";
		str += "Join my home guild:\nhttps://discord.gg/RFHKKvR";
		try {
			App.client.getOrCreatePMChannel(user).sendMessage(str);
		} catch (RateLimitException | MissingPermissionsException | DiscordException e) {
			e.printStackTrace();
		}
	}
	
	public int compareTo(Command c) {
		if(this.permLevel > c.permLevel)
			return 1;
		else if(this.permLevel < c.permLevel)
			return -1;
		return identifier.compareTo(c.identifier);
	}
}