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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import bsh.Interpreter;

public class BeanShellDebugger extends JavaPlugin {
	public static java.io.PrintStream t = System.out;
	public Interpreter bsh;
	public Logger log;
	public Map<String, PlayerData> playerDatas;
	
	public static void main(String[] args) {
		// For Eclipse to create a launch configuration so I can export the project as a runnable jar, needed to include beanshell in the jar
		t.println("This main method does nothing but print this message.");
	}
	
	@Override
	public void onEnable() {
		log = getServer().getLogger();
		log.info("[bshd] Starting BeanShell Debugger");
		playerDatas = new HashMap<String, PlayerData>();
		
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
			//getCommand("bshd").setExecutor(new BshdCommand(this, bsh));
			
		} catch (Exception e) {
			log.severe("[bshd] Error in BeanShell. " + e.toString());
		}
		
	}
	
	@Override
	public void onLoad() {}
	
	@Override
	public void onDisable() {}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String senderName = sender.getName();
		if (!playerDatas.containsKey(senderName)) {
			playerDatas.put(senderName, new PlayerData());
		}
		PlayerData playerData = playerDatas.get(senderName);
		Player player = null;
		if (cmd.getName().equalsIgnoreCase("bshd")) {
			if (!isAllowedPlayer(sender)) {
				sender.sendMessage("\u00a7cError: \u00a7fYou are not allowed to use this command");
				return false;
			}
			String statements = null;
			if (playerData.options.get("bookin")) {
				sender.sendMessage("Sourcing from book in hand");
				if (!(sender instanceof Player)) {
					sender.sendMessage("\u00a7cError: \u00a7fYou are not capable of holding a book");
					return true;
				}
				player = (Player) sender;
				ItemStack hand = player.getItemInHand();
				if (hand.getType() != Material.BOOK_AND_QUILL && hand.getType() != Material.WRITTEN_BOOK) {
					sender.sendMessage("\u00a7cError: \u00a7fYou are not holding a written book");
					return true;
				}
				List<String> pages = ((BookMeta) hand.getItemMeta()).getPages();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < pages.size(); i++) {
					sb.append(pages.get(i));
				}
				statements = sb.toString();
			} else {
				// build the code string
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < args.length; i++) {
					sb.append(args[i]);
					if (i + 1 < args.length) {
						sb.append(" ");
					}
				}
				statements = sb.toString();
			}
			try {
//				Socket s = new Socket("localhost", 1338);
//				s.setSoTimeout(10);
//				BufferedReader in = new BufferedReader
//				  (new InputStreamReader(s.getInputStream()));
//				PrintWriter out = new PrintWriter
//				  (s.getOutputStream(), true /* autoFlush */);
//				
//				out.println(sb.toString());
//				
//				boolean more = true;
//				int ln = 0;
//				try {
//					while (more) {
//					    String line = in.readLine();
//					    if (line == null)
//					    	more = false;
//					    else if (ln == 0) {
//					    	ln++;
//						} else if (ln == 1) {
//					    	sender.sendMessage(line.substring(6));
//					    	ln++;
//					    } else {
//					    	sender.sendMessage(line);
//					    }
//					 }
//				} catch (SocketTimeoutException e) {
//					
//				}
//				
//				s.close();
//				in.close();
//				out.close();
				Object evalResult = bsh.eval(statements);
				String evalResultString = String.valueOf(evalResult);
				if (playerData.options.get("bookout")) {
					sender.sendMessage("Writing result to a book");
					if (!(sender instanceof Player)) {
						sender.sendMessage("\u00a7cError: \u00a7fYou are not capable of receiving a book");
						return true;
					}
					player = (Player) sender;
					ItemStack outBook = new ItemStack(Material.WRITTEN_BOOK);
					BookMeta outBookMeta = (BookMeta) outBook.getItemMeta();
					outBookMeta.setTitle("Result");
					outBookMeta.setAuthor("BeanShellDebugger");
					List<String> pages = new ArrayList<String>();
					for (int i = 0; i * 256 < evalResultString.length(); i++) {
						pages.add(evalResultString.substring(i * 256, Math.min(i * 256 + 256, evalResultString.length())));
					}
					outBookMeta.setPages(pages);
					outBook.setItemMeta(outBookMeta);
					player.getInventory().addItem(outBook);
				} else {
					sender.sendMessage("\u00a79Result: \u00a7f" + evalResultString);
				}
			} catch (Throwable thr) {
				//StringWriter writer = new StringWriter();
				//thr.printStackTrace(new PrintWriter(writer, true));
				//sender.sendMessage("\u00a7cError: \u00a7f" + writer.toString());
				sender.sendMessage("\u00a7cError: \u00a7f" + thr.getMessage());
				return true;
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("bshdopt")) {
			if (!isAllowedPlayer(sender)) {
				sender.sendMessage("\u00a7cError: \u00a7fYou are not allowed to use this command");
				return false;
			}
			if (args.length == 1) {
				String option = args[0].toLowerCase();
				if (!playerData.options.containsKey(option)) {
					sender.sendMessage("\u00a7cError: \u00a7fOption not found");
					return true;
				}
				playerData.options.put(option, !playerData.options.get(option));
				sender.sendMessage("Option " + option + " toggled to " + playerData.options.get(option));
				return true;
			} else {
				sender.sendMessage("/bshdopt <option>");
				sender.sendMessage("Toggle one of your options:");
				for (String key : playerData.options.keySet()) {
					sender.sendMessage(key + " == " + playerData.options.get(key));
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean isAllowedPlayer(CommandSender sender) {
		if (sender instanceof ConsoleCommandSender) {
			return true;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(getDataFolder(), "allowedPlayers.txt")));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (sender.getName().equalsIgnoreCase(line)) {
					reader.close();
					return true;
				}
			}
			reader.close();
			return false;
		} catch (Throwable thr) {
			//StringWriter writer = new StringWriter();
			//thr.printStackTrace(new PrintWriter(writer, true));
			//sender.sendMessage("\u00a7cError: \u00a7f" + writer.toString());
			sender.sendMessage("\u00a7cError: \u00a7f" + thr.getMessage());
			return false;
		}
	}
}
