package org.samo_lego.taterzens.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.mixin.accessors.EntityTrackerEntryAccessor;
import org.samo_lego.taterzens.mixin.accessors.PlayerListS2CPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.ThreadedAnvilChunkStorageAccessor;

import java.util.Collections;
import java.util.NoSuchElementException;

import static net.minecraft.entity.player.PlayerEntity.PLAYER_MODEL_PARTS;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.ADD_PLAYER;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.REMOVE_PLAYER;
import static org.samo_lego.taterzens.Taterzens.TATERZEN_NPCS;

public class TaterzenNPC extends HostileEntity implements CrossbowUser, RangedAttackMob {

    private final NPCData npcData = new NPCData();
    private PlayerManager playerManager;
    private MinecraftServer server;
    private GameProfile gameProfile;
    private final LookAtEntityGoal lookAtPlayerGoal = new LookAtEntityGoal(this, PlayerEntity.class, 8.0F);
    private final FollowTargetGoal<PlayerEntity> followTargetGoal = new FollowTargetGoal<>(this, PlayerEntity.class, false, true);
    private final WanderAroundGoal wanderAroundFarGoal = new WanderAroundGoal(this, 0.4F, 30);

    private final CrossbowAttackGoal<TaterzenNPC> crossbowAttackGoal = new CrossbowAttackGoal<>(this, 1.0D, 40.0F);
    private final BowAttackGoal<TaterzenNPC> bowAttackGoal = new BowAttackGoal<>(this, 1.0D, 20, 40.0F);
    private final MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(this, 1.2D, false) {
        public void stop() {
            super.stop();
            TaterzenNPC.this.setAttacking(false);
        }

        public void start() {
            super.start();
            TaterzenNPC.this.setAttacking(true);
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity livingEntity = this.mob.getTarget();
            if(livingEntity == null || !livingEntity.isAlive()) {
                return false;
            }
            return !(livingEntity instanceof PlayerEntity) || (!livingEntity.isSpectator() && !((PlayerEntity) livingEntity).isCreative());
        }

        @Override
        protected double getSquaredMaxAttackDistance(LivingEntity entity) {
            return 12.25D;
        }
    };

    /**
     * Creates a TaterzenNPC.
     * Internal use only. Use {@link TaterzenNPC#TaterzenNPC(MinecraftServer, ServerWorld, String, Vec3d, float[])} or {@link TaterzenNPC#TaterzenNPC(ServerPlayerEntity, String)}
     *
     * @param entityType
     * @param world
     */
    public TaterzenNPC(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.stepHeight = 0.6F;
        this.setCanPickUpLoot(false);
        this.setCustomNameVisible(true);
        this.setInvulnerable(true);
        this.setPersistent();
        this.experiencePoints = 0;
        this.setMovementSpeed(0.4F);
        TATERZEN_NPCS.add(this);
    }

    public TaterzenNPC(MinecraftServer server, ServerWorld world, String displayName, Vec3d pos, float[] rotations) {
        this(Taterzens.TATERZEN, world);
        this.gameProfile = new GameProfile(this.getUuid(), displayName);
        this.applySkin(SkullBlockEntity.loadProperties(this.gameProfile), false);
        this.server = server;
        this.playerManager = server.getPlayerManager();

        //this.teleport(pos.getX(), pos.getY(), pos.getZ());
        this.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), rotations[1], rotations[2]);
        this.setHeadYaw(rotations[0]);


        this.setCustomName(new LiteralText(displayName));
        world.spawnEntity(this);
    }

    public TaterzenNPC(ServerPlayerEntity owner, String displayName) {
        this(owner.getServer(), owner.getServerWorld(), displayName, owner.getPos(), new float[]{owner.headYaw, owner.yaw, owner.pitch});
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
    public void onDeath(DamageSource source) {
        if(this.npcData.entityType == EntityType.PLAYER) {
            PlayerListS2CPacket playerListS2CPacket = new PlayerListS2CPacket();
            ((PlayerListS2CPacketAccessor) playerListS2CPacket).setAction(REMOVE_PLAYER);
            ((PlayerListS2CPacketAccessor) playerListS2CPacket).setEntries(Collections.singletonList(playerListS2CPacket.new Entry(this.gameProfile, 0, GameMode.SURVIVAL, new LiteralText(this.getCustomName().asString()))));
            this.playerManager.sendToAll(playerListS2CPacket);
        }
        TATERZEN_NPCS.remove(this);
    }

    @Override
    public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
        return this.npcData.freeWill;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public void changeType(Entity entity) {
       this.changeType(entity.getType(), entity instanceof LivingEntity);
    }

    public void changeType(EntityType<?> fakeType, boolean fakeTypeAlive) {
        this.npcData.entityType = fakeType;
        this.npcData.fakeTypeAlive = fakeTypeAlive;
        playerManager.sendToDimension(new EntitiesDestroyS2CPacket(this.getEntityId()), this.world.getRegistryKey());
        playerManager.sendToDimension(new MobSpawnS2CPacket(this), this.world.getRegistryKey()); // We'll send player packet in ServerPlayNetworkHandlerMixin if needed
        playerManager.sendToDimension(new EntityTrackerUpdateS2CPacket(this.getEntityId(), this.getDataTracker(), true), this.world.getRegistryKey());
        //todo playerManager.sendToDimension(new EntityEquipmentUpdateS2CPacket(), this.world.getRegistryKey()); // Reload equipment
    }

    /**
     * Applies skin from {@link GameProfile}.
     *
     * @param texturesProfile GameProfile containing textures.
     * @param sendUpdate whether to send the texture update.
     */
    public void applySkin(GameProfile texturesProfile, boolean sendUpdate) {
        if(this.npcData.entityType != EntityType.PLAYER)
            return;

        // Clearing current skin
        try {
            PropertyMap map = this.gameProfile.getProperties();
            Property skin = map.get("textures").iterator().next();
            map.remove("textures", skin);
        } catch (NoSuchElementException ignored) { }

        // Setting new skin
        try {
            PropertyMap map = texturesProfile.getProperties();
            Property skin = map.get("textures").iterator().next();
            PropertyMap propertyMap = this.gameProfile.getProperties();
            propertyMap.put("textures", skin);
        } catch (NoSuchElementException ignored) { }

        // Sending updates
        if(sendUpdate) {
            PlayerListS2CPacket packet = new PlayerListS2CPacket();
            //noinspection ConstantConditions
            PlayerListS2CPacketAccessor accessor = (PlayerListS2CPacketAccessor) packet;
            accessor.setEntries(Collections.singletonList(packet.new Entry(this.gameProfile, 0, GameMode.SURVIVAL, this.getCustomName())));

            accessor.setAction(REMOVE_PLAYER);
            playerManager.sendToAll(packet);
            accessor.setAction(ADD_PLAYER);
            playerManager.sendToAll(packet);

            ServerChunkManager manager = (ServerChunkManager) this.world.getChunkManager();
            ThreadedAnvilChunkStorage storage = manager.threadedAnvilChunkStorage;
            EntityTrackerEntryAccessor trackerEntry = ((ThreadedAnvilChunkStorageAccessor) storage).getEntityTrackers().get(this.getEntityId());

            trackerEntry.getTrackingPlayers().forEach(tracking -> trackerEntry.getEntry().startTracking(tracking));
        }
    }

    /**
     * Gets the entity type of the NPC used on client.
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
    public void tickMovement() {
        if(this.npcData.movement == NPCData.Movement.LOOK) {
            Vec3d direction = TargetFinder.findTarget(this, 4, 4);
            //this.lookAt(direction);
        }
        else if(this.npcData.movement != NPCData.Movement.NONE) {
            super.tickMovement();
        }
    }

    public void setEquipmentEditor(PlayerEntity player) {
        this.npcData.equipmentEditor = player;
    }
    public boolean isEquipmentEditor(PlayerEntity player) {
        return player.equals(this.npcData.equipmentEditor);
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d pos, Hand hand) {
        long lastAction = ((ServerPlayerEntity) player).getLastActionTime();
        ActionResult result = ActionResult.FAIL;

        // As weird as it sounds, this gets triggered twice, first time with the item stack player is holding
        // then with "air" if fake type is player
        if(lastAction - this.npcData.lastActionTime < 50)
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


        this.npcData.lastActionTime = lastAction;
        return result;
    }

    @Override
    protected void applyDamage(DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        if(attacker instanceof PlayerEntity && this.isEquipmentEditor((PlayerEntity) attacker)) {
            ItemStack main = this.getMainHandStack();
            this.setStackInHand(Hand.MAIN_HAND, this.getOffHandStack());
            this.setStackInHand(Hand.OFF_HAND, main);
        }
        else
            super.applyDamage(source, amount);
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        CompoundTag npcTag = tag.getCompound("TaterzenNPCTag");

        this.npcData.fakeTypeAlive = npcTag.getBoolean("fakeTypeAlive");
        this.npcData.freeWill = npcTag.getBoolean("freeWill");
        this.npcData.movement = NPCData.Movement.valueOf(npcTag.getString("movement"));

        this.npcData.leashable = npcTag.getBoolean("leashable");
        this.npcData.pushable = npcTag.getBoolean("pushable");

        Identifier identifier = new Identifier(npcTag.getString("entityType"));
        this.npcData.entityType = Registry.ENTITY_TYPE.get(identifier);

        this.npcData.command = npcTag.getString("command");

        this.gameProfile = new GameProfile(this.getUuid(), this.getCustomName().asString());
        this.applySkin(SkullBlockEntity.loadProperties(this.gameProfile), false);

        this.server = this.world.getServer();
        this.playerManager = server.getPlayerManager();

        // Initialises movement
        this.setMovement(this.npcData.movement);
    }

    /**
     * Saves Taterzen to {@link CompoundTag tag}.
     *
     * @param tag
     */
    @Override
    public void writeCustomDataToTag(CompoundTag tag) {
        super.writeCustomDataToTag(tag);

        CompoundTag npcTag = new CompoundTag();

        npcTag.putBoolean("fakeTypeAlive", this.npcData.fakeTypeAlive);
        npcTag.putBoolean("freeWill", this.npcData.freeWill);

        npcTag.putString("movement", this.npcData.movement.toString());

        npcTag.putBoolean("leashable", this.npcData.leashable);
        npcTag.putBoolean("pushable", this.npcData.pushable);
        npcTag.putString("command", this.npcData.command);

        npcTag.putString("entityType", Registry.ENTITY_TYPE.getId(this.npcData.entityType).toString());
        tag.put("TaterzenNPCTag", npcTag);

        TATERZEN_NPCS.remove(this);
    }

    @Override
    protected void updateDespawnCounter() {
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

    /**
     * Sets the command to be executed on right - click
     * @param command command to execute
     */
    public void setCommand(String command) {
        this.npcData.command = command;
    }

    /**
     * Sets {@link org.samo_lego.taterzens.npc.NPCData.Movement movement type}
     * @param movement movement type
     */
    public void setMovement(NPCData.Movement movement) {
        this.npcData.movement = movement;
        this.goalSelector.remove(this.wanderAroundFarGoal);
        this.goalSelector.remove(this.lookAtPlayerGoal);
        if(movement != NPCData.Movement.NONE) {
            this.goalSelector.add(6, lookAtPlayerGoal);
            if(movement == NPCData.Movement.FREE)
                this.goalSelector.add(3, wanderAroundFarGoal);
        }
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        if(this.npcData.pushable) {
            super.pushAwayFrom(entity);
        }
    }

    @Override
    public boolean collidesWith(Entity other) {
        return this.npcData.pushable && super.collidesWith(other);
    }
}
