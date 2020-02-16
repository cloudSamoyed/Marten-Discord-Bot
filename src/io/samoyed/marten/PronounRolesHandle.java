package io.samoyed.marten;

import java.util.ArrayList;

import io.samoyed.Marten;
import io.samoyed.marten.util.StringUtil;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.RoleAction;

public class PronounRolesHandle extends ListenerAdapter {

	private Marten app;
	private String[][] pronouns;
	private String textChannelId, guildId;
	
	public void initializeHandle(Marten app) {
		this.app = app;
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().equals(this.app.getJDA().getSelfUser()))
			return;
		if(!event.getChannelType().equals(ChannelType.TEXT))
			return;
		
		if(event.getGuild().getId().equals(this.guildId) && event.getTextChannel().getId().equals(this.textChannelId)) {
			String[] strArray = event.getMessage().getContentRaw().split(" ");
			event.getMessage().delete().queue();
			
			ArrayList<String[]> selectedPronouns = new ArrayList<String[]>();
			
			for(String str : strArray) {
				for(String s : str.split("/")) {
					for(String[] pronouns : this.pronouns) {
						for(String p : pronouns) {
							if(p.equalsIgnoreCase(s)) {
								if(selectedPronouns.contains(pronouns))
									continue;
								selectedPronouns.add(pronouns);
							}
						}
					}
					
				}
			}
			
			for(Role r : event.getMember().getRoles()) {
				for(String[] p : this.pronouns) {
					if(StringUtil.decoratePronouns(p, 2, false).equalsIgnoreCase(r.getName())) {
						if(!selectedPronouns.contains(p))
							event.getGuild().removeRoleFromMember(event.getMember(), r).complete();
					}
				}
			}
			
			
			for(String[] pronouns : selectedPronouns) {
				boolean exists = false;
				
				for(Role r: event.getGuild().getRoles()) {
					
					if(r.getName().equals(StringUtil.decoratePronouns(pronouns, 2, false))) {
						event.getGuild().addRoleToMember(event.getMember(), r).complete();
						exists = true;
						break;
					}
				}
				
				if(exists)
					continue;
				
				RoleAction r = event.getGuild().createRole();
				
				r.setName(StringUtil.decoratePronouns(pronouns, 2, false));
				r.setPermissions(event.getGuild().getPublicRole().getPermissions());
				Role role = r.complete();
				
				event.getGuild().addRoleToMember(event.getMember(), role).complete();
				
			}
			
		}
	}

		
}
