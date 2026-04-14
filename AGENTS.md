# SquidChat Agent Notes

## Quick Context
- This is a single-module Fabric client mod for Minecraft `26.1.2` on Java `25`.
- Real entrypoints are `src/main/java/com/squidpowered/squidchat/SquidChat.java` and `src/main/java/com/squidpowered/squidchat/SquidChatClient.java` as wired in `src/main/resources/fabric.mod.json`.
- Package boundaries are meaningful here: `chat/` owns chat window state + notification behavior, `config/` owns persistence + Mod Menu integration, `mixin/` owns UI injections, `sound/` owns sound registration.

## Commands
- Use the Gradle wrapper from repo root: `./gradlew build`.
- Manual verification uses the Fabric dev client: `./gradlew runClient`.
- There is no maintained test source set in `src/test`; do not claim automated coverage unless you add tests and run them.
- Useful focused verification commands that exist: `./gradlew test`, `./gradlew check`, `./gradlew genSources`.

## Files To Read First
- `build.gradle`
- `gradle.properties`
- `src/main/resources/fabric.mod.json`
- `src/main/resources/squidchat.mixins.json`
- `src/main/java/com/squidpowered/squidchat/SquidChatClient.java`
- `src/main/java/com/squidpowered/squidchat/chat/ChatWindowManager.java`

## Repo-Specific Constraints
- Keep changes under `src/main`; do not edit generated/runtime output under `build/`, `run/`, or the checked-in `bin/` copies.
- This mod is client-only (`"environment": "client"` in `fabric.mod.json`); avoid server-side assumptions.
- Target the current unobfuscated Fabric toolchain with official Mojang names. Keep compatibility with the versions pinned in `gradle.properties` unless the task is explicitly a version upgrade.
- Persist user settings through `ConfigManager` and `SquidChatConfig`; do not introduce ad hoc files or alternate storage.

## Mixins
- Treat `src/main/java/com/squidpowered/squidchat/mixin/*` as version-sensitive integration points.
- Keep injections minimal and verify any new or changed mixin class is listed in `src/main/resources/squidchat.mixins.json`.
- Preserve the existing `squidchat$...` helper/injection naming pattern in mixin methods.

## Assets And UI Hooks
- If you add user-facing text, update `src/main/resources/assets/squidchat/lang/en_us.json`.
- If you add or rename sounds, update both `src/main/resources/assets/squidchat/sounds.json` and `src/main/java/com/squidpowered/squidchat/sound/SquidChatSounds.java`.
- Chat window drag/resize behavior is split between `ChatScreenMixin` input hooks and `ChatHudMixin` render/size hooks; changes usually need both sides checked together.

## Existing Instructions
- `.github/copilot-instructions.md` already contains accurate repo guidance; keep `AGENTS.md` aligned with it instead of duplicating speculative workflow rules.
