package org.samo_lego.taterzens.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.samo_lego.taterzens.Taterzens;

import org.samo_lego.taterzens.mixin.accessors.PlayerListS2CPacketAccessor;

import java.util.Collections;

import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.ADD_PLAYER;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.REMOVE_PLAYER;

public class TaterzenNPC extends HostileEntity implements CrossbowUser, RangedAttackMob {

    private final NPCData npcData = new NPCData();
    private PlayerManager playerManager;
    private MinecraftServer server;
    private GameProfile gameProfile;

    /**
     * Creates a TaterzenNPC.
     * Internal use only. Use {@link TaterzenNPC#TaterzenNPC(MinecraftServer, ServerWorld, String, Vec3d, Vec2f)} or {@link TaterzenNPC#TaterzenNPC(ServerPlayerEntity, String)}
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
    }

    public TaterzenNPC(MinecraftServer server, ServerWorld world, String displayName, Vec3d pos, Vec2f rotation, float headYaw) {
        this(Taterzens.TATERZEN, world);
        this.gameProfile = new GameProfile(this.getUuid(), displayName);
        this.applySkin(SkullBlockEntity.loadProperties(this.gameProfile), false);
        this.server = server;
        this.playerManager = server.getPlayerManager();

        //this.teleport(pos.getX(), pos.getY(), pos.getZ());
        this.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), rotation.x, rotation.y);
        this.setHeadYaw(headYaw);


        this.setCustomName(new LiteralText(displayName));
        world.spawnEntity(this);
    }

    public TaterzenNPC(ServerPlayerEntity owner, String displayName) {
        this(owner.getServer(), owner.getServerWorld(), displayName, owner.getPos(), new Vec2f(owner.yaw, owner.pitch), owner.headYaw);
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
            ((PlayerListS2CPacketAccessor) playerListS2CPacket).setEntries(Collections.singletonList(playerListS2CPacket.new Entry(this.gameProfile, 0, GameMode.SURVIVAL, new LiteralText(gameProfile.getName()))));
            this.playerManager.sendToDimension(playerListS2CPacket, this.world.getRegistryKey());
        }
    }

    @Override
    public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
        return this.npcData.freeWill;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public void changeType(Entity entity) {
        this.npcData.entityType = entity.getType();
        this.npcData.fakeTypeAlive = entity instanceof LivingEntity;
        playerManager.sendToDimension(new EntitiesDestroyS2CPacket(this.getEntityId()), this.world.getRegistryKey());
        playerManager.sendToDimension(new MobSpawnS2CPacket(this), this.world.getRegistryKey()); // We'll send player packet in ServerPlayNetworkHandlerMixin if needed
        playerManager.sendToDimension(new EntityTrackerUpdateS2CPacket(this.getEntityId(), this.getDataTracker(), true), this.world.getRegistryKey());
    }

    public void applySkin(GameProfile texturesProfile, boolean sendUpdate) {
        if(this.npcData.entityType != EntityType.PLAYER)
            return;
        try {
            PropertyMap map = texturesProfile.getProperties();
            Property property = map.get("textures").iterator().next();
            PropertyMap propertyMap = this.gameProfile.getProperties();
            propertyMap.put("textures", property);
        } catch (Error e) {
            e.printStackTrace();
        }
        if(sendUpdate) {
            PlayerListS2CPacket packet = new PlayerListS2CPacket();
            PlayerListS2CPacketAccessor accessor = (PlayerListS2CPacketAccessor) packet;
            accessor.setEntries(Collections.singletonList(packet.new Entry(this.getGameProfile(), 0, GameMode.SURVIVAL, this.getName())));
            accessor.setAction(REMOVE_PLAYER);
            playerManager.sendToAll(packet);
            accessor.setAction(ADD_PLAYER);
            playerManager.sendToAll(packet);
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

    /*@Override
    public Text getName() {
        return new LiteralText(this.gameProfile.getName());
    }*/

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
        if(this.npcData.freeWill && !this.npcData.stationary)
            super.tickMovement();
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if(!this.npcData.command.isEmpty()) {
            System.out.println("INteract");
            this.server.getCommandManager().execute(player.getCommandSource(), this.npcData.command);
            return ActionResult.PASS;
        }
        return ActionResult.FAIL;
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        System.out.println("From tag "+ tag);
        CompoundTag npcTag = tag.getCompound("TaterzenNPCTag");

        this.npcData.fakeTypeAlive = npcTag.getBoolean("fakeTypeAlive");
        this.npcData.freeWill = npcTag.getBoolean("freeWill");
        this.npcData.stationary = npcTag.getBoolean("stationary");
        this.npcData.leashable = npcTag.getBoolean("leashable");

        Identifier identifier = new Identifier(npcTag.getString("entityType"));
        this.npcData.entityType = Registry.ENTITY_TYPE.get(identifier);
        this.npcData.command = npcTag.getString("command");
        this.gameProfile = new GameProfile(this.getUuid(), this.getCustomName().asString());
        this.applySkin(SkullBlockEntity.loadProperties(this.gameProfile), false);
        this.server = this.world.getServer();
        this.playerManager = server.getPlayerManager();
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
        npcTag.putBoolean("stationary", this.npcData.stationary);
        npcTag.putBoolean("leashable", this.npcData.leashable);
        npcTag.putString("command", this.npcData.command);

        npcTag.putString("entityType", Registry.ENTITY_TYPE.getId(this.npcData.entityType).toString());
        tag.put("TaterzenNPCTag", npcTag);
        System.out.println("To tag!");
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

    public void setCommand(String command) {
        this.npcData.command = command;
    }
}
