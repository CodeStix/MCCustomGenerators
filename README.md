# MCCustomGenerators
A bukkit plugin to create any variation of the default cobblestone generator!

## Installation
1. Download the plugin [here](https://github.com/CodeStix/MCCustomGenerators/releases). (pick the right one for your server version)
2. Place the downloaded jar file in your server's `plugins` folder.
3. Reload/restart your server

## Usage
`/cobble [help | list | select <block1> <block2> | deselect | info | remove | set <block> <chance> | unset <block> | particle <name> [count] [speed]]`
- `list`: Show all the created generators.
- `select <block1> <block2>`: Select (or create) the generator that is activated by `<block1>` and `<block2>`. Either `<block1>` or `<block2>` must be a liquid. For example: `/cobble select lava water` to select the default cobblestone generator. 
- `set <block> <chance>`: Set the `<chance>` for the selected generator to generate `<block>`. The chance is calculated by (chance / sum of all block chances). Chance can be any number. Higher values mean higher chance.
- `unset <block>`: Do not generate `<block>` in the selected generator.
- `deselect`: Deselect the selected generator.
- `remove`: Remove the selected generator.
- `info`: Show information about the selected generator.
- `particle [name] [count] [speed]`: Set the particle that will spawn when a block is generated in the selected generator. Or show a list of available particles when no particle name is given.

### Example 1
To customize the default cobblestone generator to also spawn ores, use the following command:

- `/cobble select lava water`: will create/select a custom generator and select it. Ores will be added automatically because customizing the default cobblestone generator is common.

__Done!__ Create a cobblestone generator and you will see it working.

- `/cobble info`: view the chances for each ore to appear.
- `/cobble unset diamond_ore`: do not generate diamond_ore.

### Example 2
To create a generator that will generate sand and gravel when water hits sandstone, use the following commands:

- `/cobble select water sandstone`: will create/select a custom generator that activates when water hits sandstone. (order of water and sandstone does not matter)
- `/cobble unset stone`: remove the default stone block, we don't want it to generate. (was automatically added when we created the generator in the first step)
- `/cobble set gravel 10`: set the chance for gravel to generate to __10__. Gravel has a __100%__ chance of spawning right now.
- `/cobble set sand 20`: set the chance for sand to generate to __30__.  Sand has a __75%__ chance of spawning, and gravel __25%__.

__Done!__ Let water hit sandstone and sand and gravel will generate.

## Building

Use maven to build this project:

```
mvn clean compile assembly:single
```
