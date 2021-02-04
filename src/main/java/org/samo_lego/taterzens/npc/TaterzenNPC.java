package org.samo_lego.taterzens.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.mixin.accessors.PlayerListS2CPacketAccessor;

import java.util.Collections;

import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.REMOVE_PLAYER;

public class TaterzenNPC extends SkeletonEntity implements CrossbowUser {

    private final NPCData npcData = new NPCData();
    private PlayerManager playerManager;
    private RegistryKey<World> dimension;
    private MinecraftServer server;
    private GameProfile gameProfile;

    /**
     * Creates a TaterzenNPC.
     * @deprecated Internal use only. Use {@link TaterzenNPC#TaterzenNPC(MinecraftServer, ServerWorld, String, Vec3d, Vec2f)} or {@link TaterzenNPC#TaterzenNPC(ServerPlayerEntity, String)}
     * @param world
     */
    @Deprecated
    protected TaterzenNPC(World world) {
        super(EntityType.SKELETON, world);
    }

    public TaterzenNPC(MinecraftServer server, ServerWorld world, String displayName, Vec3d pos, Vec2f rotation) {
        this(world);
        this.gameProfile = new GameProfile(this.getUuid(), displayName);
        this.server = server;
        this.playerManager = server.getPlayerManager();
        this.removed = false;
        this.stepHeight = 0.6F;
        this.setCanPickUpLoot(true);
        this.dimension = world.getRegistryKey();

        this.teleport(pos.getX(), pos.getY(), pos.getZ());
        this.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), rotation.x, rotation.y);
        this.setCustomName(new LiteralText(displayName));
        this.setCustomNameVisible(true);
        world.spawnEntity(this);
    }

    public TaterzenNPC(ServerPlayerEntity owner, String displayName) {
        this(owner.getServer(), owner.getServerWorld(), displayName, owner.getPos(), new Vec2f(owner.yaw, owner.pitch));
    }

    public NPCData getNpcData() {
        return npcData;
    }

    @Override
    public void setCharging(boolean charging) {

    }

    @Override
    public void shoot(LivingEntity target, ItemStack crossbow, ProjectileEntity projectile, float multiShotSpray) {
        super.attack(target, 4.0F);
    }

    @Override
    public void postShoot() {

    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {

    }

    @Override
    public void onDeath(DamageSource source) {
        Taterzens.TATERZENS.remove(this);
        if(this.npcData.entityType == EntityType.PLAYER) {
            PlayerListS2CPacket playerListS2CPacket = new PlayerListS2CPacket();
            ((PlayerListS2CPacketAccessor) playerListS2CPacket).setAction(REMOVE_PLAYER);
            ((PlayerListS2CPacketAccessor) playerListS2CPacket).setEntries(Collections.singletonList(playerListS2CPacket.new Entry(this.gameProfile, 0, GameMode.SURVIVAL, new LiteralText(gameProfile.getName()))));
            this.playerManager.sendToDimension(playerListS2CPacket, dimension);
        }
    }

    @Override
    public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
        return this.npcData.freeWill;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public void changeType(EntityType<?> entityType) {
        this.npcData.entityType = entityType;
        playerManager.sendToDimension(new EntitiesDestroyS2CPacket(this.getEntityId()), dimension);
        playerManager.sendToDimension(new MobSpawnS2CPacket(this), this.dimension);
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
        if(this.npcData.freeWill)
            super.tickMovement();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }
}
