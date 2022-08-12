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

- `hats`: a list of hat specifications
  - `name`: the custom name of the item that, when worn on the head, displays this model. Required.
  - `hat`: one or more hat models. Required.
    - `damage`: the minimum amount of durability damage for the model to apply. Optional. Defaults to 0.
    - `model`: the namespaced ID of the model. Required.

#### Example

The following is a valid hats file located at `assets/bunnyears/hats.json`.

It specifies that head items with the custom name "bunny" should display the model at `assets/bunnyears/models/hat/bunny_ears.json` when the item has no damage.
The hat will use the model at `assets/bunnyears/models/hat/bunny_ears_one_down.json` when the item has at least 65% damage.
Finally, the hat will use the model at `assets/bunnyears/models/hat/bunny_ears_both_down.json` when the item has at least 75% damage.
```
{
  "hats": [
    {
      "name": "bunny",
      "hat": [
        {
          "model": "bunnyears:bunny_ears"
        },
        {
          "damage": 65,
          "model": "bunnyears:bunny_ears_one_down"
        },
        {
          "damage": 75,
          "model": "bunnyears:bunny_ears_both_down"
        }
      ]
    }
  ]
}
```