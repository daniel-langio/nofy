# Authoring scenes

Entities and their actions live entirely under `assets/data/` as JSON. No Java code changes needed to add a new action, entity, or scene.

## Actions (`assets/data/actions/*.json`)

An action is a named constant-velocity movement + loop flag, reusable across entities:

```json
// assets/data/actions/bounce.json
{ "id": "bounce", "vx": 40, "vy": 30, "loop": true }
```

| field | meaning |
|---|---|
| `id` | referenced by entities/scenes as `actionId` |
| `vx`, `vy` | world units/sec, applied via `Sprite.translate` each frame |
| `loop` | whether the sprite animation loops or freezes on last frame |

## Entities (`assets/data/entities/*.json`)

An entity type: size, default action, and one animation binding per action it supports.

```json
// assets/data/entities/ball.json
{
  "id": "ball",
  "width": 16, "height": 16,
  "defaultActionId": "bounce",
  "animations": [
    { "actionId": "bounce", "spriteSheetPath": "sprites/prototypes/ball/Ball.png",
      "frameWidth": 64, "frameHeight": 64, "frameCount": 1, "fps": 1 }
  ]
}
```

An entity can only play an action it has a binding for. Multiple entities can bind the same action id to different sprites (e.g. `ball` and `ball-blue` both bind `bounce`) — that's how one action is reused across different-looking entities.

An entity with a `rigId` instead of `animations` is a rig-based character, assembled from body parts rather than a sprite sheet — see [Rigged characters](rigged-characters.md).

## Scenes (`assets/data/scenes/*.json`)

Places entity instances in a world:

```json
{
  "id": "ball-bounce",
  "entities": [
    { "entityDefinitionId": "ball", "instanceName": "Ball", "x": 20, "y": 90,
      "bounceBounds": { "x": 0, "y": 0, "width": 200, "height": 200 } }
  ]
}
```

Per-instance fields (all optional except `entityDefinitionId`):

| field | meaning |
|---|---|
| `instanceName` | label, falls back to `entityDefinitionId` |
| `x`, `y` | spawn position |
| `initialActionId` | overrides the entity's `defaultActionId` for this instance |
| `bounceBounds` | `{x,y,width,height}` — reflects velocity at these box edges (single-entity vs. static box) |
| `solid` | opts into entity-vs-entity collision (see [triggers-and-actions.md](triggers-and-actions.md)) |
| `actionRules` | see [triggers-and-actions.md](triggers-and-actions.md) |
| `cameraTarget` | camera follows this instance |

`SceneDefinition` also takes an optional top-level `simulationRange` (world units around the camera target; entities farther away skip physics — see triggers doc).

## Loading a scene

```java
Map<String, ActionDefinition> actions = ActionLoader.loadAll("data/actions");
Map<String, EntityDefinition> entities = EntityDefinitionLoader.loadAll("data/entities");
SceneLoader.populate(world, "data/scenes/ball-bounce-scene.json", entities, actions);
```

See any class under `lwjgl3/src/test/.../demo/*/` for a full runnable example.
