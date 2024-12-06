/**
 * Copyright PDGH Minecraft Servers & HostLoad © 2013-XXXX
 * Todos os direitos reservados
 * Uso apenas para a PDGH.com.br e https://HostLoad.com.br
 * Caso você tenha acesso a esse sistema, você é privilegiado!
*/

package me.mchiappinam.pdghguerreiro;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
//import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener {
	private Main plugin;
	public Listeners(Main main) {
		plugin=main;
	}

	@EventHandler
	private void onDeath(PlayerDeathEvent e) {
		if(e.getEntity().getKiller() instanceof Player) {
			Player killer = e.getEntity().getKiller();
			if(plugin.participantes.contains(killer.getName())&&plugin.participantes.contains(e.getEntity().getName())) {
				int k = plugin.totalParticipantes.get(killer.getName());
				plugin.totalParticipantes.remove(killer.getName());
				plugin.totalParticipantes.put(killer.getName(), k+1);
				killer.sendMessage("§8§l[Guerreiro] §eVocê matou "+e.getEntity().getName()+" (total = "+(k+1)+")");
			}
		}else{
			if((plugin.getGuerreiroEtapa()!=3)||(plugin.pvpOffNovaArena))
				if(plugin.participantes.contains(e.getEntity().getName())) {
					e.getDrops().clear();
					e.setDroppedExp(0);
				}
		}
		plugin.removePlayer(e.getEntity(),1);
		plugin.checkGuerreiroEnd();
	}
	
	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		if(plugin.getGuerreiroEtapa()==3)
			e.getPlayer().setHealth(0);
		plugin.removePlayer(e.getPlayer(),2);
		if(plugin.participantes.contains(e.getPlayer().getName())) {
			
		}
	}
	
	@EventHandler
	private void onKick(PlayerKickEvent e) {
		if(plugin.getGuerreiroEtapa()==3)
			e.getPlayer().setHealth(0);
		plugin.removePlayer(e.getPlayer(),2);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	private void onDamage(EntityDamageByEntityEvent e) {
		if(plugin.getGuerreiroEtapa()!=0)
			if(e.getEntity() instanceof Player)
				if(e.getDamager() instanceof Player||e.getDamager() instanceof Projectile) {
					Player ent = (Player)e.getEntity();
					Player dam = null;
					if(e.getDamager() instanceof Player)
						dam=(Player)e.getDamager();
					else {
						Projectile a = (Projectile) e.getDamager();
						if(a.getShooter() instanceof Player)
							dam=(Player)a.getShooter();
					}
					if(plugin.participantes.contains(ent.getName()))
						if((plugin.getGuerreiroEtapa()!=3)||(plugin.pvpOffNovaArena)) {
							e.setCancelled(true);
							if(dam!=null)
								dam.sendMessage("§8§l[Guerreiro] §4PvP desativado no momento!");
							return;
						}
				}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	private void onDamageP(PotionSplashEvent e) {
		for(Entity ent2 : e.getAffectedEntities())
			if(ent2 instanceof Player)
				if(plugin.getGuerreiroEtapa()!=0) {
					Player ent = (Player)ent2;
					Player dam = null;
					if(e.getPotion().getShooter() instanceof Player)
						dam=(Player)e.getEntity().getShooter();
					if(plugin.participantes.contains(ent.getName()))
						if((plugin.getGuerreiroEtapa()!=3)||(plugin.pvpOffNovaArena)) {
							e.setCancelled(true);
							if(dam!=null)
								dam.sendMessage("§8§l[Guerreiro] §4PvP desativado no momento!");
							return;
						}
				}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPCmd(PlayerCommandPreprocessEvent e) {
		if(plugin.getGuerreiroEtapa()!=0) {
			for(String cmd : plugin.getConfig().getStringList("comandos.denyEventoOn"))
				if(e.getMessage().toLowerCase().startsWith(cmd)) {
				e.getPlayer().sendMessage("§c§lComando bloqueado no evento guerreiro!");
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent e) {
		if(plugin.getConfig().getBoolean("inventario.semFome"))
			if(plugin.participantes.contains(e.getEntity().getName()))
				e.setCancelled(true);
	}
}
