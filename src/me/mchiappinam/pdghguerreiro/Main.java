/**
 * Copyright PDGH Minecraft Servers & HostLoad © 2013-XXXX
 * Todos os direitos reservados
 * Uso apenas para a PDGH.com.br e https://HostLoad.com.br
 * Caso você tenha acesso a esse sistema, você é privilegiado!
*/

package me.mchiappinam.pdghguerreiro;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import com.p000ison.dev.simpleclans2.api.SCCore;
import com.p000ison.dev.simpleclans2.api.clanplayer.ClanPlayerManager;

import me.mchiappinam.pdghapiutility.Metodos;
import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
//import org.bukkit.scoreboard.Team;

public class Main extends JavaPlugin {
	protected SCCore core;
	protected SimpleClans core2;
	protected static Economy econ = null;
	public boolean vault = false;
	protected int version = 0;
	
	private int guerreiroEtapa = 0;
	private int diaautoStart;
	private int horaautoStart;
	private int minautoStart;
	protected boolean canStart = true;
	
	protected Location spawn;
	protected Location saida;
	protected Location camarote;
	protected Location arenaMenor;

	protected boolean jaTeleportado = false;
	protected boolean pvpOffNovaArena = false;
	protected boolean comecouComMaisDeDois = false;
	public boolean noob=false;
	public boolean eventos=false;
	public boolean legendchat=false;
	public boolean noobEfetuado=false;
	public boolean noobEfetuadoo=true;
	int tteleportarNovaArena;
	int tliberarPvPNovaArena;
	
	protected String key=null;
	
	protected HashMap<String,Integer> totalParticipantes = new HashMap<String,Integer>();
	protected List<String> participantes = new ArrayList<String>();
	protected List<String> vips = new ArrayList<String>();
	public boolean apiutility=false;
	private me.mchiappinam.pdghapiutility.Main api;
	
	@Override
    public void onEnable() {
		getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2ativando... - Plugin by: mchiappinam");
		getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2verificando config... - Plugin by: mchiappinam");
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			try {
				getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2salvando config pela primeira vez... - Plugin by: mchiappinam");
				saveResource("config_template.yml", false);
				File file2 = new File(getDataFolder(), "config_template.yml");
				file2.renameTo(new File(getDataFolder(), "config.yml"));
				getServer().getConsoleSender()
						.sendMessage("§3[PDGHGuerreiro] §2config salva... - Plugin by: mchiappinam");
			} catch (Exception e) {
			}
		}
		getServer().getPluginCommand("guerreiro").setExecutor(new Comando(this));
		getServer().getPluginManager().registerEvents(new Listeners(this), this);
		
		if (!setupEconomy()) {
			getLogger().warning("ERRO: Vault nao encontrado!");
			vault = false;
		} else {
			getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2Sucesso: Vault encontrado.");
			vault = true;
		}

		if (hookSimpleClans()) {
			getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2Sucesso: SimpleClans2 encontrado.");
			version = 2;
		} else if (getServer().getPluginManager().getPlugin("SimpleClans") != null) {
			getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2Sucesso: SimpleClans1 encontrado.");
			core2 = ((SimpleClans) getServer().getPluginManager().getPlugin("SimpleClans"));
			version = 1;
		} else {
			version = 0;
			getLogger().warning("ERRO: SimpleClans ou SimpleClans2 nao encontrado!");
		}
		
		if (getServer().getPluginManager().getPlugin("PDGHNoob") == null) {
			getLogger().warning("PDGHNoob API nao encontrado!");
			noob=false;
		}else{
			getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2Sucesso: PDGHNoob encontrado.");
			noob=true;
		}
		
		if (getServer().getPluginManager().getPlugin("PDGHEventos") == null) {
			getLogger().warning("PDGHEventos API nao encontrado!");
			eventos=false;
		}else{
			getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2Sucesso: PDGHEventos encontrado.");
			eventos=true;
		}
		
		if (getServer().getPluginManager().getPlugin("PDGHAPIUtility") == null) {
			getLogger().warning("PDGHAPIUtility nao encontrado!");
			apiutility=false;
		}else{
			getLogger().info("PDGHAPIUtility ativado!");
			api = (me.mchiappinam.pdghapiutility.Main)getServer().getPluginManager().getPlugin("PDGHAPIUtility");
			apiutility=true;
		}
		
		if (getServer().getPluginManager().getPlugin("Legendchat") == null) {
			getLogger().warning("Legendchat API nao encontrado!");
			legendchat=false;
		}else{
			getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2Sucesso: Legendchat encontrado.");
			getServer().getPluginManager().registerEvents(new ListenerLegendchat(this), this);
			legendchat=true;
		}
		
		if(getConfig().getBoolean("autoStart.ativado")) {
			diaautoStart = Utils.strToCalendar(getConfig().getString("autoStart.dia"));
			getServer().getConsoleSender().sendMessage("§2<> Data automatica:");
			getServer().getConsoleSender().sendMessage("§2Dia = "+diaautoStart);
			horaautoStart = Integer.parseInt(getConfig().getString("autoStart.hora").substring(0,2));
			minautoStart = Integer.parseInt(getConfig().getString("autoStart.hora").substring(2,4));
			getServer().getConsoleSender().sendMessage("§2Hora = "+(horaautoStart<10?"0"+horaautoStart:horaautoStart)+":"+(minautoStart<10?"0"+minautoStart:minautoStart));
		}
		
		String ent[] = getConfig().getString("arena.entrada").split(";");
		spawn = new Location(getServer().getWorld(ent[0]),Double.parseDouble(ent[1]),Double.parseDouble(ent[2]),Double.parseDouble(ent[3]),Float.parseFloat(ent[4]),Float.parseFloat(ent[5]));
		String sai[] = getConfig().getString("arena.saida").split(";");
		saida = new Location(getServer().getWorld(sai[0]),Double.parseDouble(sai[1]),Double.parseDouble(sai[2]),Double.parseDouble(sai[3]),Float.parseFloat(sai[4]),Float.parseFloat(sai[5]));
		String cam[] = getConfig().getString("arena.camarote").split(";");
		camarote = new Location(getServer().getWorld(cam[0]),Double.parseDouble(cam[1]),Double.parseDouble(cam[2]),Double.parseDouble(cam[3]),Float.parseFloat(cam[4]),Float.parseFloat(cam[5]));
		String men[] = getConfig().getString("arena.menor").split(";");
		arenaMenor = new Location(getServer().getWorld(men[0]),Double.parseDouble(men[1]),Double.parseDouble(men[2]),Double.parseDouble(men[3]),Float.parseFloat(men[4]),Float.parseFloat(men[5]));
		
		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				if(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==diaautoStart)
					if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)==horaautoStart)
						if(Calendar.getInstance().get(Calendar.MINUTE)==minautoStart)
							prepareGuerreiro();
			}
		}, 0, 700);
		
		getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2ativado - Plugin by: mchiappinam");
		getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2Acesse: http://pdgh.com.br/");
		getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2Plugin manipulado por HostLoad.");
		getServer().getConsoleSender().sendMessage("§3[PDGHGuerreiro] §2Pode ser desativado a qualquer momento para quem nao hospeda na HostLoad!");
	}
	
	@Override
    public void onDisable() {
		getServer().getConsoleSender().sendMessage("§3[Guerreiro] §2desativado - Plugin by: mchiappinam");
		getServer().getConsoleSender().sendMessage("§3[Guerreiro] §2Acesse: http://pdgh.com.br/");
	}
	
    public Metodos getMetodos() {
    	return api.getMetodos();
    }
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

	private boolean hookSimpleClans() {
		try {
			for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
				if ((plugin instanceof SCCore)) {
					core = ((SCCore) plugin);
					return true;
				}
			}
		} catch (NoClassDefFoundError e) {
			return false;
		}
		return false;
	}

	public ClanPlayerManager getClanPlayerManager() {
		return core.getClanPlayerManager();
	}
	
	
	protected void prepareGuerreiro() {
		String ent[] = getConfig().getString("arena.entrada").split(";");
		spawn = new Location(getServer().getWorld(ent[0]),Double.parseDouble(ent[1]),Double.parseDouble(ent[2]),Double.parseDouble(ent[3]),Float.parseFloat(ent[4]),Float.parseFloat(ent[5]));
		String sai[] = getConfig().getString("arena.saida").split(";");
		saida = new Location(getServer().getWorld(sai[0]),Double.parseDouble(sai[1]),Double.parseDouble(sai[2]),Double.parseDouble(sai[3]),Float.parseFloat(sai[4]),Float.parseFloat(sai[5]));
		String cam[] = getConfig().getString("arena.camarote").split(";");
		camarote = new Location(getServer().getWorld(cam[0]),Double.parseDouble(cam[1]),Double.parseDouble(cam[2]),Double.parseDouble(cam[3]),Float.parseFloat(cam[4]),Float.parseFloat(cam[5]));
		String men[] = getConfig().getString("arena.menor").split(";");
		arenaMenor = new Location(getServer().getWorld(men[0]),Double.parseDouble(men[1]),Double.parseDouble(men[2]),Double.parseDouble(men[3]),Float.parseFloat(men[4]),Float.parseFloat(men[5]));
		if(guerreiroEtapa!=0)
			return;
		//getServer().dispatchCommand(getServer().getConsoleSender(), "simpleclans globalff allow");
		guerreiroEtapa=1;
		tirarTagsAntigas();
		if(eventos) {
			me.mchiappinam.pdgheventos.Comandos.cancelarTodosEventos();
			me.mchiappinam.pdgheventos.Comandos.setEvento("API");
		}
    	if(apiutility) {
    		getMetodos().sendTweet("EVENTO GUERREIRO INICIANDO...");
    	}
		messagePrepare(getConfig().getInt("timers.preparar.avisos"));
	}
	private void messagePrepare(final int vezes) {
		canStart=true;
		if(guerreiroEtapa!=1)
			return;
		canStart=false;
		if(vezes==0)
			preparedGuerreiro();
		else {
			getServer().broadcastMessage(" ");
			getServer().broadcastMessage("§8§l[Guerreiro] §eEvento guerreiro automático começando!");
			getServer().broadcastMessage("§8§l[Guerreiro] §ePara participar digite: §6§l/guerreiro");
			getServer().broadcastMessage("§8§l[Guerreiro] §ePremio: §c$"+getConfig().getDouble("premios.dinheiro")+"§e e tag "+getConfig().getString("premios.tag").replace("&", "§"));
			getServer().broadcastMessage("§8§l[Guerreiro] §eTempo restante: §c"+vezes*getConfig().getInt("timers.preparar.tempoEntre")+" segundos");
			getServer().broadcastMessage("§8§l[Guerreiro] §eJogadores: "+participantes.size());
			getServer().broadcastMessage(" ");
		}
		getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				canStart=true;
				if(guerreiroEtapa!=1)
					return;
				canStart=false;
				messagePrepare(vezes-1);
			}
		}, 20*getConfig().getInt("timers.preparar.tempoEntre"));
	}
	
	
	
	
	
	protected void preparedGuerreiro() {
		if(participantes.size()<getConfig().getInt("jogadoresMinimos")) {
			cancelGuerreiro();
			getServer().broadcastMessage(" ");
			getServer().broadcastMessage("§8§l[Guerreiro] §eEvento guerreiro automático §cCANCELADO!");
			getServer().broadcastMessage("§8§l[Guerreiro] §eMotivo: Quantidade de jogadores menor que "+getConfig().getInt("jogadoresMinimos"));
			getServer().broadcastMessage(" ");
			return;
		}
		guerreiroEtapa=2;
		getServer().broadcastMessage(" ");
		getServer().broadcastMessage("§8§l[Guerreiro] §eEvento guerreiro sendo INICIADO!");
		getServer().broadcastMessage("§8§l[Guerreiro] §eTeleporte para o evento BLOQUEADO!");
		getServer().broadcastMessage(" ");
		canStart=false;
		messageiniciando(getConfig().getInt("timers.iniciando.avisos"));
	}
	private void messageiniciando(final int vezes) {
		canStart=true;
		if(guerreiroEtapa!=2)
			return;
		canStart=false;
		if(vezes==0)
			startGuerreiro();
		else {
			sendMessageGuerreiro(" ");
			sendMessageGuerreiro("§8§l[Guerreiro] §eEvento guerreiro automático começando!");
			sendMessageGuerreiro("§8§l[Guerreiro] §eTempo inicial para os jogadores se prepararem!");
			sendMessageGuerreiro("§8§l[Guerreiro] §eTempo restante: §c"+vezes*getConfig().getInt("timers.iniciando.tempoEntre")+" segundos");
			sendMessageGuerreiro(" ");
		}
		getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				canStart=true;
				if(guerreiroEtapa!=2)
					return;
				canStart=false;
				messageiniciando(vezes-1);
			}
		}, 20*getConfig().getInt("timers.iniciando.tempoEntre"));
	}
	
	
	
	
	
	protected void startGuerreiro() {
		canStart=true;
		guerreiroEtapa=3;
		if(participantes.size()>getConfig().getInt("arena.jogadoresMenor"))
			comecouComMaisDeDois=true;
		sendMessageGuerreiro(" ");
		sendMessageGuerreiro("§8§l[Guerreiro] §eVALENDO!");
		sendMessageGuerreiro("§8§l[Guerreiro] §eVALENDO!");
		sendMessageGuerreiro("§8§l[Guerreiro] §eVALENDO!");
		sendMessageGuerreiro(" ");
    	if(apiutility) {
    		getMetodos().sendTweet("EVENTO GUERREIRO COMEÇOU! O PVP JÁ ESTÁ ON!");
    	}
	}
	
	
	
	
	
	protected void checkGuerreiroEnd() {
		if(participantes.size()==1)
			if(guerreiroEtapa==3) {
				guerreiroEtapa=4;
				String vencedor = participantes.get(0);
				if(eventos)
					me.mchiappinam.pdgheventos.Comandos.setEvento("nenhum");
				noobEfetuado=true;
				cteleportarNovaArena();
				cliberarPvPNovaArena();
				jaTeleportado = false;
				pvpOffNovaArena = false;
				comecouComMaisDeDois = false;
				econ.depositPlayer(vencedor, getConfig().getDouble("premios.dinheiro"));
				String v1 = null;
				int v1_v = -1;
				String v2 = null;
				int v2_v = -1;
				String v3 = null;
				int v3_v = -1;
				for(String n : totalParticipantes.keySet()){
					int matou = totalParticipantes.get(n);
					if(matou>v1_v) {
						v3_v=v2_v;
						v3=v2;
						v2_v=v1_v;
						v2=v1;
						v1=n;
						v1_v=matou;
					}else if(matou>v2_v) {
						v3=v2;
						v3_v=v2_v;
						v2=n;
						v2_v=matou;
					}else if(matou>v2_v) {
						v3=n;
						v3_v=matou;
					}
				}
				darTagsNovas(vencedor);

		    	if(apiutility) {
		    		getMetodos().sendTweet(vencedor+" VENCEU O EVENTO GUERREIRO. TOP KILLS: 1º "+v1+" ("+v1_v+")"+(v2!=null?", 2º "+v2+" ("+v2_v+")" : "")+(v3!=null?", 3º "+v3+" ("+v3_v+")" : ""));
		    	}
				getServer().broadcastMessage(" ");
				getServer().broadcastMessage("§8§l[Guerreiro] §eEvento guerreiro FINALIZADO!");
				getServer().broadcastMessage("§8§l[Guerreiro] §eVencedor: §l"+vencedor);
				getServer().broadcastMessage("§8§l[Guerreiro] §ePremio: §c$"+getConfig().getDouble("premios.dinheiro")+"§e e tag "+getConfig().getString("premios.tag").replace("&", "§"));
				getServer().broadcastMessage("§8§l[Guerreiro] §eTOP 3 KILLS:");
				getServer().broadcastMessage("§8§l[Guerreiro] §e1º colocado: "+v1+" ("+v1_v+")");
				if(v2!=null)
					getServer().broadcastMessage("§8§l[Guerreiro] §e2º colocado: "+v2+" ("+v2_v+")");
				if(v3!=null)
					getServer().broadcastMessage("§8§l[Guerreiro] §e3º colocado: "+v3+" ("+v3_v+")");
				getServer().broadcastMessage(" ");
				sendMessageGuerreiro("§8§l[Guerreiro] §b§lVocê tem "+getConfig().getInt("timers.finalizando")+" segundos para recolher os itens do evento Guerreiro!");
				//getServer().dispatchCommand(getServer().getConsoleSender(), "simpleclans globalff auto");
				getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						finalizarGuerreiro();
					}
				}, 20*getConfig().getInt("timers.finalizando"));
				return;
			}
	}
	
	
	
	
	protected void finalizarGuerreiro() {
		sendMessageGuerreiro(" ");
		sendMessageGuerreiro("§8§l[Guerreiro] §eTempo esgotado!");
		sendMessageGuerreiro("§8§l[Guerreiro] §eFim do evento!");
		sendMessageGuerreiro(" ");
		cancelGuerreiro();
	}
	
	protected void darTagsNovas(String v2) {
		getConfig().set("vencedor", v2);
		saveConfig();
	}
	
	protected void tirarTagsAntigas() {
		getConfig().set("vencedor", "");
		saveConfig();
	}
	
	protected void cancelGuerreiro() {
		if(guerreiroEtapa==0)
			return;
		//getServer().dispatchCommand(getServer().getConsoleSender(), "simpleclans globalff auto");
		guerreiroEtapa=0;
		for(String n : participantes) {
			getServer().getPlayer(n).teleport(saida);
		}
		participantes.clear();
		totalParticipantes.clear();
		if(eventos)
			me.mchiappinam.pdgheventos.Comandos.setEvento("nenhum");
		noobEfetuado=false;
	}
	
	protected int getGuerreiroEtapa() {
		return guerreiroEtapa;
	}
	
	protected boolean isInventoryEmpty(Player p) {
		for(ItemStack i : p.getInventory().getContents())
			if(i != null)
				if (i.getType() != Material.AIR)
					return false;
		for (ItemStack a : p.getInventory().getArmorContents())
			if (a.getType() != Material.AIR )
				return false;
		return true;
	}
	
	@SuppressWarnings("deprecation")
	protected void addPlayer(Player p) {
		if(getConfig().getBoolean("inventario.limpar"))
			clearInv(p);
		if(getConfig().getBoolean("inventario.semFome"))
			p.setFoodLevel(20);
		if(version!=0)
			if(version==1)
				if(core2.getClanManager().getClanPlayer(p) != null)
					core2.getClanManager().getClanPlayer(p).setFriendlyFire(true);
			else if(version==2)
				if(core.getClanPlayerManager().getClanPlayer(p) != null)
					core.getClanPlayerManager().getClanPlayer(p).setFriendlyFire(true);
		totalParticipantes.put(p.getName(), 0);
		participantes.add(p.getName());
		p.teleport(spawn);
		p.sendMessage(" ");
		p.sendMessage("§8§l[Guerreiro] §eVocê entrou no evento guerreiro!");
		p.sendMessage("§8§l[Guerreiro] §cPara sair digite: §c§l/guerreiro sair");
		p.sendMessage("§8§l[Guerreiro] §ePrepare-se enquanto o evento está iniciando!");
		p.sendMessage(" ");
		if(getConfig().getBoolean("inventario.limpar"))
			clearInv(p);
		if(getConfig().getBoolean("inventario.darItens")) {
			Kit(p);
			p.updateInventory();
		}
		if(getConfig().getBoolean("inventario.limparPocoes"))
		    for(PotionEffect effect : p.getActivePotionEffects()) {
		    	p.removePotionEffect(effect.getType());
		    	p.sendMessage("§8§l[Guerreiro] §ePoção §6"+effect.getType().getName()+" §eremovida.");
		    }
		if(p.hasPermission("pdgh.admin"))
			return;
		if(p.hasPermission("pdgh.vip"))
			if(!vips.contains(p.getName().toLowerCase())) {
				getServer().broadcastMessage("§8§l[Guerreiro] §6§l"+p.getName()+" §eé VIP e entrou no evento Guerreiro.");
				vips.add(p.getName().toLowerCase());
			}
	}
	
	public void clearInv(Player p) {
		p.closeInventory();
		p.closeInventory();
		p.closeInventory();
		p.closeInventory();
		p.closeInventory();
		p.getInventory().setHelmet(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setBoots(null);
		p.getInventory().clear();
	}
	
	public void Kit(Player p) {
		if(p.hasPermission("pdgh.vip")) {
			ItemStack espada = new ItemStack(Material.DIAMOND_SWORD, 1);
			espada.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
			espada.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
			ItemStack arco = new ItemStack(Material.BOW, 1);
			arco.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE , 5);
			arco.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
			arco.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			arco.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			ItemStack elmo = new ItemStack(Material.DIAMOND_HELMET, 1);
			elmo.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			elmo.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			ItemStack peito = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
			peito.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			peito.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			ItemStack calca = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
			calca.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			calca.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			ItemStack bota = new ItemStack(Material.DIAMOND_BOOTS, 1);
			bota.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			bota.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			p.getInventory().addItem(espada);
			p.getInventory().addItem(arco);
			p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 30, (short) 1));
			p.getInventory().addItem(new ItemStack(Material.POTION, 20, (short) 8233));
			p.getInventory().addItem(new ItemStack(Material.POTION, 20, (short) 8226));
			p.getInventory().addItem(elmo);
			p.getInventory().addItem(peito);
			p.getInventory().addItem(calca);
			p.getInventory().addItem(bota);
			p.getInventory().setHelmet(elmo);
			p.getInventory().setChestplate(peito);
			p.getInventory().setLeggings(calca);
			p.getInventory().setBoots(bota);
			p.getInventory().addItem(new ItemStack(Material.ARROW, 1));
			
			if(!vips.contains(p.getName().toLowerCase())) {
				getServer().broadcastMessage("§8§l[Guerreiro] §6§l"+p.getName()+" §eé VIP e ganhou em toda sua armadura +2 leveis de encantamento, +1 armadura completa e o dobro de todas as poções.");
				vips.add(p.getName().toLowerCase());
			}
			
		}else{
		    ItemStack espada = new ItemStack(Material.DIAMOND_SWORD, 1);
		    espada.addEnchantment(Enchantment.DAMAGE_ALL, 5);
		    espada.addEnchantment(Enchantment.FIRE_ASPECT, 2);
		    ItemStack arco = new ItemStack(Material.BOW, 1);
		    arco.addEnchantment(Enchantment.ARROW_DAMAGE , 5);
		    arco.addEnchantment(Enchantment.ARROW_FIRE, 1);
		    arco.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		    arco.addEnchantment(Enchantment.DURABILITY, 3);
		    ItemStack elmo = new ItemStack(Material.DIAMOND_HELMET, 1);
		    elmo.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
		    elmo.addEnchantment(Enchantment.DURABILITY, 3);
		    ItemStack peito = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
		    peito.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
		    peito.addEnchantment(Enchantment.DURABILITY, 3);
		    ItemStack calca = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
		    calca.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
		    calca.addEnchantment(Enchantment.DURABILITY, 3);
		    ItemStack bota = new ItemStack(Material.DIAMOND_BOOTS, 1);
		    bota.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
		    bota.addEnchantment(Enchantment.DURABILITY, 3);
		    p.getInventory().addItem(espada);
		    p.getInventory().addItem(arco);
		    p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 30, (short) 1));
		    p.getInventory().addItem(new ItemStack(Material.POTION, 10, (short) 8233));
		    p.getInventory().addItem(new ItemStack(Material.POTION, 10, (short) 8226));
		    p.getInventory().addItem(new ItemStack(Material.ARROW, 1));
			p.getInventory().setHelmet(elmo);
			p.getInventory().setChestplate(peito);
			p.getInventory().setLeggings(calca);
			p.getInventory().setBoots(bota);
		}
	}
	
	protected void removePlayer(Player p,int motive) {//0=sair, 1=morrer, 2=quit, 3=kick
		if(!participantes.contains(p.getName()))
			return;
		if(version!=0)
			if(version==1)
				if(core2.getClanManager().getClanPlayer(p) != null)
					core2.getClanManager().getClanPlayer(p).setFriendlyFire(false);
			else if(version==2)
				if(core.getClanPlayerManager().getClanPlayer(p) != null)
					core.getClanPlayerManager().getClanPlayer(p).setFriendlyFire(false);
		participantes.remove(p.getName());
		if(guerreiroEtapa<2)
			totalParticipantes.remove(p.getName());
		else if(guerreiroEtapa==3) {
			if(participantes.size()>1) {
				if((noob)&&(!noobEfetuado)) {
					noobEfetuado=true;
					me.mchiappinam.pdghnoob.Main.setNoob(p.getName(), (me.mchiappinam.pdghnoob.Main) Bukkit.getPluginManager().getPlugin("PDGHNoob"));
					getServer().broadcastMessage("§8§l[Guerreiro] §d§l"+p.getName()+" §cfoi o primeiro a morrer e virou o novo §dⓃⓄⓄⒷ§c!");
				}
				
				getServer().broadcastMessage("§8§l[Guerreiro] §eRestam "+participantes.size()+" jogadores dentro do guerreiro!");
				checkGuerreiroEnd();
			}
			if((participantes.size()==getConfig().getInt("arena.jogadoresMenor"))&&(!jaTeleportado)&&(comecouComMaisDeDois)) {
				getServer().broadcastMessage(" ");
				getServer().broadcastMessage("§8§l[Guerreiro] §eApenas "+getConfig().getInt("arena.jogadoresMenor")+" jogadores participando!");
				getServer().broadcastMessage("§8§l[Guerreiro] §eOs participantes restantes irão ser teleportados para uma arena menor!");
				getServer().broadcastMessage(" ");
				teleportarNovaArena();
			}
			checkGuerreiroEnd();
		}
		if(guerreiroEtapa==1) {
			totalParticipantes.remove(p.getName());
			if(motive!=3) {
				p.teleport(saida);
				p.sendMessage("§8§l[Guerreiro] §eVocê saiu do evento guerreiro, para voltar: §c/guerreiro");
			}else{
				p.teleport(saida);
				p.sendMessage("§8§l[Guerreiro] §cVocê foi kickado do evento guerreiro");
			}
		}
		else {
			if(motive==0) {
				p.teleport(saida);
				p.sendMessage("§8§l[Guerreiro] §eVocê saiu do evento guerreiro");
			}else if(motive==1)
				p.sendMessage("§8§l[Guerreiro] §eVocê morreu no evento guerreiro");
			else if(motive==3) {
				p.teleport(saida);
				p.sendMessage("§8§l[Guerreiro] §cVocê foi kickado do evento guerreiro");
			}
		}
	}
	
	public void liberarPvPNovaArena() {
		tliberarPvPNovaArena = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
    		int timer = 10;
    		public void run() {
    			if(timer==0) {
    				pvpOffNovaArena=false;
    				for(String n : participantes)
    		  			getServer().getPlayer(n).playSound(getServer().getPlayer(n).getLocation(), Sound.ANVIL_LAND, 1.0F, (byte) 30);
    				sendMessageGuerreiro(" ");
    				sendMessageGuerreiro("§8§l[Guerreiro] §eVALENDO!");
    				sendMessageGuerreiro("§8§l[Guerreiro] §eVALENDO!");
    				sendMessageGuerreiro("§8§l[Guerreiro] §eVALENDO!");
    				sendMessageGuerreiro(" ");
    				cliberarPvPNovaArena();
    			}
    			timer--;
    		}
		}, 0, 20);
	}
	
	public void teleportarNovaArena() {
		pvpOffNovaArena=true;
		jaTeleportado=true;
		tteleportarNovaArena = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
    		int timer = 20;
    		public void run() {
    			if(timer==0) {
    				for(String n : participantes) {
    					getServer().getPlayer(n).sendMessage("§8§l[Guerreiro] §b§lVocê foi teleportado para uma arena menor!");
    					getServer().getPlayer(n).sendMessage("§8§l[Guerreiro] §b§lO PvP será ativado em 10 segundos.");
    		  			getServer().getPlayer(n).playSound(getServer().getPlayer(n).getLocation(), Sound.ANVIL_LAND, 1.0F, (byte) 30);
    					getServer().getPlayer(n).teleport(arenaMenor);
    				}
    				liberarPvPNovaArena();
    				cteleportarNovaArena();
    			}
    			if(timer==20) {
    				sendMessageGuerreiro("§8§l[Guerreiro] §b§lVocê tem 20 segundos para recolher os itens do evento Guerreiro!");
    				for(String n : participantes)
    		  			getServer().getPlayer(n).playSound(getServer().getPlayer(n).getLocation(), Sound.EXPLODE, 1.0F, (byte) 30);
    			}else if((timer==15)||(timer==10)) {
    				sendMessageGuerreiro("§8§l[Guerreiro] §b§lVocê tem "+timer+" segundos para recolher os itens do evento Guerreiro!");
    			}else if((timer<=5)&&(timer>1))
    				sendMessageGuerreiro("§8§l[Guerreiro] §b§lVocê tem "+timer+" segundos para recolher os itens do evento Guerreiro!");
    			else if(timer==1)
    				sendMessageGuerreiro("§8§l[Guerreiro] §b§lVocê tem "+timer+" segundo para recolher os itens do evento Guerreiro!");
    			timer--;
    		}
		}, 0, 20);
	}

	public void cteleportarNovaArena() {
		getServer().getScheduler().cancelTask(tteleportarNovaArena);
	}

	public void cliberarPvPNovaArena() {
		getServer().getScheduler().cancelTask(tliberarPvPNovaArena);
	}
	
	protected void sendMessageGuerreiro(String msg) {
		for(String n : participantes)
			getServer().getPlayer(n).sendMessage(msg);
	}
}
