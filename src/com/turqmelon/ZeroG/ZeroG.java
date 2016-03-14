package com.turqmelon.ZeroG;


import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZeroG extends JavaPlugin implements Listener {

    private Map<UUID, Vector> vectors = new HashMap<>();
    private Map<UUID, Location> locations = new HashMap<>();
    private Map<UUID, Boolean> onGround = new HashMap<>();
    private Map<UUID, GravLock> gravLockMap = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getScheduler().runTaskTimer(this, this::doVectorShit, 1L, 1L);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDmg(EntityDamageEvent event){
        if (event.isCancelled())return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && getGravity(event.getEntity()) != 1){
            event.setCancelled(true);
        }
        else if ((event instanceof EntityDamageByEntityEvent)){

            // When you attack somebody, it passes on your gravity to them
            // Epic cliff battles anybody?
            Entity entity = event.getEntity();
            Entity damager =  ((EntityDamageByEntityEvent) event).getDamager();
            double grav = getGravity(damager);
            if (grav != 1){
                gravLockMap.put(entity.getUniqueId(), new GravLock(grav, (long) (System.currentTimeMillis()+(750*event.getDamage()))));
            }

        }
    }

    public double getGravity(Entity entity){

        if (gravLockMap.containsKey(entity.getUniqueId())){
            GravLock lock = gravLockMap.get(entity.getUniqueId());
            if (System.currentTimeMillis() < lock.getExpiration()){
                return lock.getGravity();
            }
            else{
                gravLockMap.remove(entity.getUniqueId());
            }
        }

        Player player = null;
        if ((entity instanceof Player)){
            player = (Player)entity;
        }
        else if ((entity instanceof Projectile)){
            Projectile projectile = (Projectile)entity;
            if ((projectile.getShooter() instanceof Player)){
                player = (Player)projectile.getShooter();
            }
        }
        if (player != null){
            double grav = 1;
            ItemStack feet = player.getInventory().getBoots();
            if (feet != null && feet.getType() != Material.AIR){
                switch(feet.getType()){
                    case DIAMOND_BOOTS:
                        grav = 0.2;
                        break;
                    case IRON_BOOTS:
                        grav = 0.4;
                        break;
                    case GOLD_BOOTS:
                    case CHAINMAIL_BOOTS:
                        grav = 0.6;
                        break;
                    case LEATHER_BOOTS:
                        grav = 0.8;
                        break;
                }
            }
            return player.isSneaking() ? (grav*2) : grav;
        }
        return 1;
    }

    public void doVectorShit() {

        for(World world : Bukkit.getWorlds()) {

            for(Entity entity : world.getEntities()){

                double gravity = getGravity(entity);
                if (gravity == 1) continue;

                if ((entity instanceof Player)){
                    Player player = (Player)entity;
                    if (player.getAllowFlight() || (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)) continue;
                }

                Vector vector = entity.getVelocity();

                UUID uuid = entity.getUniqueId();
                if ((vectors.containsKey(uuid)) && (this.onGround.containsKey(uuid)) && (!entity.isOnGround()) && (!entity.isInsideVehicle()))
                {
                    Vector oldv = vectors.get(uuid);
                    if (!this.onGround.get(uuid))
                    {
                        Vector d = oldv.clone();
                        d.subtract(vector);
                        double dy = d.getY();
                        if ((dy > 0.0D) && ((vector.getY() < -0.01D) || (vector.getY() > 0.01D)))
                        {
                            vector.setY(oldv.getY() - dy * gravity);
                            boolean newxchanged = (vector.getX() < -0.001D) || (vector.getX() > 0.001D);
                            boolean oldxchanged = (oldv.getX() < -0.001D) || (oldv.getX() > 0.001D);
                            if ((newxchanged) && (oldxchanged)) {
                                vector.setX(oldv.getX());
                            }
                            boolean newzchanged = (vector.getZ() < -0.001D) || (vector.getZ() > 0.001D);
                            boolean oldzchanged = (oldv.getZ() < -0.001D) || (oldv.getZ() > 0.001D);
                            if ((newzchanged) && (oldzchanged)) {
                                vector.setZ(oldv.getZ());
                            }
                            entity.setVelocity(vector.clone());
                        }
                    }
                    else if (((entity instanceof Player)) &&
                            (locations.containsKey(uuid)))
                    {
                        Vector pos = entity.getLocation().toVector();
                        Vector oldpos = locations.get(uuid).toVector();
                        Vector velocity = pos.subtract(oldpos);
                        vector.setX(velocity.getX());
                        vector.setZ(velocity.getZ());
                    }
                    entity.setVelocity(vector.clone());
                }
                vectors.put(uuid, vector.clone());
                onGround.put(uuid, entity.isOnGround());
                locations.put(uuid, entity.getLocation());

            }
        }

    }

}
