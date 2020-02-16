package io.samoyed.marten.api;

import io.samoyed.Marten;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class CommandSpec implements CommandInterface {

	private String[] aliases;
	protected Marten app;
	
	public CommandSpec(String[] aliases, Marten app) {
		this.aliases = aliases;
		this.app = app;
	}
	
	public String[] getAliases() {
		return aliases;
	}
}

interface CommandInterface {
	
	public void onCommand(MessageReceivedEvent event, String[] args);
}
