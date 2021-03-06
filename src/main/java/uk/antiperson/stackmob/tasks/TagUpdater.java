package uk.antiperson.stackmob.tasks;

import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.scheduler.BukkitRunnable;
import uk.antiperson.stackmob.Configuration;
import uk.antiperson.stackmob.api.events.TagUpdateEvent;
import uk.antiperson.stackmob.plugins.MythicMobs;
import uk.antiperson.stackmob.StackMob;


/**
 * Created by nathat on 21/10/16.
 */
public class TagUpdater extends BukkitRunnable {


    private StackMob sm;
    private Configuration config;
    private MythicMobs mm;
    public TagUpdater(StackMob sm){
        this.sm = sm;
        config = sm.config;
        if(Bukkit.getServer().getPluginManager().getPlugin("MythicMobs") != null) {
            mm = new MythicMobs(sm);
        }
    }

    @Override
    public void run(){
        int removeTagAmount = config.getFilecon().getInt("creature.tag.removeat.amount");
        boolean removeTagAt = config.getFilecon().getBoolean("creature.tag.removeat.enabled");
        boolean nameVisible = config.getFilecon().getBoolean("creature.tag.visiblenothovered");
        boolean removeTag = config.getFilecon().getBoolean("creature.tamed.removetag");
        for(World world : Bukkit.getWorlds()){
            for(LivingEntity e: world.getLivingEntities()){
                if(sm.amountMap.get(e.getUniqueId()) != null){
                    String customNameFormat = config.getFilecon().getString("creature.tag.text");
                    boolean stackBack = config.getFilecon().getBoolean("creature.stackbackwards");
                    if(removeTagAt){
                        if(sm.amountMap.get(e.getUniqueId()) <= removeTagAmount){
                            if(e.getCustomName() != null){
                                e.setCustomName(null);
                                e.setCustomNameVisible(false);
                            }
                            continue;
                        }
                    }
                    if (e instanceof Tameable && removeTag) {
                        if (((Tameable) e).isTamed()) {
                            continue;
                        }
                    }
                    String nameTag;
                    String entype;
                    String amount = sm.amountMap.get(e.getUniqueId()).toString();
                    if(config.getFilecon().getString("creature.special." + e.getType().toString().toLowerCase() + ".displaytag") != null){
                        customNameFormat = config.getFilecon().getString("creature.special." + e.getType().toString().toLowerCase() + ".displaytag");
                    }
                    if(config.getFilecon().getBoolean("creature.special." + e.getType().toString().toLowerCase() + ".stackbackwards")){
                        stackBack = true;
                    }
                    if(config.getFilecon().getBoolean("creature.tag.betterentitynames")){
                        entype = toTitleCase(sm.et.getTranslationName(e).replace("_", " ").toLowerCase());
                    }else{
                        entype = sm.et.getTranslationName(e);
                    }
                    if(stackBack && Integer.valueOf(amount) > 0){
                        amount = "-" + amount;
                    }
                    if((Bukkit.getServer().getPluginManager().getPlugin("MythicMobs") != null) && (config.getFilecon().getBoolean("mythicmobs.enabled")) && (mm.getMythicMobs().isActiveMob(e.getUniqueId()))) {
                        ActiveMob ma = mm.getMythicMobs().getMythicMobInstance(e);
                        nameTag = customNameFormat.replace("%entity%", ma.getType().toString()).replace("%amount%", amount);
                    }else{
                        nameTag = customNameFormat.replace("%entity%", entype).replace("%amount%", amount);
                    }
                    String nameTag2 = ChatColor.translateAlternateColorCodes('&', nameTag);
                    TagUpdateEvent tue = new TagUpdateEvent(sm.getAPI().getEntityManager().getStackedEntity(e), nameTag2, customNameFormat, nameVisible);
                    Bukkit.getPluginManager().callEvent(tue);
                    if(tue.isCancelled()){
                        continue;
                    }
                    nameTag2 = tue.getUpdatedTag();
                    nameVisible = tue.isVisibleOnHover();
                    e.setCustomName(nameTag2);
                    e.setCustomNameVisible(nameVisible);
                }
            }
        }
    }

    public String toTitleCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}


