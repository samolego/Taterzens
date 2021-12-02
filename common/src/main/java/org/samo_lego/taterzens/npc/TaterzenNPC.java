package org.samo_lego.taterzens.npc;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.compatibility.BungeeCompatibility;
import org.samo_lego.taterzens.compatibility.DisguiseLibCompatibility;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.interfaces.ITaterzenPlayer;
import org.samo_lego.taterzens.mixin.accessors.ChunkMapAccessor;
import org.samo_lego.taterzens.mixin.accessors.ClientboundAddPlayerPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.EntityTrackerEntryAccessor;
import org.samo_lego.taterzens.npc.ai.goal.*;
import org.samo_lego.taterzens.util.TextUtil;

import java.util.*;

import static org.samo_lego.taterzens.Taterzens.*;
import static org.samo_lego.taterzens.commands.NpcCommand.npcNode;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.gui.EditorGUI.createCommandGui;
import static org.samo_lego.taterzens.mixin.accessors.PlayerAccessor.getPLAYER_MODE_CUSTOMISATION;
import static org.samo_lego.taterzens.util.TextUtil.successText;

/**
 * The NPC itself.
 */
public class TaterzenNPC extends PathfinderMob implements CrossbowAttackMob, RangedAttackMob {

    /**
     * Data of the NPC.
     */
    private final NPCData npcData = new NPCData();

    private final MinecraftServer server;
    private final Player fakePlayer;
    private final LinkedHashMap<ResourceLocation, TaterzenProfession> professions = new LinkedHashMap<>();
    private GameProfile gameProfile;

    /**
     * Goals
     * Public so they can be accessed from professions.
     */
    public final LookAtPlayerGoal lookPlayerGoal = new LookAtPlayerGoal(this, Player.class, 8.0F);
    public final RandomLookAroundGoal lookAroundGoal = new RandomLookAroundGoal(this);
    public final RandomStrollGoal wanderAroundFarGoal = new RandomStrollGoal(this, 1.0D, 30);

    /**
     * Target selectors.
     */
    public final NearestAttackableTargetGoal<LivingEntity> followTargetGoal = new NearestAttackableTargetGoal<>(this, LivingEntity.class, 100, false, true, target -> !this.isAlliedTo(target));
    public final NearestAttackableTargetGoal<Monster> followMonstersGoal = new NearestAttackableTargetGoal<>(this, Monster.class, 100,false, true, target -> !this.isAlliedTo(target));
    public final NearestAttackableTargetGoal<Player> followPlayersGoal = new NearestAttackableTargetGoal<>(this, Player.class, 100,false, true, target -> !this.isAlliedTo(target));

    /**
     * Tracking movement
     */
    public final TrackEntityGoal trackLivingGoal = new TrackEntityGoal(this, LivingEntity.class, target -> !(target instanceof ServerPlayer) && target.isAlive());
    public final TrackEntityGoal trackPlayersGoal = new TrackEntityGoal(this, Player.class, target -> !((ServerPlayer) target).hasDisconnected());
    public final TrackUuidGoal trackUuidGoal = new TrackUuidGoal(this, entity -> entity.getUUID().equals(this.npcData.follow.targetUuid));


    /**
     * Used for {@link NPCData.Movement#PATH}.
     */
    public final MoveTowardsRestrictionGoal pathGoal = new MoveTowardsRestrictionGoal(this, 1.0D);
    public final DirectPathGoal directPathGoal = new DirectPathGoal(this, 1.0D);

    /**
     * Attack-based goals
     */
    public final RangedAttackGoal projectileAttackGoal = new RangedAttackGoal(this, 1.2D, 40, 40.0F);
    public final ReachMeleeAttackGoal reachMeleeAttackGoal = new ReachMeleeAttackGoal(this, 1.2D, false);
    public final TeamRevengeGoal revengeGoal = new TeamRevengeGoal(this);
    public final MeleeAttackGoal attackMonstersGoal = new MeleeAttackGoal(this, 1.2D, false);
    private @Nullable Vec3 respawnPosition;

    /**
     * Creates a TaterzenNPC.
     * You'd probably want to use
     * {@link org.samo_lego.taterzens.api.TaterzensAPI#createTaterzen(ServerLevel, String, Vec3, float[])} or
     * {@link org.samo_lego.taterzens.api.TaterzensAPI#createTaterzen(ServerPlayer, String)}
     * instead, as this one doesn't set the position and custom name.
     *
     * @param entityType Taterzen entity type
     * @param world Taterzen's world
     */
    public TaterzenNPC(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        this.maxUpStep = 0.6F;
        this.setCanPickUpLoot(true);
        this.setCustomNameVisible(true);
        this.setCustomName(this.getName());
        this.setInvulnerable(config.defaults.invulnerable);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.setSpeed(0.4F);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);

        this.gameProfile = new GameProfile(this.getUUID(), this.getName().getString());
        if(DISGUISELIB_LOADED) {
            DisguiseLibCompatibility.setGameProfile(this, this.gameProfile);
        }
        this.server = world.getServer();

        this.fakePlayer = new Player(world, this.blockPosition(), this.yHeadRot, new GameProfile(this.uuid, null)) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        this.fakePlayer.getEntityData().set(getPLAYER_MODE_CUSTOMISATION(), (byte) 0x7f);

        TATERZEN_NPCS.add(this);
    }

    /**
     * Creates default taterzen attributes.
     * @return attribute supplier builder.
     */
    public static AttributeSupplier.Builder createTaterzenAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.25D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2505D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    /**
     * Adds command to the list
     * of commands that will be executed on
     * right-clicking the Taterzen.
     * @param command command to add
     */
    public void addCommand(String command) {
        this.npcData.commands.add(command);
    }

    /**
     * Gets all available commands
     * @return array list of commands that will be executed on right click
     */
    public ArrayList<String> getCommands() {
        return new ArrayList<>(this.npcData.commands);
    }

    /**
     * Removes certain command from command list.
     * @param index index of where to remove command
     * @param bungee whether command to remove is a bungee or normal one
     */
    public void removeCommand(int index, boolean bungee) {
        ArrayList<?> cmds = bungee ? this.npcData.bungeeCommands : this.npcData.commands;
        if(index >= 0 && index < cmds.size())
            cmds.remove(index);
    }

    /**
     * Clears all the commands Taterzen
     * executes on right-click.
     *
     * @param bungee whether to clear normal or bungee commands
     */
    public void clearCommands(boolean bungee) {
        ArrayList<?> cmds = bungee ? this.npcData.bungeeCommands : this.npcData.commands;
        cmds.clear();
    }

    @Override
    protected int getPermissionLevel() {
        return this.npcData.permissionLevel;
    }

    public void setPermissionLevel(int newPermissionLevel) {
        this.npcData.permissionLevel = newPermissionLevel;
    }

    /**
     * Sets {@link org.samo_lego.taterzens.npc.NPCData.Movement movement type}
     * and initialises the goals.
     *
     * @param movement movement type
     */
    public void setMovement(NPCData.Movement movement) {
        this.npcData.movement = movement;
        this.goalSelector.removeGoal(this.wanderAroundFarGoal);
        this.goalSelector.removeGoal(this.directPathGoal);
        this.goalSelector.removeGoal(this.pathGoal);
        this.goalSelector.removeGoal(this.lookPlayerGoal);
        this.goalSelector.removeGoal(this.lookAroundGoal);

        // Follow types
        this.goalSelector.removeGoal(this.trackLivingGoal);
        this.goalSelector.removeGoal(this.trackUuidGoal);
        this.goalSelector.removeGoal(this.trackPlayersGoal);

        this.npcData.follow.targetUuid = null;
        this.npcData.follow.type = NPCData.FollowTypes.NONE;

        this.trackPlayersGoal.resetTrackingEntity();
        this.trackLivingGoal.resetTrackingEntity();

        for(TaterzenProfession profession : this.professions.values()) {
            profession.onMovementSet(movement);
        }

        if(movement != NPCData.Movement.NONE && movement != NPCData.Movement.FORCED_LOOK) {
            if(movement == NPCData.Movement.FORCED_PATH) {
                this.goalSelector.addGoal(4, directPathGoal);
            } else {
                this.goalSelector.addGoal(8, lookPlayerGoal);
                this.goalSelector.addGoal(9, lookAroundGoal);
                if(movement == NPCData.Movement.PATH)
                    this.goalSelector.addGoal(4, pathGoal);
                else if(movement == NPCData.Movement.FREE)
                    this.goalSelector.addGoal(6, wanderAroundFarGoal);
            }
        }
    }

    /**
     * Gets current movement of taterzen.
     * @return current movement
     */
    public NPCData.Movement getMovement() {
        return this.npcData.movement;
    }

    /**
     * Adds block position as a node in path of Taterzen.
     * @param blockPos position to add.
     */
    public void addPathTarget(BlockPos blockPos) {
        this.npcData.pathTargets.add(blockPos);
        this.restrictTo(this.npcData.pathTargets.get(0), 1);
    }

    @Override
    public SynchedEntityData getEntityData() {
        return this.fakePlayer.getEntityData();
    }

    /**
     * Handles name visibility on sneaking
     * @param sneaking whether npc's name should look like on sneaking.
     */
    @Override
    public void setShiftKeyDown(boolean sneaking) {
        this.fakePlayer.setShiftKeyDown(sneaking);
        super.setShiftKeyDown(sneaking);
    }

    /**
     * Sets the npc pose.
     * @param pose entity pose.
     */
    @Override
    public void setPose(Pose pose) {
        this.fakePlayer.setPose(pose);
        super.setPose(pose);
    }

    /**
     * Removes node from path targets.
     * @param blockPos position from path to remove
     */
    public void removePathTarget(BlockPos blockPos) {
        this.npcData.pathTargets.remove(blockPos);
    }

    /**
     * Gets the path nodes / targets.
     * @return array list of block positions.
     */
    public ArrayList<BlockPos> getPathTargets() {
        return this.npcData.pathTargets;
    }

    /**
     * Clears all the path nodes / targets.
     */
    public void clearPathTargets() {
        this.npcData.pathTargets = new ArrayList<>();
        this.npcData.currentMoveTarget = 0;
    }

    /**
     * Ticks the movement depending on {@link org.samo_lego.taterzens.npc.NPCData.Movement} type
     */
    @Override
    public void aiStep() {
        if(this.npcData.equipmentEditor != null)
            return;

        // Profession event
        professionLoop:
        for(TaterzenProfession profession : this.professions.values()) {
            InteractionResult result = profession.tickMovement();
            switch(result) {
                case CONSUME: // Stop processing others, but continue with base Taterzen movement tick
                    break professionLoop;
                case FAIL: // Stop whole movement tick
                    return;
                case SUCCESS: // Continue with super, but skip Taterzen's movement tick
                    super.aiStep();
                    return;
                default: // Continue with other professions
                    break;
            }
        }

        // FORCED_LOOK is processed in tick(), as we get nearby players there
        if(this.npcData.movement != NPCData.Movement.NONE && this.npcData.movement != NPCData.Movement.FORCED_LOOK) {
            this.setYRot(this.yHeadRot); // Rotates body as well
            LivingEntity target = this.getTarget();

            if((this.npcData.movement == NPCData.Movement.FORCED_PATH && !this.npcData.pathTargets.isEmpty()) && !this.isPathFinding()) {
                // Checking here as well (if path targets size was changed during the previous tick)
                if(this.npcData.currentMoveTarget >= this.npcData.pathTargets.size())
                    this.npcData.currentMoveTarget = 0;

                if(this.getRestrictCenter().distSqr(this.position(), true) < 5.0D) {
                    if(++this.npcData.currentMoveTarget >= this.npcData.pathTargets.size())
                        this.npcData.currentMoveTarget = 0;

                    // New target
                    this.restrictTo(this.npcData.pathTargets.get(this.npcData.currentMoveTarget), 1);
                }
            } else if(this.npcData.movement == NPCData.Movement.PATH && !this.pathGoal.canContinueToUse() && !this.npcData.pathTargets.isEmpty()) {
                // Checking here as well (if path targets size was changed during the previous tick)
                if(this.npcData.currentMoveTarget >= this.npcData.pathTargets.size())
                    this.npcData.currentMoveTarget = 0;

                if(this.npcData.pathTargets.get(this.npcData.currentMoveTarget).distSqr(this.position(), true) < 5.0D) {
                    if(++this.npcData.currentMoveTarget >= this.npcData.pathTargets.size())
                        this.npcData.currentMoveTarget = 0;

                    // New target
                    this.restrictTo(this.npcData.pathTargets.get(this.npcData.currentMoveTarget), 1);
                }
            }

            super.aiStep();
            if(this.isAggressive() && this.npcData.jumpWhileAttacking && this.onGround && target != null && this.distanceToSqr(target) < 4.0D && this.random.nextInt(5) == 0)
                this.jumpFromGround();
        } else {
            // As super.aiStep() isn't executed, we check for items that are available to be picked up
            if (this.isAlive() && !this.dead) {
                List<ItemEntity> list = this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0D, 0.0D, 1.0D));

                for (ItemEntity itemEntity : list) {
                    if (!itemEntity.isRemoved() && !itemEntity.getItem().isEmpty() && !itemEntity.hasPickUpDelay() && this.wantsToPickUp(itemEntity.getItem())) {
                        this.pickUpItem(itemEntity);
                    }
                }
            }
        }
    }

    /**
     * Ticks the Taterzen and sends appropriate messages
     * to players in radius of 2 blocks.
     */
    @Override
    public void tick() {
        super.tick();

        AABB box = this.getBoundingBox().inflate(4.0D);
        List<ServerPlayer> players = this.level.getEntitiesOfClass(ServerPlayer.class, box);

        if(!this.npcData.messages.isEmpty()) {
            for(ServerPlayer player: players) {
                // Filter them here (not use a predicate above)
                // as we need the original list below
                if(((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.MESSAGES || this.distanceTo(player) > config.messages.speakDistance)
                    continue;

                ITaterzenPlayer pl = (ITaterzenPlayer) player;
                int msgPos = pl.getLastMsgPos(this.getUUID());
                if (msgPos >= this.npcData.messages.size())
                    msgPos = 0;
                if (this.npcData.messages.get(msgPos).getSecond() < pl.ticksSinceLastMessage(this.getUUID())) {
                    player.sendMessage(
                            new TranslatableComponent(config.messages.structure, this.getName().copy(), this.npcData.messages.get(msgPos).getFirst()),
                            this.uuid
                    );
                    // Resetting message counter
                    pl.resetMessageTicks(this.getUUID());

                    ++msgPos;
                    // Setting new message position
                    pl.setLastMsgPos(this.getUUID(), msgPos);
                }
            }
        }
        if(!players.isEmpty()) {
            // We tick forced look here, as we already have players list.
            if(this.npcData.movement == NPCData.Movement.FORCED_LOOK) {
                ServerPlayer player = players.get(this.random.nextInt(players.size()));

                this.lookAt(player, 60.0F, 60.0F);
                this.setYHeadRot(this.getYRot());
            }
            // Tick profession
            for(TaterzenProfession profession : this.professions.values()) {
                profession.onPlayersNearby(players);
            }
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        ClientboundAddPlayerPacket ClientboundAddPlayerPacket = new ClientboundAddPlayerPacket(this.fakePlayer);
        //noinspection ConstantConditions
        ClientboundAddPlayerPacketAccessor addPlayerPacketAccessor = (ClientboundAddPlayerPacketAccessor) ClientboundAddPlayerPacket;
        addPlayerPacketAccessor.setId(this.getId());
        addPlayerPacketAccessor.setUuid(this.getUUID());
        addPlayerPacketAccessor.setX(this.getX());
        addPlayerPacketAccessor.setY(this.getY());
        addPlayerPacketAccessor.setZ(this.getZ());
        addPlayerPacketAccessor.setYRot((byte)((int)(this.getYHeadRot() * 256.0F / 360.0F)));
        addPlayerPacketAccessor.setXRot((byte)((int)(this.getXRot() * 256.0F / 360.0F)));

        return ClientboundAddPlayerPacket;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    /**
     * Sets the custom name
     * @param name new name to be set.
     */
    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        String profileName = "Taterzen";
        if(name != null) {
            profileName = name.getString();
            if(name.getString().length() > 16) {
                // Minecraft kicks you if player has name longer than 16 chars in GameProfile
                profileName = name.getString().substring(0, 16);
            }
        }
        CompoundTag skin = null;
        if(this.gameProfile != null)
            skin = this.writeSkinToTag(this.gameProfile);
        this.gameProfile = new GameProfile(this.getUUID(), profileName);
        if(skin != null) {
            this.setSkinFromTag(skin);
            this.sendProfileUpdates();
        }
    }

    /**
     * Updates Taterzen's {@link GameProfile} for others.
     */
    public void sendProfileUpdates() {
        if(DISGUISELIB_LOADED)
            DisguiseLibCompatibility.setGameProfile(this, this.gameProfile);
        else {
            ServerChunkCache manager = (ServerChunkCache) this.level.getChunkSource();
            ChunkMap storage = manager.chunkMap;
            EntityTrackerEntryAccessor trackerEntry = ((ChunkMapAccessor) storage).getEntityMap().get(this.getId());
            if(trackerEntry != null)
                trackerEntry.getSeenBy().forEach(tracking -> trackerEntry.getPlayer().addPairing(tracking.getPlayer()));
        }
    }


    /**
     * Applies skin from {@link GameProfile}.
     *
     * @param texturesProfile GameProfile containing textures.
     */
    public void applySkin(GameProfile texturesProfile) {
        if(this.gameProfile == null)
            return;

        // Setting new skin
        setSkinFromTag(writeSkinToTag(texturesProfile));

        if(DISGUISELIB_LOADED) {
            DisguiseLibCompatibility.setGameProfile(this, this.gameProfile);
        }

        // Sending updates
        this.sendProfileUpdates();
    }

    /**
     * Sets the Taterzen skin from tag
     * @param tag compound tag containing the skin
     */
    public void setSkinFromTag(CompoundTag tag) {
        // Clearing current skin
        try {
            PropertyMap map = this.gameProfile.getProperties();
            Property skin = map.get("textures").iterator().next();
            map.remove("textures", skin);
        } catch (NoSuchElementException ignored) { }
        // Setting the skin
        try {
            String value = tag.getString("value");
            String signature = tag.getString("signature");

            if(!value.isEmpty() && !signature.isEmpty()) {
                PropertyMap propertyMap = this.gameProfile.getProperties();
                propertyMap.put("textures", new Property("textures", value, signature));
            }

        } catch (Error ignored) { }
    }

    /**
     * Writes skin to tag
     * @param profile game profile containing skin
     *
     * @return compound tag with skin values
     */
    public CompoundTag writeSkinToTag(GameProfile profile) {
        CompoundTag skinTag = new CompoundTag();
        try {
            PropertyMap propertyMap = profile.getProperties();
            Property skin = propertyMap.get("textures").iterator().next();

            skinTag.putString("value", skin.getValue());
            skinTag.putString("signature", skin.getSignature());
        } catch (NoSuchElementException ignored) { }

        return skinTag;
    }
    /**
     * Loads Taterzen from {@link CompoundTag}.
     * @param tag tag to load Taterzen from.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag npcTag = tag.getCompound("TaterzenNPCTag");

        CompoundTag tags = npcTag.getCompound("Tags");
        this.setLeashable(tags.getBoolean("Leashable"));
        this.setPushable(tags.getBoolean("Pushable"));
        this.setPerformAttackJumps(tags.getBoolean("JumpAttack"));
        this.allowEquipmentDrops(tags.getBoolean("DropsAllowed"));
        this.setShiftKeyDown(tags.getBoolean("SneakNameType"));
        this.setAllowSounds(tags.getBoolean("AllowSounds"));

        // Skin layers
        this.setSkinLayers(npcTag.getByte("SkinLayers"));

        // Multiple commands
        ListTag commands = (ListTag) npcTag.get("Commands");
        if(commands != null) {
            commands.forEach(cmdTag -> this.addCommand(cmdTag.getAsString()));
        }

        // Bungee commands
        ListTag bungeeCommands = (ListTag) npcTag.get("BungeeCommands");
        if(bungeeCommands != null) {
            bungeeCommands.forEach(cmdTag -> {
                ListTag cmdList = (ListTag) cmdTag;
                String command = cmdList.get(0).getAsString();
                String player = cmdList.get(1).getAsString();
                String argument = cmdList.get(2).getAsString();
                this.addBungeeCommand(BungeeCompatibility.valueOf(command), player, argument);
            });
        }

        ListTag pathTargets = (ListTag) npcTag.get("PathTargets");
        if(pathTargets != null) {
            if(pathTargets.size() > 0) {
                pathTargets.forEach(posTag -> {
                    if(posTag instanceof CompoundTag pos) {
                        BlockPos target = new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"));
                        this.addPathTarget(target);
                    }
                });
                this.restrictTo(this.npcData.pathTargets.get(0), 1);
            }
        }

        ListTag messages = (ListTag) npcTag.get("Messages");
        if(messages != null && messages.size() > 0) {
            messages.forEach(msgTag -> {
                CompoundTag msgCompound = (CompoundTag) msgTag;
                this.addMessage(TextUtil.fromNbtElement(msgCompound.get("Message")), msgCompound.getInt("Delay"));
            });
        }

        this.setPermissionLevel(npcTag.getInt("PermissionLevel"));
        this.setBehaviour(NPCData.Behaviour.valueOf(npcTag.getString("Behaviour")));

        String profileName = this.getName().getString();
        if(profileName.length() > 16) {
            // Minecraft kicks you if player has name longer than 16 chars in GameProfile
            profileName = profileName.substring(0, 16);
        }

        this.gameProfile = new GameProfile(this.getUUID(), profileName);
        if(DISGUISELIB_LOADED) {
            DisguiseLibCompatibility.setGameProfile(this, this.gameProfile);
        }

        // Skin is cached
        CompoundTag skinTag = npcTag.getCompound("skin");
        this.setSkinFromTag(skinTag);

        // Profession initialising
        ListTag professions = (ListTag) npcTag.get("Professions");
        if(professions != null && professions.size() > 0) {
            professions.forEach(professionTag -> {
                CompoundTag professionCompound = (CompoundTag) professionTag;

                ResourceLocation professionId = new ResourceLocation(professionCompound.getString("ProfessionType"));
                if(PROFESSION_TYPES.containsKey(professionId)) {
                    TaterzenProfession profession = PROFESSION_TYPES.get(professionId).create(this);
                    this.addProfession(professionId, profession);

                    // Parsing profession data
                    profession.readNbt(professionCompound.getCompound("ProfessionData"));
                }
                else
                    Taterzens.LOGGER.error("Taterzen {} was saved with profession id {}, but none of the mods provides it.", this.getName().getString(), professionId);
            });
        }

        // Follow targets
        CompoundTag followTag = npcTag.getCompound("Follow");
        if(followTag.contains("Type"))
            this.setFollowType(NPCData.FollowTypes.valueOf(followTag.getString("Type")));

        if(followTag.contains("UUID"))
            this.setFollowUuid(followTag.getUUID("UUID"));

        if(npcTag.contains("Pose"))
            this.setPose(Pose.valueOf(npcTag.getString("Pose")));
        else
            this.setPose(Pose.STANDING);

        CompoundTag bodyRotations = npcTag.getCompound("BodyRotations");
        if(!bodyRotations.isEmpty()) {
            this.setXRot(bodyRotations.getFloat("XRot"));
            this.setYRot(bodyRotations.getFloat("YRot"));
        }

        this.setMovement(NPCData.Movement.valueOf(npcTag.getString("movement")));
    }

    /**
     * Saves Taterzen to {@link CompoundTag tag}.
     *
     * @param tag tag to save Taterzen to.
     */
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        CompoundTag npcTag = new CompoundTag();

        // Vanilla saves CustomNameVisible only if set to true
        super.setCustomNameVisible(tag.contains("CustomNameVisible"));

        npcTag.putString("movement", this.npcData.movement.toString());

        CompoundTag tags = new CompoundTag();

        tags.putBoolean("Leashable", this.npcData.leashable);
        tags.putBoolean("Pushable", this.npcData.pushable);
        tags.putBoolean("JumpAttack", this.npcData.jumpWhileAttacking);
        tags.putBoolean("DropsAllowed", this.npcData.allowEquipmentDrops);
        tags.putBoolean("SneakNameType", this.isShiftKeyDown());
        tags.putBoolean("AllowSounds", this.npcData.allowSounds);

        npcTag.put("Tags", tags);


        npcTag.putByte("SkinLayers", this.fakePlayer.getEntityData().get(getPLAYER_MODE_CUSTOMISATION()));

        // Commands
        ListTag commands = new ListTag();
        this.npcData.commands.forEach(cmd -> commands.add(StringTag.valueOf(cmd)));
        npcTag.put("Commands", commands);

        // Bungee commands
        ListTag bungee = new ListTag();
        this.npcData.bungeeCommands.forEach(cmd -> {
            String command = cmd.getLeft().toString();
            String player = cmd.getMiddle();
            String argument = cmd.getRight();

            ListTag triple = new ListTag();
            triple.add(StringTag.valueOf(command));
            triple.add(StringTag.valueOf(player));
            triple.add(StringTag.valueOf(argument));

            bungee.add(triple);
        });
        npcTag.put("BungeeCommands", bungee);

        npcTag.put("skin", writeSkinToTag(this.gameProfile));

        ListTag pathTargets = new ListTag();
        this.npcData.pathTargets.forEach(blockPos -> {
            CompoundTag pos = new CompoundTag();
            pos.putInt("x", blockPos.getX());
            pos.putInt("y", blockPos.getY());
            pos.putInt("z", blockPos.getZ());
            pathTargets.add(pos);
        });
        npcTag.put("PathTargets", pathTargets);

        // Messages
        ListTag messages = new ListTag();
        this.npcData.messages.forEach(pair -> {
            CompoundTag msg = new CompoundTag();
            msg.put("Message", TextUtil.toNbtElement(pair.getFirst()));
            msg.putInt("Delay", pair.getSecond());
            messages.add(msg);
        });
        npcTag.put("Messages", messages);

        npcTag.putInt("PermissionLevel", this.npcData.permissionLevel);
        npcTag.putString("Behaviour", this.npcData.behaviour.toString());

        // Profession initialising
        ListTag professions = new ListTag();
        this.professions.forEach((id, profession) -> {
            CompoundTag professionCompound = new CompoundTag();

            professionCompound.putString("ProfessionType", id.toString());

            CompoundTag professionData = new CompoundTag();
            profession.saveNbt(professionData);
            professionCompound.put("ProfessionData", professionData);

            professions.add(professionCompound);
        });
        npcTag.put("Professions", professions);

        CompoundTag followTag = new CompoundTag();
        followTag.putString("Type", this.npcData.follow.type.toString());

        if(this.npcData.follow.targetUuid != null)
            followTag.putUUID("UUID", this.npcData.follow.targetUuid);

        npcTag.put("Follow", followTag);

        npcTag.putString("Pose", this.getPose().toString());

        CompoundTag bodyRotations = new CompoundTag();
        //fixme rotations are not getting saved
        bodyRotations.putFloat("XRot", this.getXRot());
        bodyRotations.putFloat("YRot", this.getYRot());
        npcTag.put("BodyRotations", bodyRotations);

        tag.put("TaterzenNPCTag", npcTag);
    }

    /**
     * Sets player as equipment editor.
     * @param player player that will be marked as equipment editor.
     */
    public void setEquipmentEditor(@Nullable Player player) {
        this.npcData.equipmentEditor = player;
    }

    /**
     * Sets player as equipment editor.
     * @param player player to check.
     * @return true if player is equipment editor of the NPC, otherwise false.
     */
    public boolean isEquipmentEditor(@NotNull Player player) {
        return player.equals(this.npcData.equipmentEditor);
    }

    /**
     * Handles interaction (right clicking on the NPC).
     * @param player player interacting with NPC
     * @param pos interaction pos
     * @param hand player's interacting hand
     * @return {@link InteractionResult#PASS} if NPC has a right click action, otherwise {@link InteractionResult#FAIL}
     */
    @Override
    public InteractionResult interactAt(Player player, Vec3 pos, InteractionHand hand) {
        long lastAction = ((ServerPlayer) player).getLastActionTime();

        // As weird as it sounds, this gets triggered twice, first time with the item stack player is holding
        // then with "air" if fake type is player / armor stand
        if(lastAction - ((ITaterzenPlayer) player).getLastInteractionTime() < 50)
            return InteractionResult.FAIL;
        ((ITaterzenPlayer) player).setLastInteraction(lastAction);


        for(TaterzenProfession profession : this.professions.values()) {
            InteractionResult professionResult = profession.interactAt(player, pos, hand);
            if(professionResult != InteractionResult.PASS)
                return professionResult;
        }

        if(this.isEquipmentEditor(player)) {
            ItemStack stack = player.getItemInHand(hand).copy();

            if (stack.isEmpty() && player.isShiftKeyDown()) {
                this.dropCustomDeathLoot(DamageSource.playerAttack(player), 1, this.npcData.allowEquipmentDrops);
                for(EquipmentSlot slot : EquipmentSlot.values()) {
                    this.fakePlayer.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
            else if(player.isShiftKeyDown()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, stack);
                this.fakePlayer.setItemSlot(EquipmentSlot.MAINHAND, stack);
            }
            else {
                EquipmentSlot slot = getEquipmentSlotForItem(stack);
                this.setItemSlotAndDropWhenKilled(slot, stack);
                this.fakePlayer.setItemSlot(slot, stack);
            }
            // Updating behaviour (if npc had a sword and now has a bow, it won't
            // be able to attack otherwise.)
            this.setBehaviour(this.npcData.behaviour);
            return InteractionResult.PASS;
        } else if(
                player.getItemInHand(hand).getItem().equals(Items.POTATO) &&
                player.isShiftKeyDown() &&
                permissions$checkPermission(player.createCommandSourceStack(), "taterzens.npc.select", config.perms.npcCommandPermissionLevel)
        ) {
            // Select this taterzen
            ((ITaterzenEditor) player).selectNpc(this);

            player.sendMessage(
                    successText("taterzens.command.select", this.getName().getString()),
                    this.getUUID()
            );

            return InteractionResult.PASS;
        } else if (((ITaterzenEditor) player).getNpc() == this) {
            // Opens GUI for editing
            SimpleGui editorGUI = createCommandGui((ServerPlayer) player, null, npcNode, Collections.singletonList("npc"), false);
            editorGUI.open();
        }

        String playername = player.getGameProfile().getName();
        if(!this.npcData.commands.isEmpty()) {
            // Saving commands to a new list in order
            // to allow commands to be modified via commands
            // Functions are the easiest and cleanest way to do this
            ImmutableList<String> commands = ImmutableList.copyOf(this.npcData.commands);
            for(String cmd : commands) {
                if(cmd.contains("--clicker--")) {
                    cmd = cmd.replaceAll("--clicker--", playername);
                }
                this.server.getCommands().performCommand(this.createCommandSourceStack(), cmd);
            }
        }

        for(Triple<BungeeCompatibility, String, String> cmd : this.npcData.bungeeCommands) {
            String first = cmd.getLeft().getSubchannel();
            String middle = cmd.getMiddle();
            if(middle.equals("--clicker--"))
                middle = playername;

            String argument = cmd.getRight();
            if(argument.contains("--clicker--")) {
                argument = argument.replaceAll("--clicker--", playername);
            }

            // Sending command as CustomPayloadS2CPacket
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(first);
            out.writeUTF(middle);
            out.writeUTF(argument);

            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBytes(out.toByteArray());

            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(BungeeCompatibility.BUNGEE_CHANNEL, buf);
            ((ServerPlayer) player).connection.send(packet);
        }

        return this.interact(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        // Additional drop check
        if(this.npcData.allowEquipmentDrops)
            super.dropCustomDeathLoot(source, lootingMultiplier, allowDrops);
        else {
            for(EquipmentSlot slot : EquipmentSlot.values()) {
                this.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected boolean shouldDropLoot() {
        return this.npcData.allowEquipmentDrops;
    }

    @Override
    protected boolean shouldDropExperience() {
        return this.npcData.allowEquipmentDrops;
    }

    /**
     * Adds the message to taterzen's message list.
     * @param text message to add
     */
    public void addMessage(Component text) {
        this.npcData.messages.add(new Pair<>(text, config.messages.messageDelay));
    }

    /**
     * Adds the message to taterzen's message list.
     * @param text message to add
     * @param delay message delay, in ticks
     */
    public void addMessage(Component text, int delay) {
        this.npcData.messages.add(new Pair<>(text, delay));
    }

    /**
     * Edits the message from taterzen's message list at index.
     * @param index index of the message to edit
     * @param text new text message
     */
    public void editMessage(int index, Component text) {
        if(index >= 0 && index < this.npcData.messages.size())
            this.npcData.messages.set(index, new Pair<>(text, config.messages.messageDelay));
    }

    /**
     * Removes message at index.
     * @param index index of message to be removed.
     * @return removed message
     */
    public Component removeMessage(int index) {
        if(index < this.npcData.messages.size())
            return this.npcData.messages.remove(index).getFirst();
        return TextComponent.EMPTY;
    }

    /**
     * Sets message delay.
     *
     * @param index index of the message to change delay for.
     * @param delay new delay.
     */
    public void setMessageDelay(int index, int delay) {
        if(index < this.npcData.messages.size()) {
            Pair<Component, Integer> newMsg = this.npcData.messages.get(index).mapSecond(previous -> delay);
            this.npcData.messages.set(index, newMsg);
        }
    }

    public void clearMessages() {
        this.npcData.messages.clear();
    }

    /**
     * Gets {@link ArrayList} of {@link Pair}s of messages and their delays.
     * @return arraylist of pairs with texts and delays.
     */
    public ArrayList<Pair<Component, Integer>> getMessages() {
        return this.npcData.messages;
    }

    /**
     * Used for disabling pushing
     * @param entity colliding entity
     */
    @Override
    public void push(Entity entity) {
        if(this.npcData.pushable) {
            super.push(entity);
        }
    }

    /**
     * Used for disabling pushing
     * @param entity colliding entity
     */
    @Override
    protected void doPush(Entity entity) {
        if(this.npcData.pushable) {
            super.doPush(entity);
        }
    }

    /**
     * Sets the pushable flag
     * @param pushable whether Taterzen can be pushed
     */
    public void setPushable(boolean pushable) {
        this.npcData.pushable = pushable;
    }

    /**
     * Handles received hits.
     *
     * @param attacker entity that attacked NPC.
     * @return true if attack should be cancelled.
     */
    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        if(attacker instanceof Player && this.isEquipmentEditor((Player) attacker)) {
            ItemStack main = this.getMainHandItem();
            this.setItemInHand(InteractionHand.MAIN_HAND, this.getOffhandItem());
            this.setItemInHand(InteractionHand.OFF_HAND, main);
            return true;
        }
        for(TaterzenProfession profession : this.professions.values()) {
            if(profession.handleAttack(attacker))
                return true;
        }
        return this.isInvulnerable();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return this.isRemoved() || this.isInvulnerable() && damageSource != DamageSource.OUT_OF_WORLD;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return !this.isLeashed() && this.npcData.leashable;
    }

    /**
     * Sets whether Taterzen can be leashed.
     * @param leashable Taterzen leashability.
     */
    public void setLeashable(boolean leashable) {
        this.npcData.leashable = leashable;
    }

    @Override
    public void setLeashedTo(Entity entityIn, boolean sendAttachNotification) {
        super.setLeashedTo(entityIn, sendAttachNotification);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
    }

    /**
     * Handles death of NPC.
     * @param source damage source responsible for death.
     */
    @Override
    public void die(DamageSource source) {
        Pose pose = this.getPose();
        super.die(source);
        if (this.respawnPosition != null) {
            // Taterzen should be respawned instead
            this.getLevel().broadcastEntityEvent(this, EntityEvent.DEATH);
            this.dead = false;
            this.setHealth(this.getMaxHealth());
            this.setDeltaMovement(0.0D, 0.1D, 0.0D);
            this.setPos(this.respawnPosition);
            this.setPose(pose);
        } else {
            TATERZEN_NPCS.remove(this);

            for(TaterzenProfession profession : this.professions.values()) {
                profession.onRemove();
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        TATERZEN_NPCS.remove(this);

        for(TaterzenProfession profession : this.professions.values()) {
            profession.onRemove();
        }
    }

    /**
     * Sets Taterzen's {@link NPCData.Behaviour}.
     * @param level behaviour level
     */
    public void setBehaviour(NPCData.Behaviour level) {
        this.npcData.behaviour = level;

        this.goalSelector.removeGoal(reachMeleeAttackGoal);
        this.goalSelector.removeGoal(projectileAttackGoal);
        this.goalSelector.removeGoal(attackMonstersGoal);

        this.targetSelector.removeGoal(followTargetGoal);
        this.targetSelector.removeGoal(revengeGoal);
        this.targetSelector.removeGoal(followMonstersGoal);

        for(TaterzenProfession profession : this.professions.values()) {
            profession.onBehaviourSet(level);
        }


        if(this.npcData.movement == NPCData.Movement.NONE)
            this.setMovement(NPCData.Movement.TICK);

        switch (level) {
            case DEFENSIVE -> {
                this.targetSelector.addGoal(2, revengeGoal);
                this.setAttackGoal();
            }
            case FRIENDLY -> {
                this.targetSelector.addGoal(2, revengeGoal);
                this.targetSelector.addGoal(3, followMonstersGoal);
                this.goalSelector.addGoal(3, attackMonstersGoal);
            }
            case HOSTILE -> {
                this.targetSelector.addGoal(2, revengeGoal);
                this.targetSelector.addGoal(3, followTargetGoal);
                this.setAttackGoal();
            }
            default -> {
            }
        }
    }

    /**
     * Sets proper attack goal, based on hand item stack.
     */
    private void setAttackGoal() {
        ItemStack mainHandStack = this.getMainHandItem();
        ItemStack offHandStack = this.getOffhandItem();
        if(mainHandStack.getItem() instanceof ProjectileWeaponItem || offHandStack.getItem() instanceof ProjectileWeaponItem) {
            this.goalSelector.addGoal(3, projectileAttackGoal);
        } else {
            this.goalSelector.addGoal(3, reachMeleeAttackGoal);
        }
    }

    /**
     * Gets the Taterzen's target selector.
     * @return target selector of Taterzen.
     */
    public GoalSelector getTargetSelector() {
        return this.targetSelector;
    }

    /**
     * Gets the Taterzen's goal selector.
     * @return goal selector of Taterzen.
     */
    public GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem weapon) {
        return this.npcData.behaviour != NPCData.Behaviour.PASSIVE;
    }

    @Override
    public void setChargingCrossbow(boolean charging) {
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity target, ItemStack crossbow, Projectile projectile, float multiShotSpray) {
        // Crossbow attack
        this.shootProjectile(target, projectile, multiShotSpray);
    }

    @Override
    public void onCrossbowAttackPerformed() {
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {

        for(TaterzenProfession profession : this.professions.values()) {
            if(profession.cancelRangedAttack(target))
                return;
        }

        // Ranged attack
        ItemStack arrowType = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW)));
        if(arrowType.isEmpty())
            arrowType = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.CROSSBOW)));

        AbstractArrow projectile = ProjectileUtil.getMobArrow(this, arrowType.copy(), pullProgress);

        this.shootProjectile(target, projectile, 0.0F);


    }

    private void shootProjectile(LivingEntity target, Projectile projectile, float multishotSpray) {
        double deltaX = target.getX() - this.getX();
        double y = target.getY(0.3333333333333333D) - projectile.getY();
        double deltaZ = target.getZ() - this.getZ();
        double planeDistance = Mth.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));
        Vector3f launchVelocity = this.getProjectileShotVector(this, new Vec3(deltaX, y + planeDistance * 0.2D, deltaZ), multishotSpray);

        projectile.shoot(launchVelocity.x(), launchVelocity.y(), launchVelocity.z(), 1.6F, 0);

        this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 0.125F);
        this.level.addFreshEntity(projectile);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        for(TaterzenProfession profession : this.professions.values()) {
            if(profession.cancelMeleeAttack(target))
                return false;
        }
        return super.doHurtTarget(target);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(config.defaults.ambientSounds.isEmpty() || !this.npcData.allowSounds)
            return null;

        int rnd = this.random.nextInt(config.defaults.ambientSounds.size());
        ResourceLocation sound = new ResourceLocation(config.defaults.ambientSounds.get(rnd));

        return new SoundEvent(sound);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if(config.defaults.hurtSounds.isEmpty() || !this.npcData.allowSounds)
            return null;

        int rnd = this.random.nextInt(config.defaults.hurtSounds.size());
        ResourceLocation sound = new ResourceLocation(config.defaults.hurtSounds.get(rnd));

        return new SoundEvent(sound);
    }

    @Override
    protected SoundEvent getDeathSound() {
        if(config.defaults.deathSounds.isEmpty() || !this.npcData.allowSounds)
            return null;

        int rnd = this.random.nextInt(config.defaults.deathSounds.size());
        ResourceLocation sound = new ResourceLocation(config.defaults.deathSounds.get(rnd));

        return new SoundEvent(sound);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader world) {
        return 0.0F;
    }

    @Override
    protected Component getTypeName() {
        return new TextComponent("-" + config.defaults.name + "-");
    }

    public Player getFakePlayer() {
        return this.fakePlayer;
    }

    /**
     * Toggles whether Taterzen will drop its equipment.
     * @param drop drop rule
     */
    public void allowEquipmentDrops(boolean drop) {
        this.npcData.allowEquipmentDrops = drop;
    }

    /**
     * Adds {@link TaterzenProfession} to Taterzen.
     * Profession must be registered with {@link org.samo_lego.taterzens.api.TaterzensAPI#registerProfession(ResourceLocation, TaterzenProfession)}.
     * @param professionId ResourceLocation of the profession
     */
    public void addProfession(ResourceLocation professionId) {
        if(PROFESSION_TYPES.containsKey(professionId)) {
            this.addProfession(professionId, PROFESSION_TYPES.get(professionId).create(this));
        } else
            Taterzens.LOGGER.warn("Trying to add unknown profession {} to taterzen {}.", professionId, this.getName().getString());
    }

    /**
     * Adds {@link TaterzenProfession} to Taterzen.
     * @param professionId ResourceLocation of the profession
     * @param profession profession object (implementing {@link TaterzenProfession})
     */
    public void addProfession(ResourceLocation professionId, TaterzenProfession profession) {
        this.professions.put(professionId, profession);
    }

    /**
     * GetsTaterzen's professions.
     * @return all professions ids of Taterzen's professions.
     */
    public Collection<ResourceLocation> getProfessionIds() {
        return this.professions.keySet();
    }

    /**
     * Removes Taterzen's profession.
     * @param professionId id of the profession that is in Taterzen's profession map.
     */
    public void removeProfession(ResourceLocation professionId) {
        this.professions.remove(professionId);
    }

    /**
     * Gets Taterzen's profession.
     * @param professionId id of the profession that is in Taterzen's profession map.
     */
    @Nullable
    public TaterzenProfession getProfession(ResourceLocation professionId) {
        return this.professions.getOrDefault(professionId, null);
    }

    @Override
    public boolean canPickUpLoot() {
        return true;
    }

    /**
     * Manages item pickup.
     * @param item item to pick up.
     */
    @Override
    protected void pickUpItem(ItemEntity item) {
        // Profession event
        ItemStack stack = item.getItem();
        for(TaterzenProfession profession : this.professions.values()) {
            if(profession.tryPickupItem(item) || profession.tryPickupItem(stack)) {
                this.onItemPickup(item);
                this.take(item, stack.getCount());
                stack.setCount(0);
                item.discard();
                return;
            }
        }
    }

    /**
     * Makes Taterzen interact with block at given position.
     * It doesn't work if given position is too far away (>4 blocks)
     * @param pos position of block to interact with.
     * @return true if interaction was successfull, otherwise false.
     */
    public boolean interact(BlockPos pos) {
        if(this.position().distanceTo(Vec3.atCenterOf(pos)) < 4.0D && !this.level.isClientSide()) {
            this.lookAt(pos);
            this.swing(InteractionHand.MAIN_HAND);
            this.level.getBlockState(pos).use(this.level, this.fakePlayer, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, false));
            this.getMainHandItem().use(this.level, this.fakePlayer, InteractionHand.MAIN_HAND);
            return true;
        }
        return false;
    }

    /**
     * Makes Taterzen look at given block position.
     * @param target target block to look at.
     */
    public void lookAt(BlockPos target) {
        Vec3 vec3d = this.position();
        double d = target.getX() - vec3d.x;
        double e = target.getY() - vec3d.y;
        double f = target.getZ() - vec3d.z;
        double g = Math.sqrt(d * d + f * f);
        this.setXRot(Mth.wrapDegrees((float)(-(Mth.atan2(e, g) * 57.2957763671875D))));
        this.setYBodyRot(Mth.wrapDegrees((float)(Mth.atan2(f, d) * 57.2957763671875D) - 90.0F));
        this.setYHeadRot(this.getYHeadRot());
    }

    /**
     * Sets whether Taterzen can perform jumps when in
     * proximity of target that it is attacking.
     * @param jumpWhileAttacking whether to jump during attacks.
     */
    public void setPerformAttackJumps(boolean jumpWhileAttacking) {
        this.npcData.jumpWhileAttacking = jumpWhileAttacking;
    }

    /**
     * Sets the target type to follow.
     * Changes movement to {@link NPCData.Movement#PATH} as well.
     * @param followType type of target to follow
     */
    public void setFollowType(NPCData.FollowTypes followType) {
        if(followType != NPCData.FollowTypes.NONE)
            this.setMovement(NPCData.Movement.TICK);
        this.npcData.follow.type = followType;

        switch (followType) {
            case MOBS -> this.goalSelector.addGoal(4, trackLivingGoal);
            case PLAYERS -> this.goalSelector.addGoal(4, trackPlayersGoal);
            case UUID -> this.goalSelector.addGoal(4, trackUuidGoal);
            default -> {
            }
        }
    }

    /**
     * Gets follow type for taterzen.
     * @return follow type
     */
    public NPCData.FollowTypes getFollowType() {
        return this.npcData.follow.type;
    }

    /**
     * Sets the target uuid to follow.
     * @param followUuid uuid of target to follow
     */
    public void setFollowUuid(@Nullable UUID followUuid) {
        this.npcData.follow.targetUuid = followUuid;
    }

    /**
     * Gets the UUID of the entity that taterzen is following.
     * @return entity UUID if following, otherwise null.
     */
    @Nullable
    public UUID getFollowUuid() {
        return this.npcData.follow.targetUuid;
    }

    /**
     * Whether this Taterzen should make sound.
     * @param allowSounds whether to allow sounds or not.
     */
    public void setAllowSounds(boolean allowSounds) {
        this.npcData.allowSounds = allowSounds;
    }

    /**
     * Sets which skin layers should be shown to clients
     * @param skinLayers byte of skin layers, see wiki.wg for more info.
     */
    public void setSkinLayers(Byte skinLayers) {
        this.fakePlayer.getEntityData().set(getPLAYER_MODE_CUSTOMISATION(), skinLayers);
    }

    /**
     * Adds "bungee" command. It's not a standard command, executed by the server,
     * but sent as {@link net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket} to player and caught by proxy.
     *
     * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#wikiPage">Spigot thread</a> on message channels.
     * @see <a href="https://github.com/VelocityPowered/Velocity/blob/65db0fad6a221205ec001f1f68a032215da402d6/proxy/src/main/java/com/velocitypowered/proxy/connection/backend/BungeeCordMessageResponder.java#L297">Proxy implementation</a> on GitHub.
     *
     * @param bungeeCommand bungee command to add, see {@link BungeeCompatibility} for supported commands.
     * @param playername player to use when executing the command.
     * @param argument argument for command above.
     *
     * @return true if command was added successfully (if enabled in config), otherwise false
     */
    public boolean addBungeeCommand(BungeeCompatibility bungeeCommand, String playername, String argument) {
        if(config.bungee.enableCommands) {
            Triple<BungeeCompatibility, String, String> command = new BungeeCompatibility.BungeeCommand(bungeeCommand, playername, argument);
            this.npcData.bungeeCommands.add(command);
        }
        return config.bungee.enableCommands;
    }

    /**
     * Gets all "bungee" commands that Taterzen will execute when right-clicked upon.
     * @return arraylist od triples that are constructed into bungee command
     */
    public ArrayList<Triple<BungeeCompatibility, String, String>> getBungeeCommands() {
        return new ArrayList<>(this.npcData.bungeeCommands);
    }

    /**
     * Sets the respawn position for taterzen. Can be null to disable respawning.
     * @param respawnPos new respawn position.
     */
    public void setRespawnPos(@Nullable Vec3 respawnPos) {
        this.respawnPosition = respawnPos;
    }
}
