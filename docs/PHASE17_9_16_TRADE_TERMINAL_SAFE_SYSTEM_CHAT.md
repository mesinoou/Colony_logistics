# Phase 17.9.16 - Trade Terminal safe system chat

## Problem

During multiplayer beta testing, one collaborator could load the mod and join the world, but using the Trade Terminal to create a player trade disconnected them with:

```text
Internal Exception: io.netty.handler.codec.DecoderException: Failed to decode packet 'clientbound/minecraft:system_chat'
```

The same action worked for another tester, which points away from the escrow creation logic itself and toward player/client-specific data being encoded in a system-chat component.

## Fix

Added:

```text
src/main/java/jp/colonylogistics/chat/SafeSystemChat.java
```

The helper sends deliberately simple literal system messages with a fixed Colony Logistics prefix. It converts dynamic values to plain sanitized strings before creating the `Component`.

Updated Trade Terminal C2S actions and player-trade service messages to use `SafeSystemChat` instead of dynamic `Component.translatable(...)` system-chat payloads:

```text
src/main/java/jp/colonylogistics/network/CreatePlayerTradePayload.java
src/main/java/jp/colonylogistics/network/DeliverPlayerTradePayload.java
src/main/java/jp/colonylogistics/network/CancelPlayerTradePayload.java
src/main/java/jp/colonylogistics/service/PlayerTradeService.java
```

## Scope

This phase intentionally leaves gameplay behavior unchanged:

- Trade creation validation is unchanged.
- Escrow reward handling is unchanged.
- Trade delivery and cancellation are unchanged.
- Multiplayer safety checks from Phase 17.9.12 remain in place.
- `[CL-MP]` beta logs from Phase 17.9.13 remain unchanged.

Only the Trade Terminal's server-to-player system-chat notifications were simplified.

## Beta-test notes

If this fixes the collaborator-only disconnect, the likely cause was a translatable component argument or dynamic chat value that one client could not decode. If the disconnect persists, verify that all testers have the exact same jar and dependency versions, then collect both server log and the affected client log around the first `clientbound/minecraft:system_chat` decode failure.
