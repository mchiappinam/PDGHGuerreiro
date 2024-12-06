/**
 * Copyright PDGH Minecraft Servers & HostLoad © 2013-XXXX
 * Todos os direitos reservados
 * Uso apenas para a PDGH.com.br e https://HostLoad.com.br
 * Caso você tenha acesso a esse sistema, você é privilegiado!
*/

package me.mchiappinam.pdghguerreiro;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Comando implements CommandExecutor {
	private Main plugin;
	public Comando(Main main) {
		plugin=main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("guerreiro")) {
			if(args.length==0) {
				if(sender==plugin.getServer().getConsoleSender()) {
					sender.sendMessage("§8§l[Guerreiro] §cConsole bloqueado de executar o comando!");
					return true;
				}
				if(plugin.getGuerreiroEtapa()==0) {
					sender.sendMessage("§8§l[Guerreiro] §cO evento guerreiro não está acontecendo!");
					return true;
				}
				if(plugin.getGuerreiroEtapa()>1) {
					sender.sendMessage("§8§l[Guerreiro] §cO evento guerreiro já começou!");
					return true;
				}
				if(plugin.participantes.contains(sender.getName())) {
					sender.sendMessage("§8§l[Guerreiro] §cVocê já entrou no evento guerreiro!");
					return true;
				}
				if(plugin.getConfig().contains("Bans."+sender.getName().toLowerCase())) {
					sender.sendMessage("§8§l[Guerreiro] §cVocê está banido do evento guerreiro!");
					sender.sendMessage("§8§l[Guerreiro] §cBanido por "+plugin.getConfig().getString("Bans."+sender.getName().toLowerCase()+".Por")+" em "+plugin.getConfig().getString("Bans."+sender.getName().toLowerCase()+".Data"));
					return true;
				}
                if(((Player)sender).isInsideVehicle()) {
				     sender.sendMessage("§8§l[Guerreiro] §cVocê está dentro de um veículo!");
				     return true;
				}
                if(((Player)sender).isDead()) {
				     sender.sendMessage("§8§l[Guerreiro] §cVocê está morto!");
				     return true;
				}
        		if((plugin.getConfig().getBoolean("inventario.obrigatorioVazio"))&&(!plugin.isInventoryEmpty((Player)sender))) {
        				sender.sendMessage("§8§l[Guerreiro] §cSeu inventário deve estar vazio!");
        				return true;
        			}
				plugin.addPlayer((Player)sender);
				return true;
			}
			else {
				if(args[0].equalsIgnoreCase("sair")) {
					if(plugin.getGuerreiroEtapa()==0) {
						sender.sendMessage("§8§l[Guerreiro] §cO evento guerreiro não está aberto!");
						return true;
					}
					if(plugin.getGuerreiroEtapa()!=1) {
						sender.sendMessage("§8§l[Guerreiro] §cVocê não pode sair agora!");
						return true;
					}
					plugin.removePlayer((Player)sender,0);
					return true;
				}
				if(args[0].equalsIgnoreCase("camarote")) {
					if(!sender.hasPermission("pdgh.admin")) {
						sender.sendMessage("§8§l[Guerreiro] §cVocê não tem permissão para executar esse comando!");
						return true;
					}
					if(plugin.getGuerreiroEtapa()==0) {
						sender.sendMessage("§8§l[Guerreiro] §cO evento guerreiro não está acontecendo!");
						return true;
					}
					if(plugin.participantes.contains(sender.getName())) {
						sender.sendMessage("§8§l[Guerreiro] §cVocê não pode ir para o camarote participando do evento guerreiro!");
						return true;
					}
					((Player)sender).teleport(plugin.camarote);
					sender.sendMessage("§8§l[Guerreiro] §eVocê foi para o camarote do Guerreiro!");
					return true;
				}
				//outro cmds, admin!
				if(!sender.hasPermission("pdgh.admin")) {
					sender.sendMessage("§8§l[Guerreiro] §cVocê não tem permissão para executar esse comando!");
					return true;
				}
				if(args[0].equalsIgnoreCase("forcestart")) {
					if(plugin.getGuerreiroEtapa()!=0) {
						sender.sendMessage("§8§l[Guerreiro] §cJá existe um evento guerreiro sendo executado!");
						return true;
					}
					if(plugin.getGuerreiroEtapa()==0&&!plugin.canStart) {
						sender.sendMessage("§8§l[Guerreiro] §cUm evento guerreiro está sendo finalizado!");
						return true;
					}
					sender.sendMessage("§8§l[Guerreiro] §eEvento guerreiro sendo iniciado!");
					plugin.prepareGuerreiro();
					return true;
				}
				if(args[0].equalsIgnoreCase("forcestop")) {
					if(plugin.getGuerreiroEtapa()==0) {
						sender.sendMessage("§8§l[Guerreiro] §cNão há nenhum evento guerreiro sendo executado!");
						return true;
					}
					plugin.cancelGuerreiro();
					sender.sendMessage("§8§l[Guerreiro] §eEvento guerreiro sendo parado!");
					return true;
				}
				if(args[0].equalsIgnoreCase("kick")) {
					if(args.length<2) {
						sender.sendMessage("§8§l[Guerreiro] §c/guerreiro kick <nome>");
						return true;
					}
					String nome = args[1].toLowerCase();
					Player p = plugin.getServer().getPlayer(nome);
					if(p==null) {
						sender.sendMessage("§8§l[Guerreiro] §cJogador não encontrado!");
						return true;
					}
					plugin.removePlayer(p, 3);
					sender.sendMessage("§8§l[Guerreiro] §e"+nome+" foi kickado do evento guerreiro!");
					return true;
				}
				if(args[0].equalsIgnoreCase("info")) {
					if(plugin.getGuerreiroEtapa()!=3) {
						sender.sendMessage("§8§l[Guerreiro] §cO evento guerreiro não está acontecendo!");
						return true;
					}
					sender.sendMessage("§8§l[Guerreiro] §eRestam "+plugin.participantes.size()+" jogadores dentro do guerreiro!");
					return true;
				}
				if(args[0].equalsIgnoreCase("ban")) {
					if(args.length<2) {
						sender.sendMessage("§8§l[Guerreiro] §c/guerreiro ban <nome>");
						return true;
					}
					String nome = args[1].toLowerCase();
					plugin.getConfig().set("Bans."+nome+".Por", sender.getName());
					plugin.getConfig().set("Bans."+nome+".Data", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
					plugin.saveConfig();
					Player p = plugin.getServer().getPlayerExact(nome);
					if(p!=null)
						plugin.removePlayer(p, 3);
					sender.sendMessage("§8§l[Guerreiro] §e"+nome+" foi banido dos eventos guerreiroes!");
					return true;
				}
				if(args[0].equalsIgnoreCase("unban")) {
					if(args.length<2) {
						sender.sendMessage("§8§l[Guerreiro] §c/guerreiro ban <nome>");
						return true;
					}
					String nome = args[1].toLowerCase();
					if(!plugin.getConfig().contains("Bans."+nome)) {
						sender.sendMessage("§8§l[Guerreiro] §cNome não encontrado!");
						return true;
					}
					plugin.getConfig().set("Bans."+nome, null);
					plugin.saveConfig();
					sender.sendMessage("§8§l[Guerreiro] §e"+nome+" foi desbanido dos eventos guerreiroes!");
					return true;
				}
				if(args[0].equalsIgnoreCase("setspawn")) {
					if(sender==plugin.getServer().getConsoleSender()) {
						sender.sendMessage("§8§l[Guerreiro] §cConsole bloqueado de executar o comando!");
						return true;
					}
					Player p = (Player)sender;
					plugin.spawn=p.getLocation();
					plugin.getConfig().set("arena.entrada", plugin.spawn.getWorld().getName()+";"+plugin.spawn.getX()+";"+plugin.spawn.getY()+";"+plugin.spawn.getZ()+";"+plugin.spawn.getYaw()+";"+plugin.spawn.getPitch());
					plugin.saveConfig();
					sender.sendMessage("§8§l[Guerreiro] §eSpawn marcado!");
					return true;
				}
				if(args[0].equalsIgnoreCase("setsaida")) {
					if(sender==plugin.getServer().getConsoleSender()) {
						sender.sendMessage("§8§l[Guerreiro] §cConsole bloqueado de executar o comando!");
						return true;
					}
					Player p = (Player)sender;
					plugin.saida=p.getLocation();
					plugin.getConfig().set("arena.saida", plugin.saida.getWorld().getName()+";"+plugin.saida.getX()+";"+plugin.saida.getY()+";"+plugin.saida.getZ()+";"+plugin.saida.getYaw()+";"+plugin.saida.getPitch());
					plugin.saveConfig();
					sender.sendMessage("§8§l[Guerreiro] §eSaída marcada!");
					return true;
				}
				if(args[0].equalsIgnoreCase("setcamarote")) {
					if(sender==plugin.getServer().getConsoleSender()) {
						sender.sendMessage("§8§l[Guerreiro] §cConsole bloqueado de executar o comando!");
						return true;
					}
					Player p = (Player)sender;
					plugin.camarote=p.getLocation();
					plugin.getConfig().set("arena.camarote", plugin.camarote.getWorld().getName()+";"+plugin.camarote.getX()+";"+plugin.camarote.getY()+";"+plugin.camarote.getZ()+";"+plugin.camarote.getYaw()+";"+plugin.camarote.getPitch());
					plugin.saveConfig();
					sender.sendMessage("§8§l[Guerreiro] §eCamarote marcado!");
					return true;
				}
				if(args[0].equalsIgnoreCase("setmenor")) {
					if(sender==plugin.getServer().getConsoleSender()) {
						sender.sendMessage("§8§l[Guerreiro] §cConsole bloqueado de executar o comando!");
						return true;
					}
					Player p = (Player)sender;
					plugin.arenaMenor=p.getLocation();
					plugin.getConfig().set("arena.Menor", plugin.arenaMenor.getWorld().getName()+";"+plugin.arenaMenor.getX()+";"+plugin.arenaMenor.getY()+";"+plugin.arenaMenor.getZ()+";"+plugin.arenaMenor.getYaw()+";"+plugin.arenaMenor.getPitch());
					plugin.saveConfig();
					sender.sendMessage("§8§l[Guerreiro] §eArena menor marcada!");
					return true;
				}
				if(args[0].equalsIgnoreCase("reload")) {
					if(plugin.getGuerreiroEtapa()!=0) {
						sender.sendMessage("§8§l[Guerreiro] §cHá um evento guerreiro acontecendo!");
						return true;
					}
					plugin.reloadConfig();
					String ent[] = plugin.getConfig().getString("arena.entrada").split(";");
					plugin.spawn = new Location(plugin.getServer().getWorld(ent[0]),Double.parseDouble(ent[1]),Double.parseDouble(ent[2]),Double.parseDouble(ent[3]),Float.parseFloat(ent[4]),Float.parseFloat(ent[5]));
					String sai[] = plugin.getConfig().getString("arena.saida").split(";");
					plugin.saida = new Location(plugin.getServer().getWorld(sai[0]),Double.parseDouble(sai[1]),Double.parseDouble(sai[2]),Double.parseDouble(sai[3]),Float.parseFloat(sai[4]),Float.parseFloat(sai[5]));
					String cam[] = plugin.getConfig().getString("arena.camarote").split(";");
					plugin.camarote = new Location(plugin.getServer().getWorld(cam[0]),Double.parseDouble(cam[1]),Double.parseDouble(cam[2]),Double.parseDouble(cam[3]),Float.parseFloat(cam[4]),Float.parseFloat(cam[5]));
					String men[] = plugin.getConfig().getString("arena.menor").split(";");
					plugin.arenaMenor = new Location(plugin.getServer().getWorld(men[0]),Double.parseDouble(men[1]),Double.parseDouble(men[2]),Double.parseDouble(men[3]),Float.parseFloat(men[4]),Float.parseFloat(men[5]));
					sender.sendMessage("§8§l[Guerreiro] §eConfiguração recarregada!");
					return true;
				}
				sendHelp((Player)sender);
			}
			return true;
		}
		return true;
	}
	
	private void sendHelp(Player p) {
		p.sendMessage("§d§lPDGHGuerreiro - Comandos do plugin:");
		p.sendMessage("§2/guerreiro ? -§a- Lista de comandos");
		p.sendMessage("§c/guerreiro forcestart -§a- Força o inicio do evento guerreiro");
		p.sendMessage("§c/guerreiro forcestop -§a- Força a parada do evento guerreiro");
		p.sendMessage("§2/guerreiro kick <nome> -§a- Kicka um jogador do evento guerreiro");
		p.sendMessage("§2/guerreiro ban <nome> -§a- Bane um jogador do evento guerreiro");
		p.sendMessage("§2/guerreiro unban <nome> -§a- Desbane um jogador do evento guerreiro");
		p.sendMessage("§2/guerreiro setspawn -§a- Marca local de spawn do evento guerreiro");
		p.sendMessage("§2/guerreiro setsaida -§a- Marca local de saida do evento guerreiro");
		p.sendMessage("§2/guerreiro setcamarote -§a- Marca local do camarote do evento guerreiro");
		p.sendMessage("§2/guerreiro setmenor -§a- Marca local da arena menor do evento guerreiro");
		p.sendMessage("§2/guerreiro info -§a- Mostra quantos jogadores estão dentro do evento guerreiro");
		p.sendMessage("§c/guerreiro reload -§a- Recarrega a configuração");
	}

}
