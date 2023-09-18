package deathofthesungod.deathofthesungod;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.logging.Level;


public final class DeathOfTheSunGod extends JavaPlugin {
    public GameStates currentState;

    int current_time = 0;
    int setting_aboveGroundTime = 5;
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().log(Level.INFO, "Beware the Sun God has arrived." + getDescription().getVersion());
        currentState = GameStates.IDLE;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().log(Level.INFO, "The sun has gone down." + getDescription().getVersion());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //############# StartGameCommand #############
        if(label.equalsIgnoreCase("dotsg"))
            if (args.length == 1 && args[0].equalsIgnoreCase("start"))
                if(currentState == GameStates.IDLE){
                    Bukkit.broadcastMessage("§2NEW ROUND STARTING NOW!");
                    startRound((Player)sender); // Runs Round start procedure
                    return true;
                } else {
                    sender.sendMessage("Game is already running!");
                    sender.sendMessage("You can stop the current Game with /sg stop");
                    return true;
                }
        //############# StopGameCommand #############
        if(label.equalsIgnoreCase("dotsg"))
            if (args.length == 1 && args[0].equalsIgnoreCase("stop"))
                if(currentState == GameStates.IDLE){
                    sender.sendMessage("No game is currently active!");
                    sender.sendMessage("You can start the game with /sg start");
                    return true;
                } else {
                    Bukkit.broadcastMessage("§4ROUND STOPPED!");
                    stopRound(); // Runs Round start procedure
                    return true;
                }
        //############# Resume Game #############
        if(label.equalsIgnoreCase("dotsg"))
            if (args.length == 1 && args[0].equalsIgnoreCase("resume")) {
                currentState = GameStates.AFTER_SUN;
                getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
                return true;
            }

        return false;
    }

    public void TeleportAllplayersTo(Player p){
        Random random = new Random();
        World w = p.getWorld();
        int x = random.nextInt(20000);
        int z = random.nextInt(20000);
        Location loc = new Location(w,x,w.getHighestBlockYAt(x,z)+1,z);
        p.teleport(loc);
        for(Player pp : getServer().getOnlinePlayers())
            pp.teleport(p);
    }

    public void startRound(Player roundStarter){
        for(final Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20);
            player.getWorld().setTime(0);
            player.setBedSpawnLocation(player.getLocation());
        }
        TeleportAllplayersTo(roundStarter);
        currentState = GameStates.INIT_COUNTDOWN;
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getScheduler().runTaskTimer(this, new Runnable()
        {
            int time = 10; //or any other number you want to start countdown from
            @Override
            public void run()
            {
                if (this.time == 0)
                    return;
                for (final Player player : Bukkit.getOnlinePlayers())
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Game Starts in §2" + time + "§r Seconds!"));
                this.time--;
            }
        }, 0L, 20L);

        Bukkit.getScheduler().runTaskLater(this, new Runnable()
        {
            @Override
            public void run()
            {
                currentState = GameStates.BEFORE_SUN;
            }
        }, 200L);

        Bukkit.getScheduler().runTaskTimer(this, new Runnable()
        {
            int time = 120; //or any other number you want to start countdown from

            @Override
            public void run()
            {
                if (this.time == 0)
                    return;
                for (final Player player : Bukkit.getOnlinePlayers())
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Game Starts in §2" + time + "§r Seconds!"));
                this.time--;
            }
        }, 200L, 20L);

        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                currentState = GameStates.AFTER_SUN;
            }
        }, 2800);
        //RTP to random coords
        //countdown
        //countdown done => start timer
        //timer over => sunlight deadly

        //if 0 player in list the game over => loose
        //if enderdragon dead => WIN

    }

    public void stopRound(){
        currentState = GameStates.IDLE;
    }


    class PlayerMoveListener implements Listener {

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            if(currentState == GameStates.INIT_COUNTDOWN){
                event.getPlayer().teleport(event.getFrom());
            }

            if(currentState == GameStates.AFTER_SUN){
                if (event.getTo().getBlock().getLightFromSky() < 8) {
                    return;
                }

                if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                    return;
                }

                if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                    return;
                }

                if (event.getTo().getBlock().getLightFromSky() >= 8 && event.getTo().getBlock().getLightFromSky() < 13) {
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20, 300));
                }
                if (event.getTo().getBlock().getLightFromSky() >= 13) {
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HARM, 100, 10));
                }
                TextComponent message = new TextComponent("The sun god has forced death upon you!");
                message.setColor(ChatColor.DARK_RED);
                event.getPlayer().spigot().sendMessage(message);
            }

        }
    }     
}

