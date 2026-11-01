# Running demos

Interactive previews live under `lwjgl3/src/test/java/.../lwjgl3/demo/<scenario>/` (a `Game` subclass + a launcher), each with its own Gradle task — never edit `MyGame` to peek at a scenario.

| demo | task | scene |
|---|---|---|
| ball-bounce | `./gradlew lwjgl3:runBallBounce` | one ball, box-bounce only |
| paddle-control | `./gradlew lwjgl3:runPaddleControl` | mouse-dragged paddle (`HOVER` + `followTrigger`) deflecting a ball via `CollisionSystem` |
| arnold | `./gradlew lwjgl3:runArnold` | the rig-based character standing in his bind pose, one shoulder swinging (see [Rigged characters](rigged-characters.md)) |
| arnold-walk | `./gradlew lwjgl3:runArnoldWalk` | Arnold walking under keyboard control — left/right arrows or A/D, facing the way he goes |

## Adding a new demo

1. `assets/data/scenes/<name>-scene.json` (+ any new actions/entities it needs).
2. `lwjgl3/src/test/java/.../demo/<name>/<Name>Demo.java` — a `Game` subclass whose `init()` loads that scene (copy an existing one).
3. `lwjgl3/src/test/java/.../demo/<name>/<Name>Launcher.java` — copy an existing launcher, point it at your `Demo` class. Use a square window matching the world's aspect ratio (200×200) to avoid `FitViewport` letterbox bands.
4. A `runX` `JavaExec` task in `lwjgl3/build.gradle` (`classpath = sourceSets.test.runtimeClasspath`), copy an existing one.

The default demo (`./gradlew lwjgl3:run`, no `run` prefix task name) is `MyGame`, loading `assets/data/scenes/demo.json` — kept as the "normal" entry point, not a scenario preview.
