package io.samoyed.marten;

import io.samoyed.Marten;
import io.samoyed.marten.api.CommandSpec;
import io.samoyed.marten.util.StringUtil;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandHandle extends ListenerAdapter {
	
	private Marten app;
	
	public CommandHandle(Marten app) {
		this.app = app;
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().equals(this.app.getJDA().getSelfUser()))
			return;
		if(!event.getChannelType().equals(ChannelType.TEXT))
			return;

		if(event.getMessage().getContentDisplay().startsWith(this.app.getCommandPrefix())) {
			String cmd = event.getMessage().getContentRaw().split(" ")[0].substring(1);
			String[] args = new String[] {};
			
			try {
				args = StringUtil.cropStringArray(event.getMessage().getContentRaw().split(" "), 1, 0);
			} catch (NullPointerException e) {}
			
			for(CommandSpec commandSpec : this.app.getCommands()) {
				
				for(String alias : commandSpec.getAliases()) {
					if(!alias.equalsIgnoreCase(cmd))
						continue;
					commandSpec.onCommand(event, args);
					event.getMessage().delete().queue();
					break;
				}
			}
		}
		
	}
}
