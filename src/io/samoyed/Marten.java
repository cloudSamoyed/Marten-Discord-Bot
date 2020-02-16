package io.samoyed;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;

import javax.security.auth.login.LoginException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.samoyed.marten.CommandHandle;
import io.samoyed.marten.MinecraftHandle;
import io.samoyed.marten.PronounRolesHandle;
import io.samoyed.marten.ReactRoleHandle;
import io.samoyed.marten.AccessRoleHandle;
import io.samoyed.marten.api.CommandSpec;
import io.samoyed.marten.cmd.DebugCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Marten {
	
	private String token, cmdPrefix;
	private JDA jda;
	private static String charset = "UTF-8";
	private CommandSpec[] commands;
	private ReactRoleHandle[] reactRoles;
	private PronounRolesHandle pronounRoles;
	private AccessRoleHandle[] accessRoles;
	private MinecraftHandle minecraftWhitelist;
	
	public static void main(String[] args) {
	    try {
			Marten app = Marten.build("settings.json");
			app.start();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(1);
	    }
		
		
	}
	
	public static Marten build(String settingsFile) throws IOException {	
		Gson g = new Gson();
		String json = new String(Files.readAllBytes(Paths.get(settingsFile)), Charset.forName(charset));
		return g.fromJson(json, Marten.class);
	}
	
	public Marten() {
		this.commands = new CommandSpec[] {
				new DebugCommand(new String[] {"debug", "d"}, this)
		};
	}
	
	
	public void start() throws LoginException, InterruptedException, JsonSyntaxException, IOException {
		JDABuilder builder = new JDABuilder(this.token);
	    builder.setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE));
	    builder.setBulkDeleteSplittingEnabled(false);
	    this.jda = builder.build().awaitReady();
	    
	    this.jda.addEventListener(new CommandHandle(this));
	    
	    this.pronounRoles.initializeHandle(this);
	    this.jda.addEventListener(this.pronounRoles);
	    
	    
	    for(ReactRoleHandle r : reactRoles) {
	    	r.initializeHandle(this);
		    this.jda.addEventListener(r);
	    }
	    
	    for(AccessRoleHandle a : accessRoles) {
	    	a.initializeHandle(this);
		    this.jda.addEventListener(a);
	    }
	    
	    
	    this.minecraftWhitelist.initializeHandle(this);
	    this.jda.addEventListener(this.minecraftWhitelist);
	    
	    System.out.println(": ------- MARTEN SUCCESSFULLY STARTED!");
	    
	}
	
	public JDA getJDA() {
		return this.jda;
	}
	
	public String getCommandPrefix() {
		return this.cmdPrefix;
	}
	
	public CommandSpec[] getCommands() {
		return this.commands;
	}
	
	public ReactRoleHandle[] getReactRoleHandles() {
		return this.reactRoles;
	}

	public MinecraftHandle getMinecraftHandle() {
		return this.minecraftWhitelist;
	}
}

