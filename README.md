TradeCraft
==========

Custom villager tiers, trades and rebalancing.

Supported version: Spigot 1.8


## Download

If you don’t want to install from source, you can use our Jenkins site to [download the latest build](http://build.core-network.us:8080/job/Tradecraft/).

## Installation

1. Drop TradeCraft.jar into the plugins directory
2. Start the server
3. Stop the server

The plugin will create its `TradeCraft` directory and inside, `config.yml`, `example.yml` and `trades.sqlite`.

## Commands

    /tradecraft save
    
Run through console to save the trade queue to the DB. It will be automatically executed on world save as well.

    /tradecraft spawn <id> <profession>

Spawn a custom Villager which can act as a shop keeper. Parameters: `<id>` is villager type, `<profession>` is a special profession defined in config (`CustomProfessions`), outside of default ones. If you need this Villager to stop moving and be invulnerable you’ll have to use vanilla’s [`/entitydata`](http://minecraft.gamepedia.com/Commands#entitydata).

## Configuration

Refer to [`example.yml`](https://github.com/CoreNetwork/TradeCraft/blob/master/resources/example.yml) to learn how the plugin allows to edit trades.

All villagers defined in config will have their trades replaced by config values.

## Enchantment format

### Pick
Pick node is useful when you want to pick one of several different enchantments. It is inserted alongside other `enchant`:

```
- enchant:
    ...
- enchant:
    ...
- pick
    ...
```

Pick is simple parent node and will pick one of the child nodes:
```
- pick:
  - enchant:
      efficiency: 1
  - enchant:
      efficiency: 2
```

### Pick multiple items

You may also order it to pick multiple items. For example:

```    
- pick 2:
  - enchant:
      thorns: 1
  - enchant:
      feather_falling: 4
   - enchant:
      unbreaking: 3
  - enchant:
      protection: 4
```

Above example will pick 2 enchantments out of 4 provided.

### Pick Weights
In above examples, all items have same chance of being picked (their weight is 1). But you can specify weight of every item to alter its chance of being picked. You add weights after number of items (which must be provided in that case).

```    
- pick:
  - weights 10 5 3 1
  - enchant:
      thorns: 1
  - enchant:
      feather_falling: 4
   - enchant:
      unbreaking: 3
  - enchant:
      protection: 4
```
 
In above example first enchant has weight 10, second 5, third 3 and fourth 1. That means first enchant is 10 times more likely to be picked as fourth and twice more likely to be picked than second.

### Picking groups

```    
- pick:
  - enchant:
      thorns: 1
  - enchant:
      feather_falling: 4
   - group:
     - enchant:
        unbreaking: 3
    - enchant:
        protection: 4
```

You can create groups, so you can pick one enchant or group of multiple enchants.

## Villager Trade in 1.8

The plugin implements villager trading mechanics from Minecraft 1.8. You need to understand how tiers work and how trades restock to use this plugin effectively. Below is a short summary, based on the [wiki article](http://minecraft.gamepedia.com/Trading#1.8_Trading_Revamp) and in-game testing.

1. Villagers spawn with careers which determine their trades. For example a brown robe Villager formerly known as Farmer has been split into 4 careers: Farmer, Shepherd, Fisherman and Fletcher.
2. Trades are split in tiers. Think of tiers like as trade groups. When you trade with a Villager, you no longer unlock trades, you unlock new tiers which then come with trades (this can be 1 but usually it’s way more).
3. You no longer need to complete the last trade to refresh locked trades.
   * Completing any trade for the first time will always restock the whole tree (100%).
   * Completing any trade for the second time (and more) will have 20% chance to restock the whole tree.
   * Any trade can be completed 3-13 times. This means that any 2nd+ completion has 20% chance to restock the whole tree. If you trade something 8 times, the game will roll a dice 8 times.
   * This means that there is a chance for the same trade to be restocked when you close the GUI.
   * There is a super small chance to lock every trade and make a villager unusable.

## Known issues

Because the profession and its trades are pulled from the database when a villager is right clicked, some client-side mods might show different profession than the one accessed in the trade window.

Plugin will not co-operate with any other plugin influencing villager trades.
