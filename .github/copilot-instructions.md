# Project Guidelines

## Architecture
This repository is a Fabric client mod for Minecraft 26.1.2 targeting Java 25.
Core entrypoints are [src/main/java/com/squidpowered/squidchat/SquidChat.java](src/main/java/com/squidpowered/squidchat/SquidChat.java) and [src/main/java/com/squidpowered/squidchat/SquidChatClient.java](src/main/java/com/squidpowered/squidchat/SquidChatClient.java).
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
Target Java 25 and the dependency versions declared in [gradle.properties](gradle.properties) and [build.gradle](build.gradle). Keep changes compatible with the current Minecraft, official Mojang-named 26.1 toolchain, and current Fabric ecosystem unless the task is explicitly a version upgrade.

Treat mixins as version-sensitive integration points. When editing files under [src/main/java/com/squidpowered/squidchat/mixin](src/main/java/com/squidpowered/squidchat/mixin), keep injections minimal, preserve existing naming such as the `squidchat$` method prefix, and verify corresponding targets in [src/main/resources/squidchat.mixins.json](src/main/resources/squidchat.mixins.json).

Persist chat window and user settings through [src/main/java/com/squidpowered/squidchat/config/ConfigManager.java](src/main/java/com/squidpowered/squidchat/config/ConfigManager.java) and the config model instead of introducing ad hoc storage.

When adding user-facing text, keybindings, or sounds, update the matching assets under [src/main/resources/assets/squidchat](src/main/resources/assets/squidchat), especially [src/main/resources/assets/squidchat/lang/en_us.json](src/main/resources/assets/squidchat/lang/en_us.json) and [src/main/resources/assets/squidchat/sounds.json](src/main/resources/assets/squidchat/sounds.json).

## Working In This Repo
Prefer editing sources under `src/main`. Do not modify generated or runtime output under `build/` or `run/` unless the task is explicitly about generated artifacts.

This mod is client-only, as declared in [src/main/resources/fabric.mod.json](src/main/resources/fabric.mod.json). Avoid server-side assumptions and keep new behavior compatible with client initialization and screen event lifecycles.

Read these files first when a task needs broader context:
- [build.gradle](build.gradle)
- [src/main/resources/fabric.mod.json](src/main/resources/fabric.mod.json)
- [src/main/java/com/squidpowered/squidchat/SquidChatClient.java](src/main/java/com/squidpowered/squidchat/SquidChatClient.java)
- [src/main/java/com/squidpowered/squidchat/chat/ChatWindowManager.java](src/main/java/com/squidpowered/squidchat/chat/ChatWindowManager.java)

## AI / Agent Guidance

When interacting with this repository, follow these pragmatic rules:

- Use the Gradle wrapper from the repo root for build/run: `./gradlew build`, `./gradlew runClient`.
- Prefer edits under `src/main`; do not modify generated output under `build/` or runtime files in `run/`.
- Keep mixin changes minimal and verify targets in `src/main/resources/squidchat.mixins.json` when updating injections.
- Update assets under `src/main/resources/assets/squidchat` for any user-facing text, sounds, or language keys.

Example prompts to use with an AI agent:

- "Add a new config option to toggle chat notifications and persist it via `ConfigManager`."
- "Refactor `ChatWindowManager` to decouple rendering and state management; keep public behavior unchanged."
- "Implement a new sound event, register it, and add the entry to `assets/squidchat/sounds.json`."
- "Help me diagnose a mixin crash affecting the HUD on startup — suggest safe minimal changes."

Suggested next agent customizations to add (optional):

- `create-prompt` for common dev tasks: build, runClient, open main classes
- `create-skill` for codebase exploration focusing on `mixin`, `chat`, and `config` packages
- `create-instruction` scoped to mixin edits with guidance about preserving `squidchat$` prefixes

If you want, I can create any of the above agent customizations or produce a short README with quick commands.
