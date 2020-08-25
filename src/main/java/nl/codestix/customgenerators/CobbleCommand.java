package nl.codestix.customgenerators;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class CobbleCommand implements CommandExecutor {

    public MCCustomGeneratorsPlugin plugin;
    private HashMap<String, BlockGenerator> selectedGenerators = new HashMap<>();

    public CobbleCommand(MCCustomGeneratorsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        BlockGenerator gen = selectedGenerators.get(commandSender.getName());

        if (strings.length == 3 && strings[0].equalsIgnoreCase("select")) {
            try {
                Material mat1 = Material.valueOf(strings[1].toUpperCase());
                Material mat2 = Material.valueOf(strings[2].toUpperCase());
                gen = plugin.getGenerator(mat1, mat2);
                if (gen == null) {
                    gen = new BlockGenerator(mat1, mat2);
                    plugin.gens.add(gen);

                    ConfigurationSection section = plugin.generatorsConfig.getConfigurationSection(gen.getConfigSectionName());
                    if (section == null)
                    {
                        section = plugin.generatorsConfig.createSection(gen.getConfigSectionName());
                        plugin.saveGeneratorsConfig();
                    }

                    Map<Material,Integer> defaultEntries;
                    if ((mat1 == Material.LAVA && mat2 == Material.WATER) || (mat2 == Material.LAVA && mat1 == Material.WATER)) {
                        defaultEntries = BlockGenerator.DEFAULT_LAVA_WATER_CHANCES;
                    }
                    else {
                        defaultEntries = BlockGenerator.DEFAULT_OTHER_CHANCES;
                    }
                    for(Map.Entry<Material,Integer> entry : defaultEntries.entrySet()) {
                        section.set(entry.getKey().name(), entry.getValue());
                        gen.chances.put(entry.getKey(), entry.getValue());
                    }

                    commandSender.sendMessage("§dCreated and selected new generator " + gen.toString());
                }
                else {
                    commandSender.sendMessage("§dSelecting generator " + gen.toString());
                }
                selectedGenerators.put(commandSender.getName(), gen);
            }
            catch (Exception ex) {
                commandSender.sendMessage("§cCould not select generator: " + ex);
            }
        }
        else if (strings.length == 1 && strings[0].equalsIgnoreCase("deselect")) {
            selectedGenerators.remove(commandSender.getName());
            commandSender.sendMessage("§dDone, no generator selected.");
        }
        else if (strings.length == 1 && strings[0].equalsIgnoreCase("remove")) {
            if (gen == null) {
                commandSender.sendMessage("§cSelect a generator first.");
                return true;
            }
            commandSender.sendMessage("§dRemoving generator " + gen.toString());
            plugin.gens.remove(gen);
            plugin.generatorsConfig.set(gen.getConfigSectionName(), null);
            plugin.saveGeneratorsConfig();

            selectedGenerators.remove(commandSender.getName());
        }
        else if (strings.length == 2 && strings[0].equalsIgnoreCase("unset")) {
            if (gen == null) {
                commandSender.sendMessage("§cSelect a generator first.");
                return true;
            }
            try {
                Material mat = Material.valueOf(strings[1].toUpperCase());
                commandSender.sendMessage("§dRemoving " + mat);
                gen.chances.remove(mat);
                plugin.generatorsConfig.getConfigurationSection(gen.getConfigSectionName()).set(mat.name(), null);
                plugin.saveGeneratorsConfig();
            }
            catch(Exception ex) {
                commandSender.sendMessage("§cCould not remove chance: " + ex);
            }
        }
        else if (strings.length == 3 && strings[0].equalsIgnoreCase("set")) {
            if (gen == null) {
                commandSender.sendMessage("§cSelect a generator first.");
                return true;
            }
            try {
                Material mat = Material.valueOf(strings[1].toUpperCase());
                int i = Integer.parseInt(strings[2]);
                commandSender.sendMessage(String.format("§dSetting chance for %s to %d", mat.name().toLowerCase(), i));
                gen.chances.put(mat, i);
                plugin.generatorsConfig.getConfigurationSection(gen.getConfigSectionName()).set(mat.name(), i);
                plugin.saveGeneratorsConfig();
            }
            catch(Exception ex) {
                commandSender.sendMessage("§cCould not set chance: " + ex);
            }
        }
        else if (strings.length >= 2 && strings[0].equalsIgnoreCase("particle")) {
            if (gen == null) {
                commandSender.sendMessage("§cSelect a generator first.");
                return true;
            }
            try {
                gen.particle = Particle.valueOf(strings[1].toUpperCase());
                if (strings.length >= 3)
                    gen.particleCount = Integer.parseInt(strings[2]);
                if (strings.length >= 4)
                    gen.particleSpeed = Double.parseDouble(strings[3]);
                commandSender.sendMessage(String.format("§dGeneration particle for %s is set to %s (count = %d, speed = %f)", gen.toString(), gen.particle.name().toLowerCase(), gen.particleCount, gen.particleSpeed));
            }
            catch(Exception ex) {
                commandSender.sendMessage("§cCould not set particle: " + ex);
                StringBuilder builder = new StringBuilder();
                Particle[] particles = Particle.values();
                for(int i = 0; i < particles.length; i++) {
                    if (i != 0)
                        builder.append(", ");
                    builder.append(particles[i].name());
                }
                commandSender.sendMessage("§8List of available particles: " + builder);
            }
        }
        else if ((strings.length == 1 && strings[0].equalsIgnoreCase("list"))
            || (strings.length == 0 && gen == null)) {
            if (plugin.gens.size() > 0) {
                commandSender.sendMessage(String.format("§dList of %d generators:", plugin.gens.size()));
                for(BlockGenerator g : plugin.gens) {
                    commandSender.sendMessage(g.toString());
                }
                commandSender.sendMessage("§8Select a generator with /cobble select <block1> <block2>");
            }
            else {
                commandSender.sendMessage("§dYou don't have any custom generators.");
                commandSender.sendMessage("§8Create/select a generator using /cobble select <block1> <block2>");
                commandSender.sendMessage("§8Then, use /cobble to display more information on what you can do.");
            }
        }
        else if (strings.length == 0) {
            int sum = gen.getChancesSum();
            commandSender.sendMessage(String.format("§dGenerator %s: (%d chances sum)", gen.toString(), sum));
            for(Map.Entry<Material,Integer> entry : gen.chances.entrySet()) {
                commandSender.sendMessage(String.format("%s = %d / %d = %.4f", entry.getKey().name().toLowerCase(), entry.getValue(), sum, (float)entry.getValue() / sum));
            }
            commandSender.sendMessage("§8Set a block chance using /cobble set <block> <chance>");
            commandSender.sendMessage("§8Remove a generating block using /cobble unset <block>");
            commandSender.sendMessage("§8Set a particle using /cobble particle <name> [count] [speed]");
            commandSender.sendMessage("§8Deselect this generator using /cobble deselect");
            commandSender.sendMessage("§8Remove this generator using /cobble remove");
        }
        else {
            commandSender.sendMessage("§cUnknown subcommand. Usage: " + command.getUsage());
        }

        return true;
    }
}
