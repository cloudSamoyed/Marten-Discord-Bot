package io.samoyed.marten;

import io.samoyed.Marten;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AccessRoleHandle extends ListenerAdapter {
	
	private String guildId, roleId;
	private Marten app;

	public void initializeHandle(Marten app) {
		this.app = app;
		this.refresh();
	}
	
	
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		
		if(event.getUser().equals(this.app.getJDA().getSelfUser()))
			return;
		
		if(event.getGuild().getId().equals(this.guildId) && event.getRoles().contains(event.getGuild().getRoleById(this.roleId))) {
			for(ReactRoleHandle rrh : this.app.getReactRoleHandles()) rrh.redactUser(event.getUser());
			for(Role r : event.getMember().getRoles()) {
				try {
					event.getGuild().removeRoleFromMember(event.getMember(), r).complete();
				} catch (Exception e) {}
			}
			
			try {
				this.app.getMinecraftHandle().refresh();
			} catch (Exception e) {}
		}
	}
	

	
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		this.refresh();
	}
	
	
	
	public void refresh() {
		
		System.out.println("ACCESSROLE REFRESH: " + guildId + "-"+ roleId);
		
		Guild g = this.app.getJDA().getGuildById(this.guildId);
		for(Member m : g.getMembers()) {
			if(!m.getRoles().contains(g.getRoleById(this.roleId))) {
				for(ReactRoleHandle rrh : this.app.getReactRoleHandles()) rrh.redactUser(m.getUser());
				for(Role r : m.getRoles()) {
					try {
						g.removeRoleFromMember(m, r).complete();
					} catch (Exception e) {}
				}
			}
		}
	}
}
