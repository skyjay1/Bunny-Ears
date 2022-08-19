# Bunny Ears

This is a small but powerful client-side mod that can display armor using custom models. Users can define their own models using a resource pack.

Note: all users must have the mod and the same resource pack in order to see models on another player.

## Adding Custom Models

To add a custom hat, create a resource pack with the following:
- [Model](#model)
- [Texture](#texture)
- [hats.json](#json)

### Model

This mod uses the same model format as blocks and items. The model must be saved under `assets/[namespace]/models/hat/[model_name].json`

Tips for designing your model:
- The player head is 8x8x8 and is centered underneath the 0,0 plane _facing north_.
- Textures that are the same size as a block or item (16x16) are the most compatible. If larger textures are required, keep the image size a power of 2 (eg, 32x32 or 64x64)

### Texture

The texture for each model is defined in the model itself. These do not need to be registered anywhere.

### JSON

Register the model by adding an entry to `assets/bunnyears/hats.json`. The JSON file must contain the following:

- `[equipment_slot]`: the name of an item equipment slot. Can be `"head"`, `"chest"`, `"legs"`, or `"feet"`
  - `[item_name]`: the custom name of the item that displays a custom model. Required. Contains one or more of the following:
    - `[body_part]`: the body part to render the model. Can be `"head_part"`, `"chest_part"`, `"left_arm_part"`, `"right_arm_part"`, `"left_leg_part"`, or `"left_arm_part"`. Contains one or more of the following:
      - `damage`: the minimum amount of durability damage for the model to apply. Optional. Defaults to 0.
      - `model`: the namespaced ID of the model. Required.

#### Example

The following is a valid hats file located at `assets/bunnyears/hats.json`.

It specifies that head items with the custom name "bunny" should display the model at `assets/bunnyears/models/head/bunny_ears.json` when the item has no damage.
The hat will use the model at `assets/bunnyears/models/head/bunny_ears_one_down.json` when the item has at least 65% damage.
The hat will use the model at `assets/bunnyears/models/head/bunny_ears_both_down.json` when the item has at least 75% damage.
Additionally, a head item with this custom name will render a chest model at `assets/bunnyears/models/chest/bunny_tail.json`. The chestplate armor model will still render.

Next, the file specifies that leg items with the custom name "bunny" should display the models at `assets/bunnyears/models/left_leg/bunny_left_leg.json` and `assets/bunnyears/models/right_leg/bunny_right_leg.json`.

There are no entries under `chest` or `feet`, so this resource pack will not register any additional models for those equipment slots.

```
{
  "head": {
    "bunny": {
      "head_part": [
        {
          "model": "bunnyears:head/bunny_ears"
        },
        {
          "damage": 65,
          "model": "bunnyears:head/bunny_ears_one_down"
        },
        {
          "damage": 75,
          "model": "bunnyears:head/bunny_ears_both_down"
        }
      ],
      "chest_part": [
        {
          "model": "bunnyears:chest/bunny_tail"
        }
      ],
      "left_arm_part": [ ],
      "right_arm_part": [ ]
    }
  },
  "chest": { },
  "legs": {
    "bunny": {
      "left_leg_part": [
	    {
          "model": "bunnyears:left_leg/bunny_left_leg"
        }
	  ],
      "right_leg_part": [
	    {
          "model": "bunnyears:right_leg/bunny_right_leg"
        }
	  ]
	}
  },
  "feet": { }
}
```