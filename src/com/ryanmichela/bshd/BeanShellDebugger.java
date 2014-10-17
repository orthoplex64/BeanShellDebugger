//Copyright (C) 2011  Ryan Michela
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.ryanmichela.bshd;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import bsh.Interpreter;

public class BeanShellDebugger extends JavaPlugin {
	public static java.io.PrintStream t = System.out;
	public Interpreter bsh;
	public Logger log;
	
	
	public static void main(String[] args) {
		// For Eclipse to create a launch configuration so I can export the project as a runnable jar, needed to include beanshell in the jar
		t.println("This main method does nothing but print this message.");
	}
	
	@Override
	public void onDisable() {}

	@Override
	public void onEnable() {
		log = getServer().getLogger();
		log.info("[bshd] Starting BeanShell Debugger");
		
		// Initialize the data folder
		File dataFolder = getDataFolder();
		if (!dataFolder.exists()) {
			dataFolder.mkdir();
		}
		File allowedPlayersFile = new File(dataFolder, "allowedPlayers.txt");
		if (!allowedPlayersFile.exists()) {
			log.info("[bshd] No allowedPlayers.txt found; creating one");
			try {
				allowedPlayersFile.createNewFile();
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}
		
		bsh = new Interpreter();
		try {
			//bsh.set("portnum", 1337);
			
			// Set up debug environment with globals
			bsh.set("pluginLoader", getPluginLoader());
			bsh.set("pluginManager", getServer().getPluginManager());
			bsh.set("server", getServer());
			bsh.set("classLoader", getClassLoader());
			
			// Create an alias for each plugin name using its class name
			for(Plugin p : getServer().getPluginManager().getPlugins()) {
				String[] cn = p.getClass().getName().split("\\.");
				log.info("[bshd] Regisering object " + cn[cn.length-1]);
				bsh.set(cn[cn.length-1], p);
			}
			
			// Source any .bsh files in the plugin directory
			if (dataFolder.listFiles() != null) {
				for (File file : dataFolder.listFiles()) {
					String fileName = file.getName();
					if (fileName.endsWith(".bsh")) {
						log.info("[bshd] Sourcing file " + fileName);
						bsh.source(file.getPath());
					} else {
						log.info("*** skipping " + file.getAbsolutePath());
					}
				}
			}
			
			bsh.eval("setAccessibility(true)"); // turn off access restrictions
			//bsh.eval("server(portnum)");
			//log.info("[bshd] BeanShell web console at http://localhost:1337");
			//log.info("[bshd] BeanShell telnet console at localhost:1338");
			
			// Register the bshd command
			getCommand("bshd").setExecutor(new BshdCommand(this, bsh));
			
		} catch (Exception e) {
			log.severe("[bshd] Error in BeanShell. " + e.toString());
		}
		
	}

	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		
	}

}
