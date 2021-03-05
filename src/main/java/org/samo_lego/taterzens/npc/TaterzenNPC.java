package org.samo_lego.taterzens.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.interfaces.TaterzenPlayer;
import org.samo_lego.taterzens.mixin.accessors.EntityTrackerEntryAccessor;
import org.samo_lego.taterzens.mixin.accessors.PlayerListS2CPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.ThreadedAnvilChunkStorageAccessor;
import org.samo_lego.taterzens.npc.ai.goal.DirectPathGoal;
import org.samo_lego.taterzens.npc.ai.goal.ReachMeleeAttackGoal;
import org.samo_lego.taterzens.util.TextUtil;

import java.util.*;
import java.util.stream.Collectors;

import static net.minecraft.entity.EntityType.loadEntityWithPassengers;
import static net.minecraft.entity.player.PlayerEntity.PLAYER_MODEL_PARTS;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.ADD_PLAYER;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.REMOVE_PLAYER;
import static org.samo_lego.taterzens.Taterzens.TATERZEN_NPCS;
import static org.samo_lego.taterzens.Taterzens.config;

/**
 * The NPC itself.
 */
public class TaterzenNPC extends HostileEntity implements CrossbowUser, RangedAttackMob {

    /**
     * Data of the NPC.
     */
    private final NPCData npcData = new NPCData();

    private final PlayerManager playerManager;
    private final MinecraftServer server;
    private GameProfile gameProfile;

    // Goals
    private final LookAtEntityGoal lookPlayerGoal = new LookAtEntityGoal(this, PlayerEntity.class, 8.0F);
    private final LookAroundGoal lookAroundGoal = new LookAroundGoal(this);

    private final FollowTargetGoal<PlayerEntity> followTargetGoal = new FollowTargetGoal<>(this, PlayerEntity.class, false, true);
    private final WanderAroundGoal wanderAroundFarGoal = new WanderAroundGoal(this, 0.4F, 30);
    private final GoToWalkTargetGoal pathGoal = new GoToWalkTargetGoal(this, 0.4F);
    private final DirectPathGoal directPathGoal = new DirectPathGoal(this, 0.4F);

    private final CrossbowAttackGoal<TaterzenNPC> crossbowAttackGoal = new CrossbowAttackGoal<>(this, 1.0D, 40.0F);
    private final BowAttackGoal<TaterzenNPC> bowAttackGoal = new BowAttackGoal<>(this, 1.0D, 20, 40.0F);
    private final ReachMeleeAttackGoal reachMeleeAttackGoal = new ReachMeleeAttackGoal(this, 1.2D, false);
    private short ticks = 0;

    /**
     * Creates a TaterzenNPC.
     * You'd probably want to use
     * {@link TaterzenNPC#TaterzenNPC(ServerWorld, String, Vec3d, float[])} or {@link TaterzenNPC#TaterzenNPC(ServerPlayerEntity, String)} or
     * {@link TaterzenNPC#TaterzenNPC(ServerPlayerEntity, String)} instead, as this one doesn't set the position and custom name.
     *
     * @param entityType
     * @param world
     */
    public TaterzenNPC(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.stepHeight = 0.6F;
        this.setCanPickUpLoot(false);
        this.setCustomNameVisible(true);
        this.setCustomName(this.getName());
        this.setInvulnerable(true);
        this.setPersistent();
        this.experiencePoints = 0;
        this.setMovementSpeed(0.4F);
        ((MobNavigation) this.getNavigation()).setCanPathThroughDoors(true);

        this.gameProfile = new GameProfile(this.getUuid(), this.getName().asString());
        this.server = world.getServer();
        this.playerManager = server.getPlayerManager();

        TATERZEN_NPCS.add(this);
    }

    public TaterzenNPC(ServerWorld world, String displayName, Vec3d pos, float[] rotations) {
        this(Taterzens.TATERZEN, world);

        this.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), rotations[1], rotations[2]);
        this.setHeadYaw(rotations[0]);
        this.setCustomName(new LiteralText(displayName));
        this.applySkin(SkullBlockEntity.loadProperties(this.gameProfile));
    }

    public TaterzenNPC(ServerPlayerEntity owner, String displayName) {
        this(owner.getServerWorld(), displayName, owner.getPos(), new float[]{owner.headYaw, owner.yaw, owner.pitch});
    }

    /**
     * Sets the command to be executed on right - click
     * @param command command to execute
     */
    public void setCommand(String command) {
        this.npcData.command = command;
    }

    /**
     * Sets {@link org.samo_lego.taterzens.npc.NPCData.Movement movement type}
     * and initialises the goals.
     *
     * @param movement movement type
     */
    public void setMovement(NPCData.Movement movement) {
        this.npcData.movement = movement;
        this.goalSelector.remove(this.wanderAroundFarGoal);
        this.goalSelector.remove(this.directPathGoal);
        this.goalSelector.remove(this.pathGoal);
        this.goalSelector.remove(this.lookPlayerGoal);
        this.goalSelector.remove(this.lookAroundGoal);

        if(movement != NPCData.Movement.NONE && movement != NPCData.Movement.FORCED_LOOK) {
            if(movement == NPCData.Movement.FORCED_PATH) {
                this.goalSelector.add(3, directPathGoal);
            } else {
                this.goalSelector.add(5, lookPlayerGoal);
                this.goalSelector.add(6, lookAroundGoal);
                if(movement == NPCData.Movement.PATH)
                    this.goalSelector.add(3, pathGoal);
                else if(movement == NPCData.Movement.FREE)
                    this.goalSelector.add(3, wanderAroundFarGoal);
            }
        }
    }

    /**
     * Adds block position as a node in path of Taterzen.
     * @param blockPos position to add.
     */
    public void addPathTarget(BlockPos blockPos) {
        this.npcData.pathTargets.add(blockPos);
        this.setPositionTarget(this.npcData.pathTargets.get(0), 1);
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
    public void tickMovement() {
        if(++this.ticks >= 20)
            this.ticks = 0;
        if(this.npcData.equipmentEditor != null)
            return;
        if(this.npcData.movement == NPCData.Movement.FORCED_LOOK) {
            if(this.ticks % 5 == 0) {
                Box box = this.getBoundingBox().expand(4.0D);
                this.world.getEntityCollisions(this, box, entity -> {
                    if(entity instanceof ServerPlayerEntity) {
                        this.lookAtEntity(entity, 60.0F, 60.0F);
                        this.setHeadYaw(this.yaw);
                        return true;
                    }
                    return false;
                });
            }
        } else if(this.npcData.movement != NPCData.Movement.NONE) {
            this.yaw = this.headYaw; // Rotates body as well
            if((this.npcData.movement == NPCData.Movement.FORCED_PATH && !this.npcData.pathTargets.isEmpty())) {
                if(this.getPositionTarget().getSquaredDistance(this.getPos(), false) < 5.0D) {
                    if(++this.npcData.currentMoveTarget >= this.npcData.pathTargets.size())
                        this.npcData.currentMoveTarget = 0;
                    // New target
                    this.setPositionTarget(this.npcData.pathTargets.get(this.npcData.currentMoveTarget), 2);
                }
            } else if(this.npcData.movement == NPCData.Movement.PATH && !this.pathGoal.shouldContinue() && !this.npcData.pathTargets.isEmpty()) {
                if(this.npcData.pathTargets.get(this.npcData.currentMoveTarget).getSquaredDistance(this.getPos(), false) < 5.0D) {
                    if(++this.npcData.currentMoveTarget >= this.npcData.pathTargets.size())
                        this.npcData.currentMoveTarget = 0;
                    // New target
                    this.setPositionTarget(this.npcData.pathTargets.get(this.npcData.currentMoveTarget), 1);
                }
            }
            super.tickMovement();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.npcData.messages.isEmpty()) {
            Box box = this.getBoundingBox().offset(2.0D, 1.0D, 2.0D);
            this.world.getEntityCollisions(this, box, entity -> {
                if(entity instanceof ServerPlayerEntity) {
                    if(this.npcData.messages.get(this.npcData.currentMessage).getSecond() < ((TaterzenPlayer) entity).ticksSinceLastMessage()) {
                        if(++this.npcData.currentMessage >= this.npcData.messages.size())
                            this.npcData.currentMessage = 0;
                        entity.sendSystemMessage(
                                this.getName().copy().append(" -> you: ").append(this.npcData.messages.get(this.npcData.currentMessage).getFirst()),
                                this.uuid
                        );
                        // Resetting message counter
                        ((TaterzenPlayer) entity).resetMessageTicks();
                    }
                    return true;
                }
                return false;
            });
        }
    }
    /**
     * Gets the entity type of the NPC used on client.
     *
     * @return EntityType of the NPC.
     */
    public EntityType<?> getFakeType() {
        return this.npcData.entityType;
    }
    /**
     * If fake type is an instance of {@link LivingEntity}.
     * Used for packet sending, as living entities use different ones.
     *
     * @return true if so, otherwise false.
     */
    public boolean isFakeTypeAlive() {
        return this.npcData.fakeTypeAlive;
    }

    /**
     * Changes type of NPC, shown on client
     * @param entityId identifier of the entity
     */
    public void changeType(Identifier entityId) {
        if(entityId.getPath().equals("player")) {
            //Minecraft has built-in protection against creating players :(
            this.changeType(EntityType.PLAYER, true);
        } else {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", entityId.toString());
            Optional<Entity> optionalEntity = Optional.ofNullable(loadEntityWithPassengers(tag, this.world, (entity) -> entity));
            optionalEntity.ifPresent(entity -> this.changeType(entity.getType(), entity instanceof LivingEntity));
        }
    }

    /**
     * Changes type of NPC, shown on client.
     * fakeTypeAlive is required because LivingEntities use different packets.
     *
     * @param fakeType fake entity type
     * @param fakeTypeAlive whether fake type is an instance of living entity
     */
    public void changeType(EntityType<?> fakeType, boolean fakeTypeAlive) {
        this.npcData.entityType = fakeType;
        this.npcData.fakeTypeAlive = fakeTypeAlive;
        playerManager.sendToDimension(new EntitiesDestroyS2CPacket(this.getEntityId()), this.world.getRegistryKey());
        playerManager.sendToDimension(new MobSpawnS2CPacket(this), this.world.getRegistryKey()); // We'll send player packet in ServerPlayNetworkHandlerMixin if needed
        playerManager.sendToDimension(new EntityTrackerUpdateS2CPacket(this.getEntityId(), this.getDataTracker(), true), this.world.getRegistryKey()); // todo -> skin
        playerManager.sendToDimension(new EntityEquipmentUpdateS2CPacket(this.getEntityId(), this.getEquipment()), this.world.getRegistryKey()); // Reload equipment
    }

    /**
     * Gets equipment as list of {@link Pair Pairs}.
     * @return equipment list of pairs.
     */
    private List<Pair<EquipmentSlot, ItemStack>> getEquipment() {
        return Arrays.stream(EquipmentSlot.values()).map(slot -> new Pair<>(slot, this.getEquippedStack(slot))).collect(Collectors.toList());
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    /**
     * Sets the custom name
     * @param name new name to be set.
     */
    @Override
    public void setCustomName(Text name) {
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
        this.gameProfile = new GameProfile(this.getUuid(), profileName);
        if(this.getFakeType() == EntityType.PLAYER && skin != null) {
            this.setSkinFromTag(skin);
            this.sendProfileUpdates();
        }
    }

    /**
     * Updates Taterzen's {@link GameProfile} for others.
     */
    public void sendProfileUpdates() {
        PlayerListS2CPacket packet = new PlayerListS2CPacket();
        //noinspection ConstantConditions
        PlayerListS2CPacketAccessor accessor = (PlayerListS2CPacketAccessor) packet;
        accessor.setEntries(Collections.singletonList(packet.new Entry(this.gameProfile, 0, GameMode.SURVIVAL, this.getName())));

        accessor.setAction(REMOVE_PLAYER);
        playerManager.sendToAll(packet);
        accessor.setAction(ADD_PLAYER);
        playerManager.sendToAll(packet);

        ServerChunkManager manager = (ServerChunkManager) this.world.getChunkManager();
        ThreadedAnvilChunkStorage storage = manager.threadedAnvilChunkStorage;
        EntityTrackerEntryAccessor trackerEntry = ((ThreadedAnvilChunkStorageAccessor) storage).getEntityTrackers().get(this.getEntityId());
        if(trackerEntry != null)
            trackerEntry.getTrackingPlayers().forEach(tracking -> trackerEntry.getEntry().startTracking(tracking));
    }


    /**
     * Applies skin from {@link GameProfile}.
     *
     * @param texturesProfile GameProfile containing textures.
     */
    public void applySkin(GameProfile texturesProfile) {
        if(this.npcData.entityType != EntityType.PLAYER)
            return;

        // Setting new skin
        setSkinFromTag(writeSkinToTag(texturesProfile));

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
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        CompoundTag npcTag = tag.getCompound("TaterzenNPCTag");

        this.npcData.fakeTypeAlive = npcTag.getBoolean("fakeTypeAlive");
        this.npcData.hostile = npcTag.getBoolean("hostile");
        this.npcData.movement = NPCData.Movement.valueOf(npcTag.getString("movement"));

        this.npcData.leashable = npcTag.getBoolean("leashable");
        this.npcData.pushable = npcTag.getBoolean("pushable");

        Identifier identifier = new Identifier(npcTag.getString("entityType"));
        this.npcData.entityType = Registry.ENTITY_TYPE.get(identifier);

        this.npcData.command = npcTag.getString("command");

        ListTag pathTargets = (ListTag) npcTag.get("PathTargets");
        if(pathTargets != null) {
            if(pathTargets.size() > 0) {
                pathTargets.forEach(posTag -> {
                    if(posTag instanceof CompoundTag) {
                        CompoundTag pos = (CompoundTag) posTag;
                        BlockPos target = new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"));
                        this.npcData.pathTargets.add(target);
                    }
                });
                this.setPositionTarget(this.npcData.pathTargets.get(0), 1);
            }
        }

        ListTag messages = (ListTag) npcTag.get("Messages");
        if(messages != null) {
            if(messages.size() > 0) {
                messages.forEach(msgTag -> {
                    CompoundTag msgCompound = (CompoundTag) msgTag;
                    this.npcData.messages.add(new Pair<>(TextUtil.fromTag(msgCompound.get("Message")), msgCompound.getInt("Delay")));
                });
            }
        }


        this.gameProfile = new GameProfile(this.getUuid(), this.getDisplayName().asString());

        // Skin is cached
        CompoundTag skinTag = npcTag.getCompound("skin");
        this.setSkinFromTag(skinTag);

        // Initialises movement
        this.setMovement(this.npcData.movement);
    }

    /**
     * Saves Taterzen to {@link CompoundTag tag}.
     *
     * @param tag tag to save Taterzen to.
     */
    @Override
    public void writeCustomDataToTag(CompoundTag tag) {
        super.writeCustomDataToTag(tag);

        CompoundTag npcTag = new CompoundTag();

        npcTag.putBoolean("fakeTypeAlive", this.npcData.fakeTypeAlive);
        npcTag.putBoolean("hostile", this.npcData.hostile);

        npcTag.putString("movement", this.npcData.movement.toString());

        npcTag.putBoolean("leashable", this.npcData.leashable);
        npcTag.putBoolean("pushable", this.npcData.pushable);
        npcTag.putString("command", this.npcData.command);

        npcTag.putString("entityType", Registry.ENTITY_TYPE.getId(this.npcData.entityType).toString());

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
            msg.put("Message", TextUtil.toTag(pair.getFirst()));
            msg.putInt("Delay", pair.getSecond());
            messages.add(msg);
        });
        npcTag.put("Messages", messages);

        tag.put("TaterzenNPCTag", npcTag);

        TATERZEN_NPCS.remove(this);
    }

    /**
     * Sets player as equipment editor.
     * @param player player that will be marked as equipment editor.
     */
    public void setEquipmentEditor(@Nullable PlayerEntity player) {
        this.npcData.equipmentEditor = player;
    }

    /**
     * Sets player as equipment editor.
     * @param player player to check.
     * @return true if player is equipment editor of the NPC, otherwise false.
     */
    public boolean isEquipmentEditor(@NotNull PlayerEntity player) {
        return player.equals(this.npcData.equipmentEditor);
    }

    /**
     * Handles interaction (right clicking on the NPC).
     * @param player player interacting with NPC
     * @param pos interaction pos
     * @param hand player's interacting hand
     * @return {@link ActionResult#PASS} if NPC has a right click action, otherwise {@link ActionResult#FAIL}
     */
    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d pos, Hand hand) {
        long lastAction = ((ServerPlayerEntity) player).getLastActionTime();
        ActionResult result = ActionResult.FAIL;

        // As weird as it sounds, this gets triggered twice, first time with the item stack player is holding
        // then with "air" if fake type is player / armor stand
        if(lastAction - ((TaterzenPlayer) player).getLastInteractionTime() < 50)
            return result;

        if(this.isEquipmentEditor(player)) {
            ItemStack stack = player.getStackInHand(hand);

            if (stack.isEmpty() && player.isSneaking()) {
                this.dropEquipment(DamageSource.player(player), 1, true);
            }
            else if(player.isSneaking()) {
                this.equipStack(EquipmentSlot.MAINHAND, stack);
            }
            else {
                this.equipLootStack(getPreferredEquipmentSlot(stack), stack);
            }
            result = ActionResult.PASS;
        }
        else if(!this.npcData.command.isEmpty()) {
            this.server.getCommandManager().execute(player.getCommandSource(), this.npcData.command);
            result = ActionResult.PASS;
        }

        ((TaterzenPlayer) player).setLastInteraction(lastAction);
        return result;
    }

    public void addMessage(Text text) {
        this.npcData.messages.add(new Pair<>(text, config.defaults.messageDelay));
    }

    public void setMessageDelay(int delay) {
        if(!this.npcData.messages.isEmpty()) {
            this.npcData.messages.get(this.npcData.currentMessage).mapSecond(previous -> delay);
        }
    }

    public void clearMessages() {
        this.npcData.messages = new ArrayList<>();
        this.npcData.currentMessage = 0;
    }

    /**
     * Used for disabling pushing
     * @param entity colliding entity
     */
    @Override
    public void pushAwayFrom(Entity entity) {
        if(this.npcData.pushable) {
            super.pushAwayFrom(entity);
        }
    }

    /**
     * Used for disabling pushing
     * @param entity colliding entity
     */
    @Override
    protected void pushAway(Entity entity) {
        if(this.npcData.pushable) {
            super.pushAway(entity);
        }
    }

    /**
     * Handles received hits.
     *
     * @param attacker entity that attacked NPC.
     * @return true if attack should be cancelled.
     */
    @Override
    public boolean handleAttack(Entity attacker) {
        if(attacker instanceof PlayerEntity && this.isEquipmentEditor((PlayerEntity) attacker)) {
            ItemStack main = this.getMainHandStack();
            this.setStackInHand(Hand.MAIN_HAND, this.getOffHandStack());
            this.setStackInHand(Hand.OFF_HAND, main);
            return true;
        }
        return this.npcData.movement == NPCData.Movement.LOOK || this.npcData.movement == NPCData.Movement.NONE || super.handleAttack(attacker);
    }


    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(PLAYER_MODEL_PARTS, (byte) 0x7f);
    }

    @Override
    protected boolean isAffectedByDaylight() {
        return false;
    }

    @Override
    protected boolean isDisallowedInPeaceful() {
        return false;
    }

    @Override
    public boolean canBeLeashedBy(PlayerEntity player) {
        return !this.isLeashed() && this.npcData.leashable;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new LongDoorInteractGoal(this, true));
    }

    /**
     * Handles death of NPC.
     * @param source damage source responsible for death.
     */
    @Override
    public void onDeath(DamageSource source) {
        if(this.npcData.entityType == EntityType.PLAYER) {
            PlayerListS2CPacket playerListS2CPacket = new PlayerListS2CPacket();
            ((PlayerListS2CPacketAccessor) playerListS2CPacket).setAction(REMOVE_PLAYER);
            ((PlayerListS2CPacketAccessor) playerListS2CPacket).setEntries(Collections.singletonList(playerListS2CPacket.new Entry(this.gameProfile, 0, GameMode.SURVIVAL, new LiteralText(this.getName().asString()))));
            this.playerManager.sendToAll(playerListS2CPacket);
        }
        TATERZEN_NPCS.remove(this);
    }

    @Override
    public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
        return this.npcData.hostile;
    }

    @Override
    protected void updateDespawnCounter() {
    }
    @Override
    public void setCharging(boolean charging) {

    }

    @Override
    public void shoot(LivingEntity target, ItemStack crossbow, ProjectileEntity projectile, float multiShotSpray) {
        //this.attack(target, 4.0F);
    }

    @Override
    public void postShoot() {
    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PLAYER_BREATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return 0.0F;
    }

    @Override
    protected Text getDefaultName() {
        return new LiteralText(config.defaults.name);
    }
}
