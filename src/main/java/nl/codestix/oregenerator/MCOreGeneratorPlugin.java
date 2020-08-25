package nl.codestix.oregenerator;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MCOreGeneratorPlugin extends JavaPlugin implements Listener {

    public ArrayList<BlockGenerator> gens = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("cobble").setExecutor(new CobbleCommand(this));

        for(Map.Entry<String, Object> rootEntry : getConfig().getValues(false).entrySet()) {
            List<String> keySplit = Arrays.asList(rootEntry.getKey().split("&"));
            if (keySplit.size() != 2)
            {
                getLogger().warning("A generator should be configured using the LAVA&WATER format. Not: " + String.join("&", keySplit));
                continue;
            }

            try {
                Material mat1 = Material.valueOf(keySplit.get(0).toUpperCase());
                Material mat2 = Material.valueOf(keySplit.get(1).toUpperCase());
                BlockGenerator gen = new BlockGenerator(mat1, mat2);
                gens.add(gen);

                ConfigurationSection section = getConfig().getConfigurationSection(rootEntry.getKey());
                for(Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                    Material mat = Material.valueOf(entry.getKey().toUpperCase());
                    int chance = (int)entry.getValue();
                    gen.chances.put(mat, chance);
                }
            }
            catch(Exception ex) {
                getLogger().warning("Could not read config value: " + ex);
            }
        }
    }

    public BlockGenerator getGenerator(Material mat1, Material mat2) {
        for(BlockGenerator gen : gens) {
            if ((gen.mat1 == mat1 && gen.mat2 == mat2) || (gen.mat2 == mat1 && gen.mat1 == mat2)) {
                return gen;
            }
        }
        return null;
    }

    @Override
    public void onDisable() {
        for(BlockGenerator gen : gens) {
            ConfigurationSection section = getConfig().getConfigurationSection(gen.getConfigSectionName());
            if (section == null)
                section = getConfig().createSection(gen.getConfigSectionName());
            for(Map.Entry<Material,Integer> chance : gen.chances.entrySet()) {
                section.set(chance.getKey().name(), chance.getValue());
            }
        }

        saveConfig();
    }

    private final BlockFace[] ALL_FACES = new BlockFace[]{
            BlockFace.SELF,
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    @EventHandler
    public void handleGenerator(BlockFromToEvent event) {

        Block b = event.getToBlock();
        if (b.getType() != Material.AIR)
            return;

        Material type = event.getBlock().getType();
        if (type != Material.WATER && type != Material.LAVA)
            return;

        BlockGenerator genToUse = null;
        for (BlockFace face : ALL_FACES){
            Block r = b.getRelative(face, 1);
            for(BlockGenerator gen :gens) {
                if ((gen.mat1 == type && gen.mat2 == r.getType())
                || (gen.mat2 == type && gen.mat1 == r.getType()))
                {
                    genToUse = gen;
                    break;
                }
            }
        }
        if (genToUse == null)
            return;

        event.setCancelled(true);
        event.getToBlock().setType(genToUse.getRandomOre());
        Location loc = event.getToBlock().getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, SoundCategory.AMBIENT, 1.0f, 0.9f);
//        loc.getWorld().playEffect(loc, Effect.SMOKE, 1);
    }
}
