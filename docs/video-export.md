# Video export

Renders a scene headlessly to an MP4 — same `GameWorld`/`SceneLoader` pipeline as the interactive game, but driven by a fixed timestep (not wall-clock) and captured frame-by-frame via a bundled JCodec encoder. No ffmpeg needed at runtime; still needs a real display to render frames to (e.g. Xvfb on a headless server).

```
./gradlew lwjgl3:videoExportJar
java -jar lwjgl3/build/libs/Nofy-video-export-1.0.0.jar assets/data/video/ball-bounce.json
```

## Config (`assets/data/video/*.json`)

```json
{
  "actionsDir": "data/actions",
  "entitiesDir": "data/entities",
  "rigsDir": "data/rigs",
  "sceneFile": "data/scenes/two-balls-collide-scene.json",
  "outputPath": "output/two-balls-collide.mp4",
  "fps": 30,
  "durationSeconds": 60,
  "width": 600,
  "height": 600,
  "stopAfterCollisionCount": 2
}
```

| field | meaning |
|---|---|
| `actionsDir`, `entitiesDir`, `rigsDir`, `sceneFile` | same loaders as the interactive game — internal/classpath paths, resolved from inside the jar. `rigsDir` defaults to `data/rigs` and can be omitted |
| `outputPath` | plain filesystem path (not a Gdx asset), relative to wherever the jar is launched from |
| `fps`, `width`, `height` | output video specs |
| `durationSeconds` | **safety cap**, not the expected length — stops here regardless of anything else |
| `stopAfterCollisionCount` | optional; once this many entity-vs-entity collisions have occurred, stops ~1s later instead of running the full duration. Tune `durationSeconds` generously if using this — collision timing between two independently-bouncing entities isn't tightly predictable, and too short a cap risks never reaching the count |

`CollisionSystem` and `TriggerSystem` both run in this path (not just the interactive game) — a scene using `solid`/`actionRules`/`cameraTarget` behaves the same whether exported to video or run interactively, except no real mouse exists on a CI runner (so `HOVER`/`CLICK` rules never fire there). `TIME_OF_DAY`/`PROXIMITY`/`ENVIRONMENT_EVENT` rules work fine headlessly.
