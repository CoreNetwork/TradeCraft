TradeCraft
==========

Custom villager tiers, trades and rebalancing.

[CraftBukkit plugin](http://bukkit.org/)  
Supported version: 1.7.10


## Download

If you don’t want to install from source, you can use our Jenkins site to [download the latest build](http://build.core-network.us:8080/job/Tradecraft/).

## Installation

1. Stop your server
2. Drop TradeCraft.jar into the plugins directory
3. Start the server
4. Stop the server

The plugin will create its `TradeCraft` directory and inside, `config.yml`, `example.yml` and `trades.sqlite`.

## Commands

    /tradecraft save
    
Run through console to save the trade queue to the DB. It will be automatically executed on server stop but it’s advisable to run it as often as you save you world files.

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
      id: 32
      level: 1
  - enchant:
      id: 32
      level: 2
```

### Pick multiple items

You may also order it to pick multiple items. For example:

```    
- pick 2:
  - enchant:
      id: 7
      level: 1
      comment: thorns 1
  - enchant:
      id: 2
      level: 4
      comment: feather falling 4
   - enchant:
      id: 34
      level: 3
      comment: unbreaking 3
  - enchant:
      id: 0
      level: 4
      comment: protection 4
```

Above example will pick 2 enchantments out of 4 provided.

### Pick Weights
In above examples, all items have same chance of being picked (their weight is 1). But you can specify weight of every item to alter its chance of being picked. You add weights after number of items (which must be provided in that case).

```    
- pick:
  - weights 10 5 3 1
  - enchant:
      id: 7
      level: 1
      comment: thorns 1
  - enchant:
      id: 2
      level: 4
      comment: feather falling 4
   - enchant:
      id: 34
      level: 3
      comment: unbreaking 3
  - enchant:
      id: 0
      level: 4
      comment: protection 4
```
 
In above example first enchant has weight 10, second 5, third 3 and fourth 1. That means first enchant is 10 times more likely to be picked as fourth and twice more likely to be picked than second.

### Picking groups

```    
- pick:
  - enchant:
      id: 7
      level: 1
      comment: thorns 1
  - enchant:
      id: 2
      level: 4
      comment: feather falling 4
   - group:
     - enchant:
        id: 34
        level: 3
        comment: unbreaking 3
    - enchant:
        id: 0
        level: 4
        comment: protection 4
```

You can create groups, so you can pick one enchant or group of multiple enchants.

## Villager Trade in 1.8

The plugin implements villager trading mechanics from 1.8 snapshots. You need to understand how tiers work and how trades restock to use this plugin effectively. Below is a short summary, based on the [wiki article](http://minecraft.gamepedia.com/Trading#1.8_Trading_Revamp) and in-game testing.

1. Villagers spawn with careers which determine their trades. For example a brown robe Villager formerly known as Farmer has been split into 4 careers: Farmer, Shepherd, Fisherman and Fletcher.
2. Trades are split in tiers. Think of tiers like as trade groups. When you trade with a Villager, you no longer unlock trades, you unlock new tiers which then come with trades (this can be 1 but usually it’s way more).
   * Villagers start with first tier. Completing any trade from this tier will have 100% chance to unlock next tier.
   * Likewise, completing any trade from the last available tier will unlock the next tier (if there are more to choose from, depending on villager’s profession)
   * Completing trade on previous tiers has 20% of chance to unlock new tier.
3. You no longer need to complete the last trade to refresh locked trades.
   * Completing any trade for the first time will always restock the whole tree (100%).
   * Completing any trade for the second time (and more) will have 20% chance to restock the whole tree.
   * Any trade can be completed 3-13 times. This means that any 2nd+ completion has 20% chance to restock the whole tree. If you trade something 8 times, the game will roll a dice 8 times.
   * This means that there is a chance for the same trade to be restocked when you close the GUI.
   * There is a super small chance to lock every trade and make a villager unusable.


## Caveats

Because Minecaft 1.8 is not yet released, there are certain trade mechanics which depend on 1.7 and cannot be changed by the plugin.

1. Left slots (`itemA` and `itemB`) ignore damage value. You can sell black wool even if trade asks for white.
2. It's not advisable to use more than 64 items of the same type in left slots. This is a Minecraft bug: https://v.usetapes.com/X1dL5FZT8W 
3. Trying to trade more items than it's possible by the server will flash some ghost items in player's inventory for a second. It's a Minecraft 1.7 client-side bug and TradeCraft will actually refresh the inventory to remove the ghost items.
