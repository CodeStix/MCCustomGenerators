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
                    else if ((mat1 == Material.LAVA && mat2 == Material.BLUE_ICE) || (mat2 == Material.LAVA && mat1 == Material.BLUE_ICE)) {
                        defaultEntries = BlockGenerator.DEFAULT_LAVA_BLUE_ICE_CHANCES;
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
        else if (strings.length >= 1 && strings[0].equalsIgnoreCase("particle")) {
            if (strings.length == 1) {
                Particle[] ps =  Particle.values();
                commandSender.sendMessage("§dList of available particles:");
                for(Particle p : ps) {
                    commandSender.sendMessage(p.name());
                }
                return true;
            }
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
            }
        }
        else if (strings.length == 1 && strings[0].equalsIgnoreCase("list")) {
            commandSender.sendMessage(String.format("§dList of %d generators:", plugin.gens.size()));
            for(BlockGenerator g : plugin.gens) {
                commandSender.sendMessage(g.toString());
            }
        }
        else if (strings.length == 1 && strings[0].equalsIgnoreCase("info")) {
            if (gen == null) {
                commandSender.sendMessage("§cSelect a generator first.");
                return true;
            }
            int sum = gen.getChancesSum();
            commandSender.sendMessage(String.format("§dGenerator %s: (%d chances sum)", gen.toString(), sum));
            for(Map.Entry<Material,Integer> entry : gen.chances.entrySet()) {
                commandSender.sendMessage(String.format("%s = %d / %d = %.4f", entry.getKey().name().toLowerCase(), entry.getValue(), sum, (float)entry.getValue() / sum));
            }
        }
        else if ((strings.length == 1 && strings[0].equalsIgnoreCase("help")) || strings.length == 0) {
            commandSender.sendMessage("§d§lCustom Generators Help");
            commandSender.sendMessage("§6/cobble ...");
            commandSender.sendMessage("   §6select <block1> <block2>§7: Select (or create) the generator that is activated by <block1> and <block2>. Either <block1> or <block2> must be a liquid.");
            commandSender.sendMessage("   §6list§7: Show all the created generators.");
            commandSender.sendMessage("   §6deselect§7: Deselect the selected generator.");
            commandSender.sendMessage("   §6remove§7: Remove the selected generator.");
            commandSender.sendMessage("   §6info§7: Show information about the selected generator.");
            commandSender.sendMessage("   §6set <block> <chance>§7: Set the <chance> for the selected generator to generate <block>. The chance is calculated by (block chance / sum of all block chances).");
            commandSender.sendMessage("   §6unset <block>§7: Do not generate <block> in the selected generator.");
            commandSender.sendMessage("   §6particle [name] [count] [speed]§7: Set the particle that will spawn when a block is generated in the selected generator. Or show a list of particles if no particle name is given.");
        }
        else {
            commandSender.sendMessage("§cInvalid subcommand! Use '/cobble help' to show available subcommands.");

            /*commandSender.sendMessage("§6§lHints");
            if (gen == null) {
                if (plugin.gens.size() > 0) {
                    commandSender.sendMessage("§7Select a generator with /cobble select <block1> <block2>");
                }
                else {
                    commandSender.sendMessage("§7Create a generator using /cobble select <block1> <block2>");
                    commandSender.sendMessage("To customize the default cobblestone generator, use /cobble select lava water");
                    commandSender.sendMessage("§7Show help using /cobble help");
                }
            }
            else {
                commandSender.sendMessage("§7Show info using /cobble info");
                commandSender.sendMessage("Set a block chance using /cobble set <block> <chance>");
                commandSender.sendMessage("§7Remove a generating block using /cobble unset <block>");
                commandSender.sendMessage("Set a particle using /cobble particle <name> [count] [speed]");
                commandSender.sendMessage("§7Deselect this generator using /cobble deselect");
                commandSender.sendMessage("Remove this generator using /cobble remove");
            }*/
        }
        return true;
    }
}
