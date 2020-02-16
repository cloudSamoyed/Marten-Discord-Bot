package io.samoyed.marten;

import java.util.List;

import io.samoyed.Marten;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactRoleHandle extends ListenerAdapter {

	private String guildId, textChannelId, textMessageId;
	String[][] roleAssign;
	int historyLimit, roleLimit;
	private Marten app;
	private Message message;
	
	public ReactRoleHandle() {
		this.historyLimit = 10;
		this.roleLimit = 0;
	}
	
	
	public void refresh() {
		
		System.out.println("REACTROLE REFRESH: " + guildId + "-"+ textChannelId);
		
		for(MessageReaction mr : message.getReactions()) {
			for(String[] r : roleAssign) {
				if(mr.getReactionEmote().isEmoji()) {
					if(r[0].equalsIgnoreCase(mr.getReactionEmote().toString().substring(3))) {
						
						
						//cleanReactRole()
						
						for(int i = 1; i < r.length; i++) {
							
							Role role = this.message.getGuild().getRoleById(r[i]);
							
							List<Member> membersWithRole = this.message.getGuild().getMembersWithRoles(role);
							List<User> validUsers = mr.retrieveUsers().complete();
							
							for(Member m : membersWithRole) {
								if(!validUsers.contains(m.getUser()))
									this.message.getGuild().removeRoleFromMember(m, role).complete();
							}
						}
						
						
						
						//-----
						
						for(User u : mr.retrieveUsers()) {
							if(mr.getGuild().isMember(u)) {
								assignRolesFromArray(r, mr.getGuild().getMember(u));
							} else {
								mr.removeReaction(u).queue();
							}
						}
						
					}
				} else if(mr.getReactionEmote().isEmote()) {
					if((r[0].replace(":", "(") + ")").equalsIgnoreCase(mr.getReactionEmote().toString().substring(3))) {
						
						
						//cleanReactRole()
						
						for(int i = 1; i < r.length; i++) {
							
							Role role = this.message.getGuild().getRoleById(r[i]);
							
							List<Member> membersWithRole = this.message.getGuild().getMembersWithRoles(role);
							List<User> validUsers = mr.retrieveUsers().complete();
							
							for(Member m : membersWithRole) {
								if(!validUsers.contains(m.getUser()))
									this.message.getGuild().removeRoleFromMember(m, role).complete();
							}
						}
						
						
						
						//-----
						for(User u : mr.retrieveUsers()) {
							if(mr.getGuild().isMember(u)) {
								assignRolesFromArray(r, mr.getGuild().getMember(u));
							} else {
								mr.removeReaction(u).queue();
							}
						}
					}
				}
				
				
			}
		}
		
		//---- TODO: Check everyone's roll and see if their reactions are on there properly
		
		
		
		
		
	}
	
	public void initializeHandle(Marten app) {
		this.app = app;
		this.message = this.app.getJDA().getGuildById(this.guildId).getTextChannelById(this.textChannelId).getHistoryFromBeginning(this.historyLimit).complete().getMessageById(this.textMessageId);
		for(String[] reactEmote : roleAssign) {
			this.message.addReaction(reactEmote[0]).queue();
		}
		
		this.refresh();
		
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if(event.getUser().equals(this.app.getJDA().getSelfUser()))
			return;
		
		
		if(event.getMessageId().equals(this.textMessageId) && event.getTextChannel().getId().equals(this.textChannelId) && event.getGuild().getId().equals(this.guildId)) {

			if(event.getReactionEmote().isEmoji()) {
				for(String[] r : roleAssign) {
					if(r[0].equalsIgnoreCase(event.getReactionEmote().toString().substring(3))) {
						assignRolesFromArray(r, event.getMember());
					}
				}
			} else {
				for(String[] r : roleAssign) {
					if((r[0].replace(":", "(") + ")").equalsIgnoreCase(event.getReactionEmote().toString().substring(3))) {
						assignRolesFromArray(r, event.getMember());
					}
				}
			}
		}
	}
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		
		if(event.getUser() == null)
			return;
		
		if(event.getUser().equals(this.app.getJDA().getSelfUser()))
			return;
		
		
		if(event.getMessageId().equals(this.textMessageId) && event.getTextChannel().getId().equals(this.textChannelId) && event.getGuild().getId().equals(this.guildId)) {
			if(event.getReactionEmote().isEmoji()) {
				for(String[] r : roleAssign) {
					if(r[0].equalsIgnoreCase(event.getReactionEmote().toString().substring(3))) {
						removeRolesFromArray(r, event.getMember());
					}
				}
			} else {
				for(String[] r : roleAssign) {
					if((r[0].replace(":", "(") + ")").equalsIgnoreCase(event.getReactionEmote().toString().substring(3))) {
						removeRolesFromArray(r, event.getMember());
					}
				}
			}
		}
	}
	
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		this.refresh();
	}
	
	public void redactUser(User user) {
		for(MessageReaction r : this.message.getReactions()) {
			List<User> users = r.retrieveUsers().complete();
			if(users.contains(user))
				r.removeReaction(user).queue();
		}
		
	}

	private void assignRolesFromArray(String[] assignRole, Member member) {
		for(int i = 1; i < assignRole.length; i++) {
			this.message.getGuild().addRoleToMember(member, this.message.getGuild().getRoleById(assignRole[i])).queue();
		}
	}
	
	private void removeRolesFromArray(String[] assignRole, Member member) {
		for(int i = 1; i < assignRole.length; i++) {
			this.message.getGuild().removeRoleFromMember(member, this.message.getGuild().getRoleById(assignRole[i])).queue();
		}
	}
}
