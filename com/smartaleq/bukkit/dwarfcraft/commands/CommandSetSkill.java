package com.smartaleq.bukkit.dwarfcraft.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.smartaleq.bukkit.dwarfcraft.CommandParser;
import com.smartaleq.bukkit.dwarfcraft.DCCommandException;
import com.smartaleq.bukkit.dwarfcraft.DCPlayer;
import com.smartaleq.bukkit.dwarfcraft.DwarfCraft;
import com.smartaleq.bukkit.dwarfcraft.Skill;
import com.smartaleq.bukkit.dwarfcraft.DCCommandException.Type;

public class CommandSetSkill extends Command {
	private final DwarfCraft plugin;

	public CommandSetSkill(final DwarfCraft plugin) {
		super("SetSkill");
		this.plugin = plugin;
	}
	
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args){
		if (DwarfCraft.debugMessagesThreshold < 1)
			System.out.println("DC1: started command 'setskill'");

		if (args.length==0) {
			plugin.getOut().sendMessage(sender, getDescription());
		} else if (args[0].equalsIgnoreCase("?")) {
			plugin.getOut().sendMessage(sender, getUsage());
		}else{
			try{				
				CommandParser parser = new CommandParser(plugin, sender, args);
				List<Object> desiredArguments = new ArrayList<Object>();
				List<Object> outputList = null;

				DCPlayer dCPlayer = new DCPlayer(plugin, null);
				Skill skill = new Skill(0, null, 0, null, null, null, null, null);
				int level = 0;
				String name;
				desiredArguments.add(dCPlayer);
				desiredArguments.add(skill);
				desiredArguments.add(level);
				
				try {
					outputList = parser.parse(desiredArguments, false);
					dCPlayer = (DCPlayer)outputList.get(0);
					skill    = (Skill)outputList.get(1);
					level    = (Integer)outputList.get(2);
					name     = dCPlayer.getPlayer().getName();
				} catch (DCCommandException e) {
					if (e.getType() == Type.TOOFEWARGS) {
						if (sender instanceof Player){
							desiredArguments.remove(0);
							desiredArguments.add(dCPlayer);
							outputList = parser.parse(desiredArguments, true);
							dCPlayer   = (DCPlayer)outputList.get(2);
							skill      = (Skill)outputList.get(0);
							level      = (Integer)outputList.get(1);
							name       = ((Player)sender).getName();
						}
						else throw new DCCommandException(plugin, Type.CONSOLECANNOTUSE);
					}
					else throw e;
				}
				if(skill == null){
					for(Skill s : dCPlayer.getSkills().values()){
						s.setLevel(level);
						s.setDeposit1(0);
						s.setDeposit2(0);
						s.setDeposit3(0);
					}
					plugin.getOut().sendMessage(sender, "&aAdmin: &eset all skills for player &9" + name + "&e to &3" + level);
					plugin.getDataManager().saveDwarfData(dCPlayer);
				} else { 
					skill.setLevel(level);
					plugin.getOut().sendMessage(sender, "&aAdmin: &eset skill &b" + skill.getDisplayName() + "&e for player &9" + name + "&e to &3" + level);
					plugin.getDataManager().saveDwarfData(dCPlayer);
				}
			} catch (DCCommandException e) {
				e.describe(sender);
				sender.sendMessage(this.usageMessage);
				return false;		
			}
		}
		return true;		
	}
}
