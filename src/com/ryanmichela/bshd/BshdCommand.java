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

//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.net.SocketTimeoutException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

import bsh.Interpreter;

public class BshdCommand implements CommandExecutor {
	private Plugin plugin;
	private Interpreter bsh;
	
	
	public BshdCommand(Plugin plugin, Interpreter bsh) {
		this.plugin = plugin;
		this.bsh = bsh;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("bshd")) {
			if (!isAllowedPlayer(sender)) {
				sender.sendMessage("You are not allowed to use this command.");
				return false;
			}
			// build the code string
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < args.length; i++) {
				sb.append(args[i]);
				if (i + 1 < args.length) {
					sb.append(" ");
				}
			}
			String statements = sb.toString();
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
				sender.sendMessage("\u00a79Result: \u00a7f" + (evalResult == null ? "null" : evalResult));
			} catch (Throwable thr) {
				StringWriter writer = new StringWriter();
				thr.printStackTrace(new PrintWriter(writer, true));
				sender.sendMessage("\u00a7cError: \u00a7f" + writer.toString());
				return true;
			}
			return true;
		}
		return false;
	}
	
	public boolean isAllowedPlayer(CommandSender sender) {
		if (sender instanceof ConsoleCommandSender) {
			return true;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(plugin.getDataFolder(), "allowedPlayers.txt")));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (sender.getName().equalsIgnoreCase(line)) {
					reader.close();
					return true;
				}
			}
			reader.close();
			return false;
		} catch (Throwable thr) {
			StringWriter writer = new StringWriter();
			thr.printStackTrace(new PrintWriter(writer, true));
			sender.sendMessage("\u00a7cError: \u00a7f" + writer.toString());
			return false;
		}
	}
}
