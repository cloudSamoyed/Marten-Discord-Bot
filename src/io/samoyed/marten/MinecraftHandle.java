package io.samoyed.marten;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import io.samoyed.Marten;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MinecraftHandle extends ListenerAdapter {

	private String textChannelId, guildId, outputFile, accessRoleId;
	private Marten app;
	private static String fetchUrl = "https://api.mojang.com/users/profiles/minecraft/";
	
	
	public void initializeHandle(Marten app) throws JsonSyntaxException, IOException {
		this.app = app;
		this.refresh();
	}
	
	public void refresh( ) throws JsonSyntaxException, IOException {
		Gson g = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		String[][] in = g.fromJson(new String(Files.readAllBytes(Paths.get(this.outputFile)), Charset.forName("UTF-8")), String[][].class);
		ArrayList<String[]> newList = new ArrayList<String[]>();
		
		Guild guild = this.app.getJDA().getGuildById(this.guildId);
		
		for(String[] spot : in) {
			try {
				User user = this.app.getJDA().getUserById(spot[0]);
				
				if(!guild.isMember(user)) {
					System.out.println("USER VALIDATED, NOT IN SERVER");
					continue;
				}
				
				if(!guild.getMember(user).getRoles().contains(guild.getRoleById(this.accessRoleId))) {
					System.out.println("MEMBER VALIDATED, DOES NOT HAVE ACCESSROLE");
					continue;
				}
				
				System.out.println("UUID PASSED: " + this.app.getJDA().getGuildById(this.guildId).getMemberById(spot[0]).getEffectiveName() + " - "+ spot[1]);
				newList.add(spot);
			} catch (Exception e) {
				System.out.println("USER REMOVED FROM MC WHITELIST: " + spot[0]);
			}
		}
		
		String[][] out = new String[newList.size()][];
		
		newList.toArray(out);
		
		FileOutputStream fos = new FileOutputStream(new File(this.outputFile));
		
		fos.write(g.toJson(out).getBytes(Charset.forName("UTF-8")));
		fos.flush();
		fos.close();
		
		System.out.println("WHITELIST HANDLE REFRESH - " + this.guildId + " - " + this.outputFile);
	}
	
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().equals(event.getJDA().getSelfUser()))
			return;
		if(!event.getChannelType().equals(ChannelType.TEXT))
			return;
		
		if(event.getGuild().getId().equals(this.guildId) && event.getTextChannel().getId().equals(this.textChannelId)) {
			
			
					
			
			String username = event.getMessage().getContentDisplay();
			
			try {
				if(!event.getMember().getRoles().contains(event.getGuild().getRoleById(this.accessRoleId))) {
					throw new Exception();
				}
				
				URL url = new URL(fetchUrl + username);
				InputStream is = url.openStream();
				String f = new String(is.readAllBytes());
				is.close();

				if(username.length() < 4 || username.length() > 16) {
					throw new Exception();
				}
				
				
				Gson g = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
				
				PlayerUUID uuidObject = g.fromJson(f, PlayerUUID.class);
				
				String[][] whitelist = this.amendUserToWhitelist(g.fromJson(new String(Files.readAllBytes(Paths.get(this.outputFile)), Charset.forName("UTF-8")), String[][].class), 
						new String[] {event.getAuthor().getId(), uuidObject.getUUID() });
				
				
				
				FileOutputStream fos = new FileOutputStream(new File(this.outputFile));
				
				fos.write(g.toJson(whitelist).getBytes(Charset.forName("UTF-8")));
				fos.flush();
				fos.close();
				
				
				
				
				event.getChannel().sendMessage("Success! Your whitelisted account has been set to: ```Username: " + uuidObject.getName() + "\nUUID: " + uuidObject.getUUID() + "```").complete().delete().queueAfter(5, TimeUnit.SECONDS);
				
			} catch (Exception e) {
				event.getChannel().sendMessage("An error has occured; User not placed on whitelist: ```" + event.getMessage().getContentDisplay() + "```").complete().delete().queueAfter(5, TimeUnit.SECONDS);
			}
			
			event.getMessage().delete().queue();
		}
	}
	

	
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		//TODO remove user from whitelist

		try {
			Gson g = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			String[][] whitelist = g.fromJson(new String(Files.readAllBytes(Paths.get(this.outputFile)),Charset.forName("UTF-8")), String[][].class);
			
			
			
			for(int i = 0; i < whitelist.length; i++) {
				if(whitelist[i][0].equals(event.getUser().getId())) {
					
					String[][] out = new String[whitelist.length - 1][];
					
					for(int a = 0; a < out.length; a++) {
						if(a < i) {
							out[a] = whitelist[a];
							continue;
						}  
						
						out[a] = whitelist[a+1];
					}
					
					String json = g.toJson(out);
					FileOutputStream fos = new FileOutputStream(new File(this.outputFile));
					fos.write(json.getBytes(Charset.forName("UTF-8")));
					fos.flush();
					fos.close();
					return;
					
				}
			}
			
		} catch (Exception e) {
			System.out.println("ERROR OCCURRED TRYING TO REMOVE USER FROM WHITELIST");
		}
	}
	
	private String[][] amendUserToWhitelist(String[][] whitelist, String[] username) {
		
		for(int i = 0; i < whitelist.length; i++) {
			if(whitelist[i][0].equals(username[0])) {
				whitelist[i] = username;
				return whitelist;
			}
		}
		
		
		String[][] out = new String[whitelist.length + 1][];
		
			
			out[0] = username;
		for(int i = 1; i < out.length; i++) {
			out[i] = whitelist[i-1];
		}
		
		return out;
	}
	
	
	


}


class PlayerUUID {
	private String id, name;
	
	public String getName() {
		return this.name;
	}
	
	public String getUUID() {
		if(this.id.length() == 32)
			return this.id.substring(0, 8) + "-" + this.id.substring(8,12) + "-" + this.id.substring(12,16) + "-" + this.id.substring(16, 20) + "-" + this.id.substring(20, 32);
		
		return null;
	}
}