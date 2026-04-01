# Project Guidelines

## Architecture
This repository is a Fabric client mod for Minecraft 1.21.11 targeting Java 21.
Core entrypoints are [src/main/java/com/example/squidchat/SquidChat.java](src/main/java/com/example/squidchat/SquidChat.java) and [src/main/java/com/example/squidchat/SquidChatClient.java](src/main/java/com/example/squidchat/SquidChatClient.java).
Keep responsibilities separated by package:
- `chat/` manages chat window state and notification behavior.
- `config/` owns config schema, persistence, and Mod Menu integration.
- `mixin/` contains Minecraft UI injections and should stay narrowly scoped to the target behavior.
- `sound/` registers custom sound events.

## Build And Validation
Use the Gradle wrapper from the repo root.
- `./gradlew build` for the default compile/package validation.
- `./gradlew runClient` for manual verification in a Fabric development client.

There is no established automated test suite in this repo right now. Do not claim test coverage unless you actually add and run tests.

## Conventions
Target Java 21 and the dependency versions declared in [gradle.properties](gradle.properties) and [build.gradle](build.gradle). Keep changes compatible with the current Minecraft, Yarn, and Fabric versions unless the task is explicitly a version upgrade.

Treat mixins as version-sensitive integration points. When editing files under [src/main/java/com/example/squidchat/mixin](src/main/java/com/example/squidchat/mixin), keep injections minimal, preserve existing naming such as the `squidchat$` method prefix, and verify corresponding targets in [src/main/resources/squidchat.mixins.json](src/main/resources/squidchat.mixins.json).

Persist chat window and user settings through [src/main/java/com/example/squidchat/config/ConfigManager.java](src/main/java/com/example/squidchat/config/ConfigManager.java) and the config model instead of introducing ad hoc storage.

When adding user-facing text, keybindings, or sounds, update the matching assets under [src/main/resources/assets/squidchat](src/main/resources/assets/squidchat), especially [src/main/resources/assets/squidchat/lang/en_us.json](src/main/resources/assets/squidchat/lang/en_us.json) and [src/main/resources/assets/squidchat/sounds.json](src/main/resources/assets/squidchat/sounds.json).

## Working In This Repo
Prefer editing sources under `src/main`. Do not modify generated or runtime output under `build/` or `run/` unless the task is explicitly about generated artifacts.

This mod is client-only, as declared in [src/main/resources/fabric.mod.json](src/main/resources/fabric.mod.json). Avoid server-side assumptions and keep new behavior compatible with client initialization and screen event lifecycles.

Read these files first when a task needs broader context:
- [build.gradle](build.gradle)
- [src/main/resources/fabric.mod.json](src/main/resources/fabric.mod.json)
- [src/main/java/com/example/squidchat/SquidChatClient.java](src/main/java/com/example/squidchat/SquidChatClient.java)
- [src/main/java/com/example/squidchat/chat/ChatWindowManager.java](src/main/java/com/example/squidchat/chat/ChatWindowManager.java)