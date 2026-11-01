# Rigged characters

A sprite entity is one image playing one flipbook at a time. A **rigged** entity is a bone hierarchy
where each bone draws its own body part, so the parts move independently — which is what a person
needs: an arm that swings while the head stays level.

Arnold is the first one. Everything below uses him as the worked example.

## Describing one

Three files, no Java:

```
assets-src/characters/arnold/       # source art, committed
  rig.json                          #   bone hierarchy + pivots, from the rig kit
  parts/NN_*.svg                    #   one SVG per body part
assets/data/entities/arnold.json    # the entity definition
assets/data/scenes/arnold-scene.json
```

The entity definition needs only a rig and a height:

```json
{
  "id": "arnold",
  "rigId": "arnold",
  "height": 24,
  "defaultActionId": "idle"
}
```

`rigId` is what makes an entity rigged; without it the entity is built from sprite-sheet animations
as before. `width` is ignored — a character's width follows from its own proportions once scaled to
`height`. Scene instances reference it exactly like any other entity.

## Building the assets

```sh
tools/build-character.sh arnold [--scale 0.5]
```

This rasterizes each part SVG, trims it to its opaque bounds, and writes
`assets/sprites/characters/arnold/*.png` plus the runtime rig at `assets/data/rigs/arnold.json`.
Re-run it after editing any part SVG.

Trimming is not an optimization, it is required. Source parts are *full-canvas and pre-aligned* —
every part is a whole 2048×2048 image with the art sitting wherever it belongs — so shipping them
untrimmed would cost around 270 MB of VRAM for one character. Trimmed, Arnold is 68 KB.

`--scale` changes only image resolution. Every coordinate in the generated rig stays in source
canvas units, so re-rasterizing at a different scale never invalidates the rig.

**Outputs are committed.** The script needs `rsvg-convert` and Python Pillow, which CI does not have.

## Coordinate conventions

This is the part that is easy to get wrong, so it is worth stating plainly:

| | space |
|---|---|
| `rig.json`, and everything in it | canvas units, **y-down**, origin top-left |
| Everything after the `RiggedEntity` constructor | world units, **y-up**, origin at the entity's lower-left |

`RiggedEntity` performs that conversion **exactly once**, scaling by
`height / figureHeight` — where `figureHeight` is the character's actual opaque extent, not the
canvas, which is mostly empty.

Bone rotation is degrees counter-clockwise, relative to the parent.

## Two rules the implementation depends on

**Draw order is the rig's flat `z` list, never the bone tree.** The far arm draws *behind* the torso
and the near arm *in front*, though both are children of the same bone. A recursive depth-first draw
puts both arms on the same side and the character looks broken. `z` comes from each part's file-number
prefix, which is the order the art was authored in.

**Bones carry rotation and uniform scale only.** A parent's non-uniform scale combined with a rotated
child produces shear, and `Batch.draw` offers scale-then-rotate — five degrees of freedom where a
general affine needs six. Supporting it means leaving `SpriteBatch` for `PolygonSpriteBatch` or
hand-built vertices.

A rigged entity's collision box is a logical box from the rig's figure bounds, deliberately *not* the
posed art's extent — bounds that followed swinging limbs would make solid entities jitter apart.

## Previewing

```sh
./gradlew lwjgl3:runArnold       # bind pose, one shoulder swinging
./gradlew lwjgl3:runArnoldWalk   # walking under keyboard control
```

At rest Arnold stands in the rig's bind pose, which should match the rig kit's own preview render.
`runArnold` swings one shoulder so the forearm and hand visibly follow it.

`runArnoldWalk` moves him with the left/right arrows or A/D. Two things in it are worth borrowing:

- **Facing** is `RiggedEntity.setFlipped`, which reflects the whole rig across the character's own
  centre line — he turns on the spot, and no mirrored art is needed.
- **The walk is procedural**, computed from one phase angle rather than keyframes (see `WalkCycle`),
  because clips do not exist yet. Its travel speed is *derived* from the rig's leg length and swing
  angle rather than picked, which is what stops the feet skating; hard-coding a speed and tuning it
  by eye is how characters end up moon-walking.

## Not here yet

Keyframed animation clips (the plan is to author them in DragonBones and import), per-part sprite
animation such as blinking, appearance parameters such as hair graying with age, part-level triggers,
and atlas packing. Body parts are already their own drawable objects pinned to bones, which is the
seam all of those hang off — see `BodyPart`.
