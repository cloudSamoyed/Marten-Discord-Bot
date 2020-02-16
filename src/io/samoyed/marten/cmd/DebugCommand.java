package io.samoyed.marten.cmd;

import io.samoyed.Marten;
import io.samoyed.marten.api.CommandSpec;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DebugCommand extends CommandSpec {

	public DebugCommand(String[] aliases, Marten app) {
		super(aliases, app);
	}

	@Override
	public void onCommand(MessageReceivedEvent event, String[] args) {
		
		System.out.println("Debug command ran");
		
		
	}

}
