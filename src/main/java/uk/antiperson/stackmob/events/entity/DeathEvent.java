package uk.antiperson.stackmob.events.entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import uk.antiperson.stackmob.Configuration;
import uk.antiperson.stackmob.utils.EntityUtils;
import uk.antiperson.stackmob.StackMob;

import java.util.List;
import java.util.Random;

/**
 * Created by nathat on 02/10/16.
 */
public class DeathEvent implements Listener {

    private Configuration config;
    private Random rand = new Random();
    private StackMob st;
    public DeathEvent(StackMob st){
        config = st.config;
        this.st = st;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent e){
        boolean killAll = config.getFilecon().getBoolean("creature.kill-all.enabled");
        if(st.amountMap.containsKey(e.getEntity().getUniqueId())){
            if(st.amountMap.get(e.getEntity().getUniqueId()) > 1){
                Entity ea = e.getEntity();
                if(killAll){
                    if(config.getFilecon().getBoolean("creature.kill-all.drops.multiply")){
                        multiplyDrops(e.getDrops(), ea, e.getEntity().getKiller(),  st.amountMap.get(ea.getUniqueId()));
                        st.getLogger().info(e.getDrops().size() + ", " + (ea != null) + ", " + (e.getEntity().getKiller() != null) + " ," + st.amountMap.get(ea.getUniqueId()));
                    }
                    e.setDroppedExp((int) generateXpRandom(st.amountMap.get(ea.getUniqueId()), e.getDroppedExp()));
                    if(ea instanceof Slime){
                        if(((Slime)ea).getSize() <= 1){
                            st.amountMap.remove(ea.getUniqueId());
                        }
                    }else{
                        st.amountMap.remove(ea.getUniqueId());
                    }
                }else if(config.getFilecon().getBoolean("creature.kill-step.enabled")) {
                    int diff = config.getFilecon().getInt("creature.kill-step.max") - config.getFilecon().getInt("creature.kill-step.min");
                    int n = config.getFilecon().getInt("creature.kill-step-min") + new Random().nextInt(diff) + 1;
                    if(st.amountMap.get(ea.getUniqueId()) <= n){
                        multiplyDrops(e.getDrops(), e.getEntity(), e.getEntity().getKiller(), st.amountMap.get(ea.getUniqueId()));
                        st.getLogger().info(e.getDrops().size() + ", " + (ea != null) + ", " + e.getEntity().getKiller().getName() + " ," + st.amountMap.get(ea.getUniqueId()));
                    }else{
                        multiplyDrops(e.getDrops(), e.getEntity(), e.getEntity().getKiller(), n);
                        st.getLogger().info(e.getDrops().size() + ", " + (ea != null) + ", " + e.getEntity().getKiller().getName() + " ," + st.amountMap.get(ea.getUniqueId()));
                        int before = st.amountMap.get(e.getEntity().getUniqueId());
                        EntityUtils eu = new EntityUtils(st);
                        Entity es = eu.createEntity(ea, true, true);
                        st.amountMap.put(es.getUniqueId(), before - n);
                    }


                }else{
                    EntityUtils eu = new EntityUtils(st);
                    eu.createEntity(ea, true, true);
                }
            }else{
                st.amountMap.remove(e.getEntity().getUniqueId());
            }
        }
    }


    public void multiplyDrops(List<ItemStack> drops, Entity ea, Player killer, int mobAmount){
        for(ItemStack is : drops){
            if(config.getFilecon().getBoolean("creature.kill-all.drops.blacklist-enabled")){
                for(String s : config.getFilecon().getConfigurationSection("creature.kill-all.drops.blacklist").getKeys(false)){

                }
            }
            if(config.getFilecon().getBoolean("creature.kill-all.drops.ignore-armor")){
                if(is.getType().toString().contains("SWORD") || is.getType().toString().contains("SHOVEL") || is.getType().toString().contains("AXE") ||
                        is.getType().toString().contains("CHESTPLATE") || is.getType().toString().contains("LEGGINGS") || is.getType().toString().contains("BOOTS") ||
                        is.getType().toString().contains("HELMET")){
                    continue;
                }
            }
            if(config.getFilecon().getBoolean("creature.kill-all.drops.chances-enabled")) {
                if(config.getFilecon().getConfigurationSection("creature.kill-all.drops.chance").getKeys(false).contains(is.getType().toString())) {
                    for (int i = 1; i <= mobAmount; i++) {
                        double d = Math.random();
                        if (d < config.getFilecon().getDouble("creature.kill-all.drops.chance")) {
                            is.setAmount(0);
                            dropCorrectAmount(rand.nextInt(1) + 1, is, ea.getLocation(), killer);
                        }
                    }
                }else{
                    int a = (int) generateRandom(mobAmount);
                    is.setAmount(0);
                    dropCorrectAmount(a, is, ea.getLocation(), killer);
                }
            }else{
                int a = (int) generateRandom(mobAmount);
                is.setAmount(0);
                dropCorrectAmount(a, is, ea.getLocation(), killer);
            }
        }
    }

    public float generateRandom(int mobAmount){
        double a =  1 + rand.nextInt(1) + rand.nextDouble();
        double b = a * mobAmount;
        return Math.round(b);
    }

    public float generateXpRandom(int amount, int a){
        double aa = a + rand.nextDouble();
        return Math.round(aa * amount);
    }

    private void dropCorrectAmount(int randomAmount, ItemStack is, Location loc, Player killer){
        if(randomAmount < 64){
            ItemStack nis;
            if(killer != null){
                nis = new ItemStack(is.getType(), (int) Math.round(randomAmount * ((rand.nextInt(killer.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) + 1) + 2) / 1.75)));
            }else{
                nis = new ItemStack(is.getType(), randomAmount);
            }
            nis.setItemMeta(is.getItemMeta());
            nis.setData(is.getData());
            nis.setDurability(is.getDurability());
            loc.getWorld().dropItemNaturally(loc, nis);
        }else{
            double aa;
            if(killer != null){
                aa =  Math.round(randomAmount * ((rand.nextInt(killer.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) + 1) + 2) / 1.75)) / 64;
            }else{
                aa = randomAmount / 64;
            }
            double whole = Math.floor(aa);
            double leftOvers = randomAmount - (whole * 64);
            for(int i = 1; i <= whole; i++){
                ItemStack nis = new ItemStack(is.getType(), 64);
                nis.setItemMeta(is.getItemMeta());
                nis.setData(is.getData());
                nis.setDurability(is.getDurability());
                loc.getWorld().dropItemNaturally(loc, nis);
            }
            if(leftOvers != 0){
                ItemStack nis = new ItemStack(is.getType(), (int) Math.round(leftOvers));
                nis.setItemMeta(is.getItemMeta());
                nis.setData(is.getData());
                nis.setDurability(is.getDurability());
                loc.getWorld().dropItemNaturally(loc, nis);
            }
        }
    }

}