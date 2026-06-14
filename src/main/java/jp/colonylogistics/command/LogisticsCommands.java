package jp.colonylogistics.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import jp.colonylogistics.buildingstate.LogisticsBuildingKey;
import jp.colonylogistics.colony.ColonyLogisticsState;
import jp.colonylogistics.colony.LogisticsResolverRegistry;
import jp.colonylogistics.config.ColonyLogisticsConfig;
import jp.colonylogistics.container.ContainerSize;
import jp.colonylogistics.container.ContainerStandard;
import jp.colonylogistics.container.FreightContainerCoreBlockEntity;
import jp.colonylogistics.contract.DeliveryUnitType;
import jp.colonylogistics.contract.LogisticsContract;
import jp.colonylogistics.contract.LogisticsMarketSavedData;
import jp.colonylogistics.currency.CurrencyDenomination;
import jp.colonylogistics.currency.CurrencyService;
import jp.colonylogistics.profile.CarrierProfile;
import jp.colonylogistics.service.ContainerDockService;
import jp.colonylogistics.service.ContractService;
import jp.colonylogistics.service.FreightMarketService;
import jp.colonylogistics.trade.PlayerTradeContract;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class LogisticsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("colonylogistics")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("colony")
                        .then(Commands.literal("state")
                                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                        .executes(ctx -> showColonyState(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id")))))
                        .then(Commands.literal("setoffice")
                                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("level", IntegerArgumentType.integer(0, 5))
                                                .executes(ctx -> setOfficeAtSource(
                                                        ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "id"),
                                                        IntegerArgumentType.getInteger(ctx, "level")))))))
                .then(Commands.literal("dock")
                        .then(Commands.literal("bind")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("colony", IntegerArgumentType.integer(0))
                                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 5))
                                                        .executes(ctx -> bindDock(
                                                                ctx.getSource(),
                                                                BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                                                IntegerArgumentType.getInteger(ctx, "colony"),
                                                                IntegerArgumentType.getInteger(ctx, "level"))))))))
                .then(Commands.literal("terminal")
                        .then(Commands.literal("bind")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("colony", IntegerArgumentType.integer(0))
                                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 5))
                                                        .executes(ctx -> bindTerminal(
                                                                ctx.getSource(),
                                                                BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                                                IntegerArgumentType.getInteger(ctx, "colony"),
                                                                IntegerArgumentType.getInteger(ctx, "level")))))))
                        .then(Commands.literal("list")
                                .executes(ctx -> listPlayerTrades(ctx.getSource()))))
                .then(Commands.literal("freight")
                        .then(Commands.literal("generate")
                                .executes(ctx -> generateFreight(ctx.getSource())))
                        .then(Commands.literal("generatecontainers")
                                .executes(ctx -> generateContainerFreight(ctx.getSource(), null))
                                .then(Commands.argument("size", StringArgumentType.word())
                                        .executes(ctx -> generateContainerFreight(ctx.getSource(), StringArgumentType.getString(ctx, "size")))))
                        .then(Commands.literal("list")
                                .executes(ctx -> listFreight(ctx.getSource())))
                        .then(Commands.literal("mine")
                                .executes(ctx -> listMyFreight(ctx.getSource())))
                        .then(Commands.literal("accept")
                                .then(Commands.argument("contract", StringArgumentType.word())
                                        .executes(ctx -> acceptFreight(ctx.getSource(), StringArgumentType.getString(ctx, "contract")))))
                        .then(Commands.literal("deliver")
                                .executes(ctx -> deliverHeldParcel(ctx.getSource())))
                        .then(Commands.literal("purgefinished")
                                .executes(ctx -> purgeFinished(ctx.getSource()))))
                .then(Commands.literal("container")
                        .then(Commands.literal("spawn")
                                .then(Commands.argument("contract", StringArgumentType.word())
                                        .then(Commands.argument("dock", BlockPosArgument.blockPos())
                                                .then(Commands.argument("core", BlockPosArgument.blockPos())
                                                        .executes(ctx -> spawnContainer(
                                                                ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "contract"),
                                                                BlockPosArgument.getLoadedBlockPos(ctx, "dock"),
                                                                BlockPosArgument.getLoadedBlockPos(ctx, "core")))))))
                        .then(Commands.literal("deliver")
                                .then(Commands.argument("dock", BlockPosArgument.blockPos())
                                        .then(Commands.argument("core", BlockPosArgument.blockPos())
                                                .executes(ctx -> deliverContainer(
                                                        ctx.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(ctx, "dock"),
                                                        BlockPosArgument.getLoadedBlockPos(ctx, "core"))))))
                        .then(Commands.literal("localtest")
                                .then(Commands.argument("dock", BlockPosArgument.blockPos())
                                        .executes(ctx -> createLocalContainerTest(
                                                ctx.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(ctx, "dock"),
                                                "standard"))
                                        .then(Commands.argument("size", StringArgumentType.word())
                                                .executes(ctx -> createLocalContainerTest(
                                                        ctx.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(ctx, "dock"),
                                                        StringArgumentType.getString(ctx, "size"))))))
                        .then(Commands.literal("candidates")
                                .then(Commands.argument("dock", BlockPosArgument.blockPos())
                                        .executes(ctx -> showContainerSpawnCandidates(
                                                ctx.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(ctx, "dock"),
                                                "standard"))
                                        .then(Commands.argument("size", StringArgumentType.word())
                                                .executes(ctx -> showContainerSpawnCandidates(
                                                        ctx.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(ctx, "dock"),
                                                        StringArgumentType.getString(ctx, "size"))))))
                        .then(Commands.literal("range")
                                .then(Commands.argument("dock", BlockPosArgument.blockPos())
                                        .then(Commands.argument("core", BlockPosArgument.blockPos())
                                                .executes(ctx -> checkContainerRange(
                                                        ctx.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(ctx, "dock"),
                                                        BlockPosArgument.getLoadedBlockPos(ctx, "core"))))))
                        .then(Commands.literal("inspect")
                                .then(Commands.argument("core", BlockPosArgument.blockPos())
                                        .executes(ctx -> inspectContainer(
                                                ctx.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(ctx, "core")))))
                        .then(Commands.literal("diagnose")
                                .then(Commands.argument("dock", BlockPosArgument.blockPos())
                                        .executes(ctx -> diagnoseNearbyContainers(
                                                ctx.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(ctx, "dock"))))))
                .then(Commands.literal("minecolonies")
                        .then(Commands.literal("resolve")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> resolveMineColoniesBuilding(
                                                ctx.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))
                        .then(Commands.literal("blockentity")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> inspectHutBlockEntity(
                                                ctx.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(ctx, "pos"))))))
                .then(Commands.literal("balance")
                        .then(Commands.literal("show")
                                .executes(ctx -> showBalanceSettings(ctx.getSource()))))
                .then(Commands.literal("testing")
                        .then(Commands.literal("show")
                                .executes(ctx -> showTestingSettings(ctx.getSource())))
                        .then(Commands.literal("clearoverrides")
                                .executes(ctx -> clearTestingOverrides(ctx.getSource())))
                        .then(Commands.literal("selfdelivery")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setTestingBoolean(
                                                ctx.getSource(),
                                                "selfdelivery",
                                                BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(Commands.literal("loopbackfreight")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setTestingBoolean(
                                                ctx.getSource(),
                                                "loopbackfreight",
                                                BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(Commands.literal("loopbackcontainer")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setTestingBoolean(
                                                ctx.getSource(),
                                                "loopbackcontainer",
                                                BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(Commands.literal("automarket")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setTestingBoolean(
                                                ctx.getSource(),
                                                "automarket",
                                                BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(Commands.literal("autoinventory")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setTestingBoolean(
                                                ctx.getSource(),
                                                "autoinventory",
                                                BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(Commands.literal("autocontainer")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setTestingBoolean(
                                                ctx.getSource(),
                                                "autocontainer",
                                                BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(Commands.literal("inventorycap")
                                .then(Commands.argument("count", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> setTestingCap(
                                                ctx.getSource(),
                                                "inventorycap",
                                                IntegerArgumentType.getInteger(ctx, "count")))))
                        .then(Commands.literal("containercap")
                                .then(Commands.argument("count", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> setTestingCap(
                                                ctx.getSource(),
                                                "containercap",
                                                IntegerArgumentType.getInteger(ctx, "count"))))))
                .then(Commands.literal("profile")
                        .then(Commands.literal("me")
                                .executes(ctx -> showOwnProfile(ctx.getSource())))
                        .then(Commands.literal("setlevel")
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 5))
                                        .executes(ctx -> setOwnProfileLevel(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "level")))))
                        .then(Commands.literal("addcompleted")
                                .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                        .executes(ctx -> addOwnProfileCompleted(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "count")))))
                        .then(Commands.literal("reset")
                                .executes(ctx -> resetOwnProfile(ctx.getSource()))))
        );
    }

    private static int showColonyState(CommandSourceStack source, int colonyId) {
        ColonyLogisticsState state = LogisticsMarketSavedData.get(source.getLevel()).colonyState(colonyId);
        source.sendSuccess(() -> Component.literal("Colony " + colonyId
                + " office=" + state.logisticsOfficePos().toShortString()
                + " level=" + state.logisticsOfficeLevel()
                + " docks=" + state.dockPositions().size()
                + " maxDocks=" + state.limits().maxContainerDocks()
                + " activeContainerJobs=" + state.activeContainerJobs()
                + " maxContainer=" + state.limits().maxContainerSize()), false);
        return 1;
    }

    private static int setOfficeAtSource(CommandSourceStack source, int colonyId, int level) {
        ColonyLogisticsState state = LogisticsMarketSavedData.get(source.getLevel()).colonyState(colonyId);
        state.setLogisticsOffice(BlockPos.containing(source.getPosition()), level);
        LogisticsMarketSavedData.get(source.getLevel()).setDirty();
        source.sendSuccess(() -> Component.literal("Set logistics office marker for colony " + colonyId + " to level " + level), true);
        return 1;
    }

    private static int bindDock(CommandSourceStack source, BlockPos pos, int colonyId, int level) {
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(source.getLevel());
        ColonyLogisticsState state = data.colonyState(colonyId);
        boolean registered = state.registerDock(pos);
        data.dockState(LogisticsBuildingKey.of(source.getLevel(), pos));
        data.setDirty();
        source.sendSuccess(() -> Component.literal("Bound dock marker " + pos.toShortString() + " to colony " + colonyId + " level " + level + " registered=" + registered), true);
        return 1;
    }

    private static int bindTerminal(CommandSourceStack source, BlockPos pos, int colonyId, int level) {
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(source.getLevel());
        data.terminalState(LogisticsBuildingKey.of(source.getLevel(), pos));
        data.setDirty();
        source.sendSuccess(() -> Component.literal("Prepared terminal runtime state at " + pos.toShortString() + " for colony " + colonyId + " level " + level), true);
        return 1;
    }

    private static int listPlayerTrades(CommandSourceStack source) {
        var trades = LogisticsMarketSavedData.get(source.getLevel()).playerTrades().stream()
                .sorted(java.util.Comparator.comparingLong(PlayerTradeContract::createdGameTime).reversed())
                .toList();
        if (trades.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No player trade contracts. Create one from a Trade Terminal."), false);
            return 0;
        }
        for (PlayerTradeContract trade : trades) {
            source.sendSuccess(() -> Component.literal(trade.id()
                    + " | PLAYER_TRADE"
                    + " | colony=" + trade.colonyId()
                    + " terminal=" + trade.terminalPos().toShortString()
                    + " | " + trade.status()
                    + " | request=" + trade.requestedCount() + " " + trade.requestedItemId()
                    + " | reward=" + trade.rewardCount() + " " + trade.rewardItemId()), false);
        }
        return trades.size();
    }

    private static int generateFreight(CommandSourceStack source) {
        int generated = new FreightMarketService().ensureMinimumInventoryJobs(source.getLevel());
        source.sendSuccess(() -> Component.literal("Generated " + generated + " inventory freight job(s)."), true);
        return generated;
    }

    private static int generateContainerFreight(CommandSourceStack source, String requestedSize) {
        java.util.Optional<ContainerSize> size = java.util.Optional.empty();
        String suffix = "mixed";
        if (requestedSize != null && !requestedSize.equalsIgnoreCase("all")) {
            java.util.Optional<ContainerStandard> parsed = ContainerStandard.parse(requestedSize);
            if (parsed.isEmpty()) {
                source.sendFailure(Component.literal("Unknown container standard: " + requestedSize + ". Use standard, large, heavy, or all."));
                return 0;
            }
            size = java.util.Optional.of(parsed.get().physicalSize());
            suffix = parsed.get().serializedName();
        }
        int generated = new FreightMarketService().ensureMinimumContainerJobs(source.getLevel(), size);
        final String label = suffix;
        source.sendSuccess(() -> Component.literal("Generated " + generated + " " + label + " container freight job(s)."), true);
        return generated;
    }

    private static int createLocalContainerTest(CommandSourceStack source, BlockPos dockPos, String requestedSize) {
        java.util.Optional<ContainerStandard> parsed = ContainerStandard.parse(requestedSize);
        if (parsed.isEmpty()) {
            source.sendFailure(Component.literal("Unknown container standard: " + requestedSize + ". Use standard, large, or heavy."));
            return 0;
        }

        ContainerStandard standard = parsed.get();
        ContainerSize size = standard.physicalSize();
        if (!ColonyLogisticsConfig.allowLoopbackContainerFreightForTesting()) {
            source.sendFailure(Component.literal("Local same-dock container test jobs are disabled for multiplayer defaults. Temporarily run /colonylogistics testing loopbackcontainer true only for local diagnostics."));
            return 0;
        }
        var contract = new FreightMarketService().createLocalContainerTestJob(source.getLevel(), dockPos, size);
        if (contract.isEmpty()) {
            source.sendFailure(Component.literal("Could not create local container test job. Check that the dock is a usable Container Dock in a colony whose configured maxContainerStandard allows " + standard.serializedName() + "."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Created local same-dock container test job "
                + contract.get().id()
                + " at dock " + dockPos.toShortString()
                + " standard=" + standard.serializedName()
                + " physical=" + size
                + ". Accept it from the Logistics Office, then open this Dock to spawn and deliver."), true);
        return 1;
    }

    private static int listFreight(CommandSourceStack source) {
        var open = new FreightMarketService().openContracts(source.getLevel());
        if (open.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No open generated freight jobs. Player trades are stored separately; use /colonylogistics terminal list."), false);
            return 0;
        }
        for (LogisticsContract contract : open) {
            String type = contract.freightSpec()
                    .map(spec -> spec.deliveryUnitType().name())
                    .orElse("UNKNOWN");
            String extra = contract.freightSpec()
                    .filter(spec -> spec.deliveryUnitType() == DeliveryUnitType.CONTAINER_MULTIBLOCK)
                    .map(spec -> " | container=" + spec.containerRequirement().size()
                            + " weight=" + spec.containerRequirement().weightClass())
                    .orElse("");
            source.sendSuccess(() -> Component.literal(contract.id()
                    + " | " + type
                    + " | " + contract.originColonyId() + " -> " + contract.destinationColonyId()
                    + " | " + contract.status()
                    + extra
                    + " | reward=" + contract.reward().currencyAmount() + " " + contract.reward().currencyItemId()), false);
        }
        return open.size();
    }

    private static int listMyFreight(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var jobs = new FreightMarketService().playerContracts(source.getLevel(), player.getUUID());
        if (jobs.isEmpty()) {
            source.sendSuccess(() -> Component.literal("You have no active freight jobs."), false);
            return 0;
        }
        for (LogisticsContract contract : jobs) {
            String type = contract.freightSpec().map(spec -> spec.deliveryUnitType().name()).orElse("UNKNOWN");
            source.sendSuccess(() -> Component.literal(contract.id()
                    + " | " + type
                    + " | " + contract.originColonyId() + " -> " + contract.destinationColonyId()
                    + " | " + contract.status()), false);
        }
        return jobs.size();
    }

    private static int acceptFreight(CommandSourceStack source, String contractText) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        UUID id = parseUuidOrFail(source, contractText);
        if (id == null) return 0;

        var optionalContract = LogisticsMarketSavedData.get(source.getLevel()).contract(id);
        if (optionalContract.isEmpty() || optionalContract.get().freightSpec().isEmpty()) {
            source.sendFailure(Component.literal("Unknown or invalid freight contract."));
            return 0;
        }

        DeliveryUnitType type = optionalContract.get().freightSpec().get().deliveryUnitType();
        if (type == DeliveryUnitType.CONTAINER_MULTIBLOCK) {
            return new ContractService().acceptContainerFreight(player, id) ? 1 : 0;
        }

        var parcel = new ContractService().acceptInventoryFreight(player, id);
        if (parcel.isEmpty()) {
            return 0;
        }
        ItemStack stack = parcel.get();
        boolean inserted = player.getInventory().add(stack);
        if (!inserted && !stack.isEmpty()) {
            player.drop(stack, false);
        }
        source.sendSuccess(() -> Component.literal("Accepted inventory freight job and received parcel."), true);
        return 1;
    }

    private static int deliverHeldParcel(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        return new ContractService().completeInventoryFreight(player, held) ? 1 : 0;
    }

    private static int spawnContainer(CommandSourceStack source, String contractText, BlockPos dockPos, BlockPos corePos) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        UUID id = parseUuidOrFail(source, contractText);
        if (id == null) return 0;

        ContainerDockService.SpawnResult result = new ContainerDockService().spawnForAcceptedContract(player, dockPos, id, corePos);
        if (result != ContainerDockService.SpawnResult.SUCCESS) {
            source.sendFailure(ContainerDockService.spawnResultMessage(result));
            return 0;
        }

        source.sendSuccess(() -> ContainerDockService.spawnResultMessage(ContainerDockService.SpawnResult.SUCCESS).copy().append(Component.literal(" @ " + corePos.toShortString())), true);
        return 1;
    }

    private static int deliverContainer(CommandSourceStack source, BlockPos dockPos, BlockPos corePos) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        ContainerDockService service = new ContainerDockService();
        var resolvedContainer = service.findCoreForContainerBlock(source.getLevel(), corePos);
        if (resolvedContainer.isEmpty()) {
            source.sendFailure(Component.literal("No Freight Container Core or Part at " + corePos.toShortString()));
            return 0;
        }

        FreightContainerCoreBlockEntity container = resolvedContainer.get();
        ContainerDockService.DeliveryResult result = service.deliverContainer(player, dockPos, container);
        if (result != ContainerDockService.DeliveryResult.SUCCESS && result != ContainerDockService.DeliveryResult.REMOVED_NOTHING) {
            source.sendFailure(ContainerDockService.deliveryResultMessage(result));
            return 0;
        }

        source.sendSuccess(() -> ContainerDockService.deliveryResultMessage(result).copy().append(Component.literal(" @ " + container.getBlockPos().toShortString())), true);
        return 1;
    }

    private static int checkContainerRange(CommandSourceStack source, BlockPos dockPos, BlockPos corePos) {
        ContainerDockService service = new ContainerDockService();
        double distance = service.deliveryDistance(source.getLevel(), dockPos, corePos);
        boolean within = distance <= ContainerDockService.deliveryRadius();
        source.sendSuccess(() -> Component.literal("Container range check: " + within
                + " (distance=" + String.format(java.util.Locale.ROOT, "%.1f", distance)
                + " radius=" + ContainerDockService.deliveryRadius() + ")"), false);
        return within ? 1 : 0;
    }

    private static int showContainerSpawnCandidates(CommandSourceStack source, BlockPos dockPos, String requestedSize) {
        java.util.Optional<ContainerStandard> parsed = ContainerStandard.parse(requestedSize);
        if (parsed.isEmpty()) {
            source.sendFailure(Component.literal("Unknown container standard: " + requestedSize + ". Use standard, large, or heavy."));
            return 0;
        }

        ContainerStandard standard = parsed.get();
        ContainerSize size = standard.physicalSize();
        ContainerDockService service = new ContainerDockService();
        var dockForward = service.dockCargoForward(source.getLevel(), dockPos);
        var contractCandidates = service.contractSpawnCandidateCorePositions(source.getLevel(), dockPos, size);
        var contractCandidateSet = new java.util.LinkedHashSet<>(contractCandidates);
        var candidates = service.spawnCandidateCorePositions(source.getLevel(), dockPos, null, size);

        source.sendSuccess(() -> Component.literal("Spawn candidates for Dock " + dockPos.toShortString()
                + " standard=" + standard.serializedName()
                + " physical=" + size
                + " footprint=" + size.depth() + "x" + size.width() + "x" + size.height() + "(D/W/H)"
                + " dockForward=" + dockForward
                + " minPitch=(X=" + (size.depth() + jp.colonylogistics.config.ColonyLogisticsConfig.dockContainerSpawnHorizontalGap())
                + ",Z=" + (size.width() + jp.colonylogistics.config.ColonyLogisticsConfig.dockContainerSpawnHorizontalGap()) + ")"
                + " deliveryRadius=" + ContainerDockService.deliveryRadius()
                + " recognitionRadius=" + ContainerDockService.containerRecognitionRadius()
                + " contractIndoorCandidates=" + contractCandidates.size()), false);

        int shown = 0;
        for (BlockPos candidate : candidates) {
            shown++;
            double distance = service.deliveryDistance(source.getLevel(), dockPos, candidate);
            boolean inRange = distance <= ContainerDockService.deliveryRadius();
            boolean free = service.hasSpaceForContainer(source.getLevel(), dockPos, candidate, size);
            boolean contractSafe = contractCandidateSet.contains(candidate);
            int dx = candidate.getX() - dockPos.getX();
            int dy = candidate.getY() - dockPos.getY();
            int dz = candidate.getZ() - dockPos.getZ();
            final int index = shown;
            source.sendSuccess(() -> Component.literal("#" + index
                    + " core=" + candidate.toShortString()
                    + " rel=(" + dx + "," + dy + "," + dz + ")"
                    + " distance=" + String.format(java.util.Locale.ROOT, "%.1f", distance)
                    + " inRange=" + inRange
                    + " space=" + free
                    + " contractSafe=" + contractSafe
                    + " usable=" + (inRange && free)), false);
            if (shown >= 24) {
                break;
            }
        }
        return shown;
    }

    private static int inspectContainer(CommandSourceStack source, BlockPos corePos) {
        if (!(source.getLevel().getBlockEntity(corePos) instanceof FreightContainerCoreBlockEntity container)) {
            source.sendFailure(Component.literal("No Freight Container Core at " + corePos.toShortString()));
            return 0;
        }
        if (container.manifest().isEmpty()) {
            source.sendFailure(Component.literal("Freight Container Core has no manifest at " + corePos.toShortString()));
            return 0;
        }
        var manifest = container.manifest().get();
        source.sendSuccess(() -> Component.literal("Container " + manifest.containerId()
                + " contract=" + manifest.contractId()
                + " size=" + manifest.size()
                + " weight=" + manifest.weightClass()
                + " route=" + manifest.originColonyId() + " -> " + manifest.destinationColonyId()
                + " originDock=" + manifest.originDockPos().toShortString()
                + " destinationDock=" + manifest.destinationDockPos().toShortString()
                + " sealed=" + manifest.sealed()
                + " batch=" + manifest.batchText()), false);
        return 1;
    }

    private static int diagnoseNearbyContainers(CommandSourceStack source, BlockPos dockPos) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Container diagnose must be run by a player so carrier assignment can be checked."));
            return 0;
        }

        var level = source.getLevel();
        ContainerDockService service = new ContainerDockService();
        int radius = (int) Math.ceil(ContainerDockService.containerRecognitionRadius());
        var rows = BlockPos.betweenClosedStream(
                        dockPos.offset(-radius, -radius, -radius),
                        dockPos.offset(radius, radius, radius)
                )
                .map(BlockPos::immutable)
                .map(level::getBlockEntity)
                .filter(FreightContainerCoreBlockEntity.class::isInstance)
                .map(FreightContainerCoreBlockEntity.class::cast)
                .filter(container -> service.isWithinDeliveryRange(level, dockPos, container.getBlockPos(), ContainerDockService.containerRecognitionRadius()))
                .sorted(java.util.Comparator.comparingDouble(container -> service.deliveryDistance(level, dockPos, container.getBlockPos())))
                .toList();

        if (rows.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No Freight Container Core found near Dock " + dockPos.toShortString()
                    + " within recognition radius " + ContainerDockService.containerRecognitionRadius()), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Nearby container diagnostics for Dock " + dockPos.toShortString()
                + " count=" + rows.size()
                + " recognitionRadius=" + ContainerDockService.containerRecognitionRadius()
                + " deliveryRadius=" + ContainerDockService.deliveryRadius()), false);

        for (FreightContainerCoreBlockEntity container : rows) {
            if (container.manifest().isEmpty()) {
                BlockPos core = container.getBlockPos();
                source.sendSuccess(() -> Component.literal("core=" + core.toShortString() + " status=INVALID_CONTAINER manifest=missing"), false);
                continue;
            }
            var manifest = container.manifest().get();
            ContainerDockService.ContainerDeliveryAnalysis analysis = service.analyzeDelivery(level, player, dockPos, container);
            source.sendSuccess(() -> Component.literal("core=" + container.getBlockPos().toShortString()
                    + " status=" + analysis.deliveryStatus()
                    + " deliverable=" + analysis.deliverable()
                    + " distance=" + String.format(java.util.Locale.ROOT, "%.1f", analysis.distance())
                    + " contract=" + shortUuid(manifest.contractId())
                    + " contractStatus=" + analysis.contractStatus()
                    + " cargo=" + analysis.cargoName()
                    + " expected=" + analysis.expectedContainerText()
                    + " dstDock=" + manifest.destinationDockPos().toShortString()
                    + " batch=" + manifest.batchText()
                    + " hint=" + analysis.issueHint()), false);
        }
        return rows.size();
    }

    private static String shortUuid(UUID uuid) {
        String value = uuid.toString();
        return value.length() <= 8 ? value : value.substring(0, 8);
    }

    private static int resolveMineColoniesBuilding(CommandSourceStack source, BlockPos pos) {
        var resolved = LogisticsResolverRegistry.get().resolveBuilding(source.getLevel(), pos);
        if (resolved.isEmpty()) {
            source.sendFailure(Component.literal("No MineColonies building or colony at " + pos.toShortString()));
            return 0;
        }
        var building = resolved.get();
        source.sendSuccess(() -> Component.literal("Resolved MineColonies building: colony=" + building.colonyId()
                + " pos=" + building.buildingPos().toShortString()
                + " level=" + building.buildingLevel()
                + " built=" + building.built()
                + " cargoForward=" + building.cargoForward()
                + " id=" + building.buildingId()), false);
        return 1;
    }

    private static int inspectHutBlockEntity(CommandSourceStack source, BlockPos pos) {
        var level = source.getLevel();
        var state = level.getBlockState(pos);
        var blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            source.sendFailure(Component.literal("BlockEntity check: block=" + blockId
                    + " pos=" + pos.toShortString()
                    + " has no BlockEntity."));
            return 0;
        }

        var blockEntityId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
        source.sendSuccess(() -> Component.literal("BlockEntity check: block=" + blockId
                + " pos=" + pos.toShortString()
                + " beType=" + blockEntityId
                + " class=" + blockEntity.getClass().getName()), false);
        source.sendSuccess(() -> Component.literal("Colony Logistics hut expectation: logistics_office/container_dock/trade_terminal should use minecolonies:colonybuilding, not colonylogistics:* hut BlockEntities."), false);
        return 1;
    }

    private static int showBalanceSettings(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Colony Logistics balance settings (TOML-backed)"), false);
        for (int level = 0; level <= 5; level++) {
            int capturedLevel = level;
            source.sendSuccess(() -> Component.literal("  level " + capturedLevel
                    + ": openFreight=" + ColonyLogisticsConfig.levelMaxOpenFreightJobs(capturedLevel)
                    + " docks=" + ColonyLogisticsConfig.levelMaxContainerDocks(capturedLevel)
                    + " activeContainers=" + ColonyLogisticsConfig.levelMaxActiveContainerJobs(capturedLevel)
                    + " playerTrades=" + ColonyLogisticsConfig.levelMaxPlayerTradeContracts(capturedLevel)
                    + " maxContainer=" + ColonyLogisticsConfig.levelMaxContainerSize(capturedLevel)
                    + " inv=" + ColonyLogisticsConfig.levelInventoryFreightEnabled(capturedLevel)
                    + " container=" + ColonyLogisticsConfig.levelContainerFreightEnabled(capturedLevel)), false);
        }
        source.sendSuccess(() -> Component.literal("  generation: intervalTicks=" + ColonyLogisticsConfig.marketGenerationIntervalTicks()
                + " inventoryCap=" + ColonyLogisticsConfig.marketTestInventoryJobCapPerColony()
                + " containerCap=" + ColonyLogisticsConfig.marketTestContainerJobCapPerColony()
                + " lowInventory=" + ColonyLogisticsConfig.marketLowDifficultyInventoryPercent() + "%<=" + ColonyLogisticsConfig.marketLowDifficultyInventoryMax()
                + " lowContainer=" + ColonyLogisticsConfig.marketLowDifficultyContainerPercent() + "%<=" + ColonyLogisticsConfig.marketLowDifficultyContainerMax()
                + " weights standard/large/heavy="
                + ColonyLogisticsConfig.containerGenerationWeight(ContainerStandard.STANDARD) + "/"
                + ColonyLogisticsConfig.containerGenerationWeight(ContainerStandard.LARGE) + "/"
                + ColonyLogisticsConfig.containerGenerationWeight(ContainerStandard.HEAVY)), false);
        source.sendSuccess(() -> Component.literal("  carrier requirements: inventory=" + ColonyLogisticsConfig.inventoryRequiredCarrierLevel()
                + " standard=" + ColonyLogisticsConfig.requiredCarrierLevel(ContainerSize.SMALL)
                + " large=" + ColonyLogisticsConfig.requiredCarrierLevel(ContainerSize.LARGE)
                + " heavy=" + ColonyLogisticsConfig.requiredCarrierLevel(ContainerSize.HEAVY)), false);
        source.sendSuccess(() -> Component.literal("  deadlines: pickup=" + deadlineWindowLabel(ColonyLogisticsConfig.pickupWindowTicks())
                + " inventory=" + deadlineWindowLabel(ColonyLogisticsConfig.inventoryDeliveryWindowTicks())
                + " standard=" + deadlineWindowLabel(ColonyLogisticsConfig.deliveryWindowTicks(ContainerSize.SMALL))
                + " large=" + deadlineWindowLabel(ColonyLogisticsConfig.deliveryWindowTicks(ContainerSize.LARGE))
                + " heavy=" + deadlineWindowLabel(ColonyLogisticsConfig.deliveryWindowTicks(ContainerSize.HEAVY))
                + " late=" + ColonyLogisticsConfig.lateDeliveryRewardPercent() + "%"), false);
        source.sendSuccess(() -> Component.literal("  cancellation: allowCarrierCancel=" + ColonyLogisticsConfig.generatedJobsAllowCarrierCancel()
                + " countsAsFailed=" + ColonyLogisticsConfig.generatedJobsCancelCountsAsFailed()
                + " afterContainerSpawn=" + ColonyLogisticsConfig.generatedJobsAllowCancelAfterContainerSpawn()), false);
        source.sendSuccess(() -> Component.literal("  currency: base=" + new CurrencyService().defaultRewardCurrencyItemId()
                + " exchange=" + ColonyLogisticsConfig.currencyExchangeEnabled()
                + " denominations=" + currencyDenominationLabel()), false);
        source.sendSuccess(() -> Component.literal("  edit config/colonylogistics-common.toml and restart/reload the world to persist changes."), false);
        return 1;
    }

    private static String deadlineWindowLabel(long ticks) {
        return ticks <= 0L ? "unlimited" : Long.toString(ticks);
    }

    private static String currencyDenominationLabel() {
        java.util.List<CurrencyDenomination> denominations = new CurrencyService().registeredDenominations();
        if (denominations.isEmpty()) {
            return "none registered";
        }
        java.util.List<String> parts = new java.util.ArrayList<>();
        for (CurrencyDenomination denomination : denominations) {
            parts.add(denomination.itemId() + "=" + denomination.baseValue());
        }
        return String.join(", ", parts);
    }

    private static int showTestingSettings(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Colony Logistics testing settings (" + ColonyLogisticsConfig.runtimeTestOverrideLabel() + ")"), false);
        source.sendSuccess(() -> Component.literal("  selfdelivery=" + ColonyLogisticsConfig.allowSelfDeliveryForTesting()
                + " | loopbackfreight=" + ColonyLogisticsConfig.allowLoopbackFreightForTesting()
                + " | loopbackcontainer=" + ColonyLogisticsConfig.allowLoopbackContainerFreightForTesting()), false);
        source.sendSuccess(() -> Component.literal("  automarket=" + ColonyLogisticsConfig.autoGenerateMarketJobs()
                + " | autoinventory=" + ColonyLogisticsConfig.autoGenerateInventoryJobs()
                + " | autocontainer=" + ColonyLogisticsConfig.autoGenerateContainerJobs()), false);
        source.sendSuccess(() -> Component.literal("  inventorycap=" + ColonyLogisticsConfig.marketTestInventoryJobCapPerColony()
                + " | containercap=" + ColonyLogisticsConfig.marketTestContainerJobCapPerColony()
                + " | intervalTicks=" + ColonyLogisticsConfig.marketGenerationIntervalTicks()), false);
        source.sendSuccess(() -> Component.literal("  These command changes are runtime-only. Edit config/colonylogistics-common.toml for persistent defaults."), false);
        return 1;
    }

    private static int clearTestingOverrides(CommandSourceStack source) {
        ColonyLogisticsConfig.clearRuntimeTestOverrides();
        source.sendSuccess(() -> Component.literal("Cleared runtime Colony Logistics testing overrides. Effective values now come from TOML defaults."), true);
        return 1;
    }

    private static int setTestingBoolean(CommandSourceStack source, String key, boolean value) {
        switch (key) {
            case "selfdelivery" -> ColonyLogisticsConfig.setAllowSelfDeliveryForTestingOverride(value);
            case "loopbackfreight" -> ColonyLogisticsConfig.setAllowLoopbackFreightForTestingOverride(value);
            case "loopbackcontainer" -> ColonyLogisticsConfig.setAllowLoopbackContainerFreightForTestingOverride(value);
            case "automarket" -> ColonyLogisticsConfig.setAutoGenerateMarketJobsOverride(value);
            case "autoinventory" -> ColonyLogisticsConfig.setAutoGenerateInventoryJobsOverride(value);
            case "autocontainer" -> ColonyLogisticsConfig.setAutoGenerateContainerJobsOverride(value);
            default -> {
                source.sendFailure(Component.literal("Unknown testing boolean: " + key));
                return 0;
            }
        }
        source.sendSuccess(() -> Component.literal("Set runtime testing override " + key + "=" + value + ". This does not edit the TOML file."), true);
        return 1;
    }

    private static int setTestingCap(CommandSourceStack source, String key, int value) {
        switch (key) {
            case "inventorycap" -> ColonyLogisticsConfig.setMarketTestInventoryJobCapOverride(value);
            case "containercap" -> ColonyLogisticsConfig.setMarketTestContainerJobCapOverride(value);
            default -> {
                source.sendFailure(Component.literal("Unknown testing cap: " + key));
                return 0;
            }
        }
        source.sendSuccess(() -> Component.literal("Set runtime testing override " + key + "=" + value + ". This does not edit the TOML file."), true);
        return 1;
    }

    private static UUID parseUuidOrFail(CommandSourceStack source, String contractText) {
        try {
            return UUID.fromString(contractText);
        } catch (IllegalArgumentException ex) {
            source.sendFailure(Component.literal("Invalid UUID: " + contractText));
            return null;
        }
    }

    private static int purgeFinished(CommandSourceStack source) {
        int removed = LogisticsMarketSavedData.get(source.getLevel()).purgeFinishedContracts();
        source.sendSuccess(() -> Component.literal("Purged " + removed + " finished contract(s)."), true);
        return removed;
    }

    private static int showOwnProfile(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        CarrierProfile profile = LogisticsMarketSavedData.get(source.getLevel()).carrierProfile(player.getUUID());
        source.sendSuccess(() -> Component.literal("Carrier level=" + profile.carrierLevel()
                + " completed=" + profile.completedJobs()
                + " failed=" + profile.failedJobs()
                + " reputation=" + profile.reputation()
                + " distance=" + profile.totalDistanceDelivered()), false);
        return profile.carrierLevel();
    }

    private static int setOwnProfileLevel(CommandSourceStack source, int level) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(source.getLevel());
        CarrierProfile profile = data.carrierProfile(player.getUUID());
        profile.setLevelForTesting(level);
        data.setDirty();
        source.sendSuccess(() -> Component.literal("Set carrier profile for testing: level=" + profile.carrierLevel()
                + " completed=" + profile.completedJobs()
                + " reputation=" + profile.reputation()), true);
        return profile.carrierLevel();
    }

    private static int addOwnProfileCompleted(CommandSourceStack source, int count) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(source.getLevel());
        CarrierProfile profile = data.carrierProfile(player.getUUID());
        profile.addCompletedJobsForTesting(count);
        data.setDirty();
        source.sendSuccess(() -> Component.literal("Added completed jobs for testing: level=" + profile.carrierLevel()
                + " completed=" + profile.completedJobs()
                + " reputation=" + profile.reputation()), true);
        return profile.completedJobs();
    }

    private static int resetOwnProfile(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(source.getLevel());
        CarrierProfile profile = data.carrierProfile(player.getUUID());
        profile.resetForTesting();
        data.setDirty();
        source.sendSuccess(() -> Component.literal("Reset carrier profile for testing."), true);
        return 1;
    }

    private LogisticsCommands() {}
}
