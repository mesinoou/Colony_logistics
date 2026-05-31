# Phase 17.9.17 - System Chat Encoder Fix

## Problem
A collaborator reported disconnects when creating a player trade from the Trade Terminal:

```text
Internal Exception: io.netty.handler.codec.EncoderException: Failed to encode packet 'clientbound/minecraft:system_chat'
Caused by: Failed to encode: This value needs to be parsed as component translation{key='message.colonylogistics.trade_terminal.reward_must_be_currency', args=[mctradepost:mctp_coin]}
```

The multiplayer beta log showed the error occurred immediately after `CreatePlayerTradePayload` rejected a trade with `REWARD_NOT_CURRENCY` because a non-currency item was placed in the escrow reward slot.

## Root cause
Some server-side `sendSystemMessage(Component.translatable(...))` calls were sending argument-bearing translatable Components directly over `clientbound/minecraft:system_chat`. In Minecraft/NeoForge 1.21.x this can fail network encoding when the Component contains raw string arguments such as resource IDs.

## Fix
All server-side Colony Logistics system-chat sends are routed through `SafeSystemChat`.

`SafeSystemChat` now has an overload for `Component` that resolves any translatable/styled Component to plain text and sends a simple literal Component only.

This keeps system-chat payloads encoder-safe during multiplayer beta testing while preserving gameplay behavior.

## Verification performed in this workspace
- Confirmed the uploaded log points to `message.colonylogistics.trade_terminal.reward_must_be_currency` after `REWARD_NOT_CURRENCY`.
- Confirmed no direct `sendSystemMessage(...)` calls remain outside `SafeSystemChat`.
- Confirmed no Java-side direct use of `message.colonylogistics.trade_terminal.reward_must_be_currency` remains.
- Performed brace-balance checks for Java source files.

Gradle/compileJava was not run in this workspace because the provided project ZIP does not include a Gradle wrapper and this environment does not have Gradle installed.
