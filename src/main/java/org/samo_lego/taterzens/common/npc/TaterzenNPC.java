package org.samo_lego.taterzens.common.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.common.Taterzens;
import org.samo_lego.taterzens.common.api.TaterzensAPI;
import org.samo_lego.taterzens.common.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.common.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.common.interfaces.ITaterzenPlayer;
import org.samo_lego.taterzens.common.mixin.accessors.AChunkMap;
import org.samo_lego.taterzens.common.mixin.accessors.AEntityTrackerEntry;
import org.samo_lego.taterzens.common.npc.ai.goal.*;
import org.samo_lego.taterzens.common.npc.commands.AbstractTaterzenCommand;
import org.samo_lego.taterzens.common.npc.commands.CommandGroups;
import org.samo_lego.taterzens.common.util.TextUtil;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.world.InteractionHand.MAIN_HAND;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.samo_lego.taterzens.common.Taterzens.*;
import static org.samo_lego.taterzens.common.mixin.accessors.APlayer.getPLAYER_MODE_CUSTOMISATION;
import static org.samo_lego.taterzens.common.util.TextUtil.errorText;
import static org.samo_lego.taterzens.common.util.TextUtil.successText;

/**
 * The NPC itself.
 */
public class TaterzenNPC extends PathfinderMob implements CrossbowAttackMob, RangedAttackMob, PolymerEntity {

    /**
     * Data of the NPC.
     */
    private final NPCData npcData = new NPCData();

    private final CommandGroups commandGroups;
    private final LinkedHashMap<ResourceLocation, TaterzenProfession> professions = new LinkedHashMap<>();
    private GameProfile gameProfile;

    /**
     * Goals
     * Public so they can be accessed from professions.
     */
    public final LookAtPlayerGoal lookPlayerGoal = new LookAtPlayerGoal(this, Player.class, 8.0F);
    public final RandomLookAroundGoal lookAroundGoal = new RandomLookAroundGoal(this);
    public final RandomStrollGoal wanderAroundFarGoal = new RandomStrollGoal(this, 1.0D, 30);

    public final FloatGoal swimGoal = new FloatGoal(this);

    /**
     * Target selectors.
     */
    public final NearestAttackableTargetGoal<LivingEntity> followTargetGoal = new NearestAttackableTargetGoal<>(this, LivingEntity.class, 100, false, true, (target, level) -> !this.isAlliedTo(target));
    public final NearestAttackableTargetGoal<Monster> followMonstersGoal = new NearestAttackableTargetGoal<>(this, Monster.class, 100, false, true, (target, level) -> !this.isAlliedTo(target));

    /**
     * Tracking movement
     */
    public final TrackEntityGoal trackLivingGoal = new TrackEntityGoal(this, LivingEntity.class, (target, level) -> !(target instanceof ServerPlayer) && target.isAlive());
    public final TrackEntityGoal trackPlayersGoal = new TrackEntityGoal(this, ServerPlayer.class, (target, level) -> !((ServerPlayer) target).hasDisconnected() && target.isAlive());
    public final TrackUuidGoal trackUuidGoal = new TrackUuidGoal(this, entity -> entity.getUUID().equals(this.npcData.follow.targetUuid) && entity.isAlive());


    /**
     * Used for {@link NPCData.Movement#PATH}.
     */
    public final LazyPathGoal pathGoal = new LazyPathGoal(this, 1.0D);
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
     * UUID of the "owner" that has locked this NPC.
     */
    private UUID lockedUuid;
    private final Map<UUID, Long> commandTimes = new HashMap<>();
    private ServerPlayer lookTarget;

    public TaterzenNPC(Level world) {
        this(TATERZEN_TYPE.get(), world);
    }

    /**
     * Creates a TaterzenNPC.
     * You'd probably want to use
     * {@link TaterzensAPI#createTaterzen(ServerLevel, String, Vec3, float[])} or
     * {@link TaterzensAPI#createTaterzen(ServerPlayer, String)}
     * instead, as this one doesn't set the position and custom name.
     *
     * @param npcData.entityList.put
 Taterzen entity type
     * @param world      Taterzen's world
     */
    public TaterzenNPC(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        // this.setMaxUpStep(0.6F); // Removed in 1.20.5
        this.setCanPickUpLoot(true);
        this.setCustomNameVisible(true);
        this.setCustomName(this.getName());
        this.setInvulnerable(config.defaults.invulnerable);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.setSpeed(0.4F);

        this.gameProfile = new GameProfile(this.getUUID(), this.getName().getString());
        this.commandGroups = new CommandGroups(this);


        // Set the sounds of this NPC to the default values from the config file
        // (will be overwritten by individual configuration when e.g. loading corresponding NBT data)
        if (!config.defaults.ambientSounds.isEmpty()) {
            this.npcData.ambientSounds = new ArrayList<>(config.defaults.ambientSounds);
        }
        if (!config.defaults.hurtSounds.isEmpty()) {
            this.npcData.hurtSounds = new ArrayList<>(config.defaults.hurtSounds);
        }
        if (!config.defaults.deathSounds.isEmpty()) {
            this.npcData.deathSounds = new ArrayList<>(config.defaults.deathSounds);
        }

        // This is where the magic happens for changing entity type...
        CompoundTag npcTag = new CompoundTag();
        if (npcTag.contains("Entity")) {
        	// Update it to the current settting
           	getLogger("Taterzens").info("[Taterzens]: We have a valid Entity setting and are putting it in the Tag - Map -  {}, - Tag -", npcData.playerEntity.get("Entity"), npcTag.getString("Entity"));
    		 //
           	npcTag.putString("Entity", npcData.playerEntity.get("Entity"));
           	// It's actually this we need to update since we have it stored, and the playerEntity hashmap is what we're using
           	npcData.playerEntity.put("Entity", npcTag.getString("Entity"));
        } else { // Initial NPC creation we go to PLAYER
        	getLogger("Taterzens").info("[Taterzens]: No valid Entity set, so it's PLAYER time.");

    		npcTag.putString("Entity", "PLAYER");
            npcData.playerEntity.put("Entity", "PLAYER");
        }

        // We only want to do this ONCE, and it gets fussy if placed anywhere else.
        // So, we check to see if the list is empty and then fill it if it is
        if (this.npcData.entityList.isEmpty()) {
        	  
            npcData.entityList.put("ALLAY", EntityType.ALLAY);
        	npcData.entityList.put("AREA_EFFECT_CLOUD", EntityType.AREA_EFFECT_CLOUD); 
        	npcData.entityList.put("ARMOR_STAND", EntityType.ARMOR_STAND);
        	npcData.entityList.put("ARROW", EntityType.ARROW);
        	npcData.entityList.put("AXOLOTL", EntityType.AXOLOTL);
        	npcData.entityList.put("BAT", EntityType.BAT);
        	npcData.entityList.put("BEE", EntityType.BEE);
        	npcData.entityList.put("BLAZE", EntityType.BLAZE);
        	npcData.entityList.put("BLOCK_DISPLAY", EntityType.BLOCK_DISPLAY);
        	npcData.entityList.put("BOAT", EntityType.OAK_BOAT); // TODO: add all boats
        	npcData.entityList.put("BREEZE", EntityType.BREEZE);
        	npcData.entityList.put("CAMEL", EntityType.CAMEL);
        	npcData.entityList.put("CAT", EntityType.CAT);
        	npcData.entityList.put("CAVE_SPIDER", EntityType.CAVE_SPIDER);
        	npcData.entityList.put("CHEST_BOAT", EntityType.OAK_CHEST_BOAT); // TODO: add all chest boats
        	npcData.entityList.put("CHEST_MINECART", EntityType.CHEST_MINECART);
        	npcData.entityList.put("CHICKEN", EntityType.CHICKEN);
        	npcData.entityList.put("COD", EntityType.COD);
        	npcData.entityList.put("COMMAND_BLOCK_MINECART", EntityType.COMMAND_BLOCK_MINECART);
        	npcData.entityList.put("COW", EntityType.COW);
        	npcData.entityList.put("CREEPER", EntityType.CREEPER);
        	npcData.entityList.put("DOLPHIN", EntityType.DOLPHIN);
        	npcData.entityList.put("DONKEY", EntityType.DONKEY);
        	npcData.entityList.put("DRAGON_FIREBALL", EntityType.DRAGON_FIREBALL);
        	npcData.entityList.put("DROWNED", EntityType.DROWNED);
        	npcData.entityList.put("EGG", EntityType.EGG);
        	npcData.entityList.put("ELDER_GUARDIAN", EntityType.ELDER_GUARDIAN);
        	npcData.entityList.put("END_CRYSTAL", EntityType.END_CRYSTAL);
        	npcData.entityList.put("ENDER_DRAGON", EntityType.ENDER_DRAGON);
        	npcData.entityList.put("ENDER_PEARL", EntityType.ENDER_PEARL);
        	npcData.entityList.put("ENDERMAN", EntityType.ENDERMAN);
        	npcData.entityList.put("ENDERMITE", EntityType.ENDERMITE);
        	npcData.entityList.put("EVOKER", EntityType.EVOKER);
        	npcData.entityList.put("EVOKER_FANGS", EntityType.EVOKER_FANGS);
        	npcData.entityList.put("EXPERIENCE_BOTTLE", EntityType.EXPERIENCE_BOTTLE);
        	npcData.entityList.put("EXPERIENCE_ORB", EntityType.EXPERIENCE_ORB);
        	npcData.entityList.put("EYE_OF_ENDER", EntityType.EYE_OF_ENDER);
        	npcData.entityList.put("FALLING_BLOCK", EntityType.FALLING_BLOCK);
        	npcData.entityList.put("FIREBALL", EntityType.FIREBALL);
        	npcData.entityList.put("FIREWORK_ROCKET", EntityType.FIREWORK_ROCKET);
        	npcData.entityList.put("FISHING_BOBBER", EntityType.FISHING_BOBBER);
        	npcData.entityList.put("FOX", EntityType.FOX);
        	npcData.entityList.put("FROG", EntityType.FROG);
        	npcData.entityList.put("FURNACE_MINECART", EntityType.FURNACE_MINECART);
        	npcData.entityList.put("GHAST", EntityType.GHAST);
        	npcData.entityList.put("GIANT", EntityType.GIANT);
        	npcData.entityList.put("GLOW_ITEM_FRAME", EntityType.GLOW_ITEM_FRAME);
        	npcData.entityList.put("GLOW_SQUID", EntityType.GLOW_SQUID);
        	npcData.entityList.put("GOAT", EntityType.GOAT);
        	npcData.entityList.put("GUARDIAN", EntityType.GUARDIAN);
        	npcData.entityList.put("HOGLIN", EntityType.HOGLIN);
        	npcData.entityList.put("HOPPER_MINECART", EntityType.HOPPER_MINECART);
        	npcData.entityList.put("HORSE", EntityType.HORSE);
        	npcData.entityList.put("HUSK", EntityType.HUSK);
        	npcData.entityList.put("ILLUSIONER", EntityType.ILLUSIONER);
        	npcData.entityList.put("INTERACTION", EntityType.INTERACTION);
        	npcData.entityList.put("IRON_GOLEM", EntityType.IRON_GOLEM);
        	npcData.entityList.put("ITEM", EntityType.ITEM);
        	npcData.entityList.put("ITEM_DISPLAY", EntityType.ITEM_DISPLAY);
        	npcData.entityList.put("ITEM_FRAME", EntityType.ITEM_FRAME);
        	npcData.entityList.put("LEASH_KNOT", EntityType.LEASH_KNOT);
        	npcData.entityList.put("LIGHTNING_BOLT", EntityType.LIGHTNING_BOLT);
        	npcData.entityList.put("LLAMA", EntityType.LLAMA);
        	npcData.entityList.put("LLAMA_SPIT", EntityType.LLAMA_SPIT);
        	npcData.entityList.put("MAGMA_CUBE", EntityType.MAGMA_CUBE);
        	npcData.entityList.put("MARKER", EntityType.MARKER);
        	npcData.entityList.put("MINECART", EntityType.MINECART);
        	npcData.entityList.put("MOOSHROOM", EntityType.MOOSHROOM);
        	npcData.entityList.put("MULE", EntityType.MULE);
        	npcData.entityList.put("OCELOT", EntityType.OCELOT);
        	npcData.entityList.put("PAINTING", EntityType.PAINTING);
        	npcData.entityList.put("PANDA", EntityType.PANDA);
        	npcData.entityList.put("PARROT", EntityType.PARROT);
        	npcData.entityList.put("PHANTOM", EntityType.PHANTOM);
        	npcData.entityList.put("PIG", EntityType.PIG);
        	npcData.entityList.put("PIGLIN", EntityType.PIGLIN);
        	npcData.entityList.put("PIGLIN_BRUTE", EntityType.PIGLIN_BRUTE);
        	npcData.entityList.put("PILLAGER", EntityType.PILLAGER);
        	npcData.entityList.put("PLAYER", EntityType.PLAYER); 
        	npcData.entityList.put("POLAR_BEAR", EntityType.POLAR_BEAR);
        	npcData.entityList.put("POTION", EntityType.POTION);
        	npcData.entityList.put("PUFFERFISH", EntityType.PUFFERFISH);
        	npcData.entityList.put("RABBIT", EntityType.RABBIT);
        	npcData.entityList.put("RAVAGER", EntityType.RAVAGER);
        	npcData.entityList.put("SALMON", EntityType.SALMON);
        	npcData.entityList.put("SHEEP", EntityType.SHEEP);
        	npcData.entityList.put("SHULKER", EntityType.SHULKER);
        	npcData.entityList.put("SHULKER_BULLET", EntityType.SHULKER_BULLET);
        	npcData.entityList.put("SILVERFISH", EntityType.SILVERFISH);
        	npcData.entityList.put("SKELETON", EntityType.SKELETON);
        	npcData.entityList.put("SKELETON_HORSE", EntityType.SKELETON_HORSE);
        	npcData.entityList.put("SLIME", EntityType.SLIME);
        	npcData.entityList.put("SMALL_FIREBALL", EntityType.SMALL_FIREBALL);
        	npcData.entityList.put("SNIFFER", EntityType.SNIFFER);
        	npcData.entityList.put("SNOW_GOLEM", EntityType.SNOW_GOLEM);
        	npcData.entityList.put("SNOWBALL", EntityType.SNOWBALL);
        	npcData.entityList.put("SPAWNER_MINECART", EntityType.SPAWNER_MINECART);
        	npcData.entityList.put("SPECTRAL_ARROW", EntityType.SPECTRAL_ARROW);
        	npcData.entityList.put("SPIDER", EntityType.SPIDER);
        	npcData.entityList.put("SQUID", EntityType.SQUID);
        	npcData.entityList.put("STRAY", EntityType.STRAY);
        	npcData.entityList.put("STRIDER", EntityType.STRIDER);
        	npcData.entityList.put("TADPOLE", EntityType.TADPOLE);
        	npcData.entityList.put("TEXT_DISPLAY", EntityType.TEXT_DISPLAY);
        	npcData.entityList.put("TNT", EntityType.TNT);
        	npcData.entityList.put("TNT_MINECART", EntityType.TNT_MINECART);
        	npcData.entityList.put("TRADER_LLAMA", EntityType.TRADER_LLAMA);
        	npcData.entityList.put("TRIDENT", EntityType.TRIDENT);
        	npcData.entityList.put("TROPICAL_FISH", EntityType.TROPICAL_FISH);
        	npcData.entityList.put("TURTLE", EntityType.TURTLE);
        	npcData.entityList.put("VEX", EntityType.VEX);
        	npcData.entityList.put("VILLAGER", EntityType.VILLAGER);
        	npcData.entityList.put("VINDICATOR", EntityType.VINDICATOR);
        	npcData.entityList.put("WANDERING_TRADER", EntityType.WANDERING_TRADER);
        	npcData.entityList.put("WARDEN", EntityType.WARDEN);
        	npcData.entityList.put("WIND_CHARGE", EntityType.WIND_CHARGE);
        	npcData.entityList.put("WITCH", EntityType.WITCH);
        	npcData.entityList.put("WITHER", EntityType.WITHER);
        	npcData.entityList.put("WITHER_SKELETON", EntityType.WITHER_SKELETON);
        	npcData.entityList.put("WITHER_SKULL", EntityType.WITHER_SKULL);
        	npcData.entityList.put("WOLF", EntityType.WOLF);
        	npcData.entityList.put("ZOGLIN", EntityType.ZOGLIN);
        	npcData.entityList.put("ZOMBIE", EntityType.ZOMBIE);
        	npcData.entityList.put("ZOMBIE_HORSE", EntityType.ZOMBIE_HORSE);
        	npcData.entityList.put("ZOMBIE_VILLAGER", EntityType.ZOMBIE_VILLAGER);
        	npcData.entityList.put("ZOMBIFIED_PIGLIN", EntityType.ZOMBIFIED_PIGLIN);
        }
        
    }

    public void modEntity(String entType) {
    	this.npcData.playerEntity.put("Entity", entType);
    }
    

    /**
     * Creates default taterzen attributes.
     *
     * @return attribute supplier builder.
     */
    public static AttributeSupplier.Builder createTaterzenAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.25D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2505D)
                .add(Attributes.FLYING_SPEED, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    /**
     * Adds sounds to the list of ambient sounds of a Taterzen.
     *
     * @param ambientSound The ambient sound resource location to add.
     */
    public void addAmbientSound(String ambientSound) {
        this.npcData.ambientSounds.add(ambientSound);
    }

    /**
     * Adds sounds to the list of hurt sounds of a Taterzen.
     *
     * @param hurtSound The hurt sound resource location to add.
     */
    public void addHurtSound(String hurtSound) {
        this.npcData.hurtSounds.add(hurtSound);
    }

    /**
     * Adds sounds to the list of death sounds of a Taterzen.
     *
     * @param deathSound The death sound resource location to add.
     */
    public void addDeathSound(String deathSound) {
        this.npcData.deathSounds.add(deathSound);
    }

    /**
     * Removes sounds from the list of ambient sounds of a Taterzen.
     *
     * @param index The index of the ambient sound resource location within the NPCData structure.
     */
    public void removeAmbientSound(int index) {
        this.npcData.ambientSounds.remove(index);
    }

    /**
     * Removes sounds from the list of hurt sounds of a Taterzen.
     *
     * @param index The index of the hurt sound resource location within the NPCData structure.
     */
    public void removeHurtSound(int index) {
        this.npcData.hurtSounds.remove(index);
    }

    /**
     * Removes sounds from the list of death sounds of a Taterzen.
     *
     * @param index The index of the death sound resource location within the NPCData structure.
     */
    public void removeDeathSound(int index) {
        this.npcData.deathSounds.remove(index);
    }

    /**
     * Adds command to the list
     * of commands that will be executed on
     * right-clicking the Taterzen.
     *
     * @param command command to add
     */
    public boolean addCommand(AbstractTaterzenCommand command) {
        if (command.getType() == AbstractTaterzenCommand.CommandType.BUNGEE && !config.bungee.enableCommands) {
            return false;
        }
        return this.commandGroups.addCommand(command);
    }

    /**
     * Adds command to the list
     * of commands that will be executed on
     * right-clicking the Taterzen.
     *
     * @param command    command to add
     * @param groupIndex index of the group to add the command to
     */
    public boolean addCommand(AbstractTaterzenCommand command, int groupIndex) {
        if (command.getType() == AbstractTaterzenCommand.CommandType.BUNGEE && !config.bungee.enableCommands) {
            return false;
        }
        return this.commandGroups.get(groupIndex).add(command);
    }

    /**
     * Gets all available commands
     *
     * @return array of groups, each containing array of commands.
     */
    public CommandGroups getCommandGroups() {
        return this.commandGroups;
    }

    /**
     * Gets commands from specific group.
     *
     * @param group group index.
     * @return array list of commands that will be executed on right click.
     */
    public ArrayList<AbstractTaterzenCommand> getGroupCommands(int group) {
        return this.commandGroups.get(group);
    }

    /**
     * Removes certain command from command list.
     *
     * @param groupIndex   index of the group.
     * @param commandIndex index of the command.
     */
    public void removeGroupCommand(int groupIndex, int commandIndex) {
        this.commandGroups.get(groupIndex).remove(commandIndex);
    }

    /**
     * Clears all the commands Taterzen
     * executes on right-click.
     */
    public void clearAllCommands() {
        this.commandGroups.clear();
    }


    public void clearGroupCommands(int index) {
        this.commandGroups.remove(index);
    }

    //@Override
    protected int getPermissionLevel() {
        return this.npcData.permissionLevel;
    }

    public void setPermissionLevel(int newPermissionLevel) {
        this.npcData.permissionLevel = newPermissionLevel;
    }

    /**
     * Sets {@link NPCData.Movement movement type}
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

        for (TaterzenProfession profession : this.professions.values()) {
            profession.onMovementSet(movement);
        }

        if (movement != NPCData.Movement.NONE && movement != NPCData.Movement.FORCED_LOOK) {
            int priority = 8;
            if (movement == NPCData.Movement.FORCED_PATH) {
                this.goalSelector.addGoal(4, directPathGoal);
                priority = 5;
            } else if (movement == NPCData.Movement.PATH) {
                this.goalSelector.addGoal(4, pathGoal);
            } else if (movement == NPCData.Movement.FREE) {
                this.goalSelector.addGoal(6, wanderAroundFarGoal);
            }

            this.goalSelector.addGoal(priority, lookPlayerGoal);
            this.goalSelector.addGoal(priority + 1, lookAroundGoal);
        }

        if (this.getTag("AllowSwimming", config.defaults.allowSwim)) {
            this.goalSelector.addGoal(0, this.swimGoal);
        }
    }

    /**
     * Gets current movement of taterzen.
     *
     * @return current movement
     */
    public NPCData.Movement getMovement() {
        return this.npcData.movement;
    }

    /**
     * Adds block position as a node in path of Taterzen.
     *
     * @param blockPos position to add.
     */
    public void addPathTarget(BlockPos blockPos) {
        this.npcData.pathTargets.add(blockPos);
        this.restrictTo(this.npcData.pathTargets.get(0), 1);
    }


    /**
     * Removes node from path targets.
     *
     * @param blockPos position from path to remove
     */
    public void removePathTarget(BlockPos blockPos) {
        this.npcData.pathTargets.remove(blockPos);
    }

    /**
     * Gets the path nodes / targets.
     *
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
     * Ticks the movement depending on {@link NPCData.Movement} type
     */
    @Override
    public void aiStep() {
        if (this.npcData.equipmentEditor != null)
            return;

        // Profession event
        professionLoop:
        for (TaterzenProfession profession : this.professions.values()) {
            InteractionResult result = profession.tickMovement();
            if (result == InteractionResult.CONSUME) // Stop processing others, but continue with base Taterzen movement tick
                break professionLoop;
            else if (result == InteractionResult.FAIL) // Stop whole movement tick
                return;
            else if (result == InteractionResult.SUCCESS || result == InteractionResult.SUCCESS_SERVER) { // Continue with super, but skip Taterzen's movement tick
                super.aiStep();
                return;
            } else { // Continue with other professions
                break;
            }
        }

        // FORCED_LOOK is processed in tick(), as we get nearby players there
        if (this.npcData.movement != NPCData.Movement.NONE && this.npcData.movement != NPCData.Movement.FORCED_LOOK) {
            this.setYRot(this.yHeadRot); // Rotates body as well
            LivingEntity target = this.getTarget();

            if ((this.npcData.movement == NPCData.Movement.FORCED_PATH ||
                    this.npcData.movement == NPCData.Movement.PATH) &&
                    !this.npcData.pathTargets.isEmpty() &&
                    !this.isPathFinding()) {
                // Checking here as well (if path targets size was changed during the previous tick)
                if (this.npcData.currentMoveTarget >= this.npcData.pathTargets.size())
                    this.npcData.currentMoveTarget = 0;

                if (this.getRestrictCenter().distToCenterSqr(this.position()) < 5.0D) {
                    if (++this.npcData.currentMoveTarget >= this.npcData.pathTargets.size())
                        this.npcData.currentMoveTarget = 0;

                    // New target
                    this.restrictTo(this.npcData.pathTargets.get(this.npcData.currentMoveTarget), 1);
                }
            }

            super.aiStep();
            if (this.isAggressive() && this.getTag("JumpAttack", config.defaults.jumpWhileAttacking) && this.onGround() && target != null && this.distanceToSqr(target) < 4.0D && this.random.nextInt(5) == 0)
                this.jumpFromGround();
        } else {
            // As super.aiStep() isn't executed, we check for items that are available to be picked up
            if (this.isAlive() && !this.dead) {
                List<ItemEntity> list = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0D, 0.0D, 1.0D));

                for (ItemEntity itemEntity : list) {
                    if (!itemEntity.isRemoved() && !itemEntity.getItem().isEmpty() && !itemEntity.hasPickUpDelay() && !this.level().isClientSide() && this.wantsToPickUp((ServerLevel) this.level(), itemEntity.getItem())) {
                        this.pickUpItem((ServerLevel) this.level(), itemEntity);
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

        AABB box = this.getBoundingBox().inflate(config.messages.speakDistance);
        List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, box);

        if (!this.npcData.messages.isEmpty()) {
            for (ServerPlayer player : players) {
                // Filter them here (not use a predicate above)
                // as we need the original list below
                if (((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.MESSAGES) {
                    continue;
                }

                ITaterzenPlayer pl = (ITaterzenPlayer) player;
                int msgPos = pl.getLastMsgPos(this.getUUID());
                if (msgPos >= this.npcData.messages.size())
                    msgPos = 0;
                if (this.npcData.messages.get(msgPos).getSecond() < pl.ticksSinceLastMessage(this.getUUID())) {
                    player.sendSystemMessage(
                            Component.translatable(config.messages.structure, this.getName().copy(), this.npcData.messages.get(msgPos).getFirst()));
                    // Resetting message counter
                    pl.resetMessageTicks(this.getUUID());

                    ++msgPos;
                    // Setting new message position
                    pl.setLastMsgPos(this.getUUID(), msgPos);
                }
            }
        }
        if (!players.isEmpty()) {
            // We tick forced look here, as we already have players list.
            if (this.npcData.movement == NPCData.Movement.FORCED_LOOK) {
                if (this.lookTarget == null || this.distanceTo(this.lookTarget) > 5.0D || this.lookTarget.hasDisconnected() || !this.lookTarget.isAlive()) {
                    this.lookTarget = players.get(this.random.nextInt(players.size()));
                }

                this.lookAt(this.lookTarget, 60.0F, 60.0F);
                this.setYHeadRot(this.getYRot());
            }
            // Tick profession
            for (TaterzenProfession profession : this.professions.values()) {
                profession.onPlayersNearby(players);
            }
        }
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public Component getTabListName() {
        if (!config.obscureTabList) return getName();

        var component = Component.literal("").withStyle(ChatFormatting.DARK_GRAY);
        component.append(getName());
        component.append(" [NPC]");
        return component;
    }

    /**
     * Sets the custom name
     *
     * @param name new name to be set.
     */
    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        String profileName = "Taterzen";
        if (name != null) {
            profileName = name.getString();
            if (name.getString().length() > 16) {
                // Minecraft kicks you if player has name longer than 16 chars in GameProfile
                profileName = name.getString().substring(0, 16);
            }
        }
        CompoundTag skin = null;
        if (this.gameProfile != null)
            skin = this.writeSkinToTag(this.gameProfile);
        this.gameProfile = new GameProfile(this.getUUID(), profileName);
        if (skin != null) {
            this.setSkinFromTag(skin);
            this.broadcastProfileUpdates();
        }
    }

    /**
     * Updates Taterzen's {@link GameProfile} for others.
     */
    public void broadcastProfileUpdates() {
        if (this.level().isClientSide()) return;

        ServerChunkCache manager = (ServerChunkCache) this.level().getChunkSource();
        ChunkMap storage = manager.chunkMap;
        AEntityTrackerEntry trackerEntry = ((AChunkMap) storage).getEntityMap().get(this.getId());
        if (trackerEntry != null) {
            trackerEntry.getSeenBy().forEach(tracking -> {
                tracking.getPlayer().connection.send(new ClientboundPlayerInfoRemovePacket(List.of(this.getUUID())));
                trackerEntry.getPlayer().removePairing(tracking.getPlayer());
                trackerEntry.getPlayer().addPairing(tracking.getPlayer());
            });
        }
    }


    /**
     * Applies skin from {@link GameProfile}.
     *
     * @param texturesProfile GameProfile containing textures.
     */
    public void applySkin(GameProfile texturesProfile) {
        if (this.gameProfile == null)
            return;

        // Setting new skin
        setSkinFromTag(writeSkinToTag(texturesProfile));

        // Sending updates
        this.broadcastProfileUpdates();
    }

    /**
     * Sets the Taterzen skin from tag
     *
     * @param tag compound tag containing the skin
     */
    public void setSkinFromTag(CompoundTag tag) {
        // Clearing current skin
        try {
            PropertyMap map = this.gameProfile.getProperties();
            Property skin = map.get("textures").iterator().next();
            map.remove("textures", skin);
        } catch (NoSuchElementException ignored) {
        }
        // Setting the skin
        try {
            String value = tag.getString("value");
            String signature = tag.getString("signature");

            if (!value.isEmpty() && !signature.isEmpty()) {
                PropertyMap propertyMap = this.gameProfile.getProperties();
                propertyMap.put("textures", new Property("textures", value, signature));
            }

        } catch (Error ignored) {
        }
    }

    /**
     * Writes skin to tag
     *
     * @param profile game profile containing skin
     * @return compound tag with skin values
     */
    public CompoundTag writeSkinToTag(GameProfile profile) {
        CompoundTag skinTag = new CompoundTag();
        try {
            PropertyMap propertyMap = profile.getProperties();
            Property skin = propertyMap.get("textures").iterator().next();

            skinTag.putString("value", skin.value());
            skinTag.putString("signature", skin.signature());
        } catch (NoSuchElementException ignored) {
        }

        return skinTag;
    }

    /**
     * Loads Taterzen from {@link CompoundTag}.
     *
     * @param tag tag to load Taterzen from.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        // Has a "preset" tag
        // We want to overwrite self data from that provided by preset
        if (tag.contains("PresetOverride")) {
            this.loadPresetTag(tag);
            return;  // Other data doesn't need to be loaded as it will be handled by preset
        }

        CompoundTag npcTag = tag.getCompound("TaterzenNPCTag");

        // Boolean tags
        CompoundTag tags = npcTag.getCompound("Tags");

        for (String key : tags.getAllKeys()) {
            this.setTag(key, tags.getBoolean(key));
        }

        // Skin layers
        this.setSkinLayers(npcTag.getByte("SkinLayers"));

        // Sounds
        ListTag ambientSounds = (ListTag) npcTag.get("AmbientSounds");
        if (ambientSounds != null) {
            this.npcData.ambientSounds.clear(); // removes default loaded sounds
            ambientSounds.forEach(snd -> this.addAmbientSound(snd.getAsString()));
        }

        ListTag hurtSounds = (ListTag) npcTag.get("HurtSounds");
        if (hurtSounds != null) {
            this.npcData.hurtSounds.clear(); // removes default loaded sounds
            hurtSounds.forEach(snd -> this.addHurtSound(snd.getAsString()));
        }

        ListTag deathSounds = (ListTag) npcTag.get("DeathSounds");
        if (deathSounds != null) {
            this.npcData.deathSounds.clear(); // removes default loaded sounds
            deathSounds.forEach(snd -> this.addDeathSound(snd.getAsString()));
        }


        // -------------------------------------------------------------
        // Deprecated since 1.10.0
        // Commands
        ListTag commands = (ListTag) npcTag.get("Commands");
        // Bungee commands
        ListTag bungeeCommands = (ListTag) npcTag.get("BungeeCommands");

        if (commands != null && bungeeCommands != null) {
            // Scheduled for removal
            this.commandGroups.fromOldTag(commands, bungeeCommands);
        }
        // -------------------------------------------------------------

        var cmds = npcTag.getCompound("CommandGroups");
        this.commandGroups.fromTag(cmds);

        ListTag pathTargets = (ListTag) npcTag.get("PathTargets");
        if (pathTargets != null) {
            if (!pathTargets.isEmpty()) {
                pathTargets.forEach(posTag -> {
                    if (posTag instanceof CompoundTag pos) {
                        BlockPos target = new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"));
                        this.addPathTarget(target);
                    }
                });
                this.restrictTo(this.npcData.pathTargets.get(0), 1);
            }
        }
        this.npcData.currentMoveTarget = npcTag.getInt("CurrentMoveTarget");

        ListTag messages = (ListTag) npcTag.get("Messages");
        if (messages != null && !messages.isEmpty()) {
            messages.forEach(msgTag -> {
                CompoundTag msgCompound = (CompoundTag) msgTag;
                this.addMessage(TextUtil.fromNbtElement(msgCompound.get("Message")), msgCompound.getInt("Delay"));
            });
        }

        this.setPermissionLevel(npcTag.getInt("PermissionLevel"));

        if (npcTag.contains("Behaviour")) {
            this.setBehaviour(NPCData.Behaviour.valueOf(npcTag.getString("Behaviour")));
        } else {
            this.setBehaviour(NPCData.Behaviour.PASSIVE);
        }

        String profileName = this.getName().getString();
        if (profileName.length() > 16) {
            // Minecraft kicks you if player has name longer than 16 chars in GameProfile
            profileName = profileName.substring(0, 16);
        }

        this.gameProfile = new GameProfile(this.getUUID(), profileName);

        // Skin is cached
        CompoundTag skinTag = npcTag.getCompound("skin");
        this.setSkinFromTag(skinTag);

        // Profession initialising
        ListTag professions = (ListTag) npcTag.get("Professions");
        if (professions != null && !professions.isEmpty()) {
            professions.forEach(professionTag -> {
                CompoundTag professionCompound = (CompoundTag) professionTag;

                ResourceLocation professionId = ResourceLocation.parse(professionCompound.getString("ProfessionType"));
                if (PROFESSION_TYPES.containsKey(professionId)) {
                    TaterzenProfession profession = PROFESSION_TYPES.get(professionId).apply(this);
                    this.addProfession(professionId, profession);

                    // Parsing profession data
                    profession.readNbt(professionCompound.getCompound("ProfessionData"));
                } else {
                    Taterzens.LOGGER.error("Taterzen {} was saved with profession id {}, but none of the mods provides it.", this.getName().getString(), professionId);
                }
            });
        }

        // Follow targets
        CompoundTag followTag = npcTag.getCompound("Follow");
        if (followTag.contains("Type"))
            this.setFollowType(NPCData.FollowTypes.valueOf(followTag.getString("Type")));

        if (followTag.contains("UUID"))
            this.setFollowUuid(followTag.getUUID("UUID"));

        if (npcTag.contains("Pose")) {
            this.setPose(Pose.valueOf(npcTag.getString("Pose")));
        } else {
            this.setPose(Pose.STANDING);
        }


        if (npcTag.contains("movement")) {
            this.setMovement(NPCData.Movement.valueOf(npcTag.getString("movement")));
        } else {
            this.setMovement(NPCData.Movement.NONE);
        }

        if (npcTag.contains("LockedBy")) {
            this.lockedUuid = npcTag.getUUID("LockedBy");
        }


        // ------------------------------------------------------------
        //  Migration to 1.10.0
        if (npcTag.contains("AllowFlight"))
            this.setAllowFlight(npcTag.getBoolean("AllowFlight"));
        if (npcTag.contains("AllowSwimming"))
            this.setAllowSwimming(npcTag.getBoolean("AllowSwimming"));
        // --------------------------------------------------------------

        
        // This magic is where we check on loading in whether the NPC has a different TYPE to what is expected
        // Normally, it will default to PLAYER, but we want the TYPE changes to be persistent
        // The logger is pushing details to the minecraft log so you can see it actually parsing things

        getLogger("Taterzens").info("[Taterzens]: Out of interest, the Tag Entity is {}.", npcTag.get("Entity"));
 
        if (npcTag.contains("Entity")) {
        	// Update it to the current settting
        	if (npcTag.get("Entity") == null) {
            	getLogger("Taterzens").error("[Taterzens]: In the SaveDataread.");

        		npcTag.putString("Entity", "PLAYER");
                npcData.playerEntity.put("Entity", "PLAYER");
        	} else {
            	getLogger("Taterzens").info("[Taterzens]: We have a valid Entity setting and are putting it in the Tag");
        		//
            	// It's actually this we need to update since we have it stored, and the playerEntity hashmap is what we're using
            	npcData.playerEntity.put("Entity", npcTag.getString("Entity"));
            	// And we need to override the other setting
            	npcTag.putString("Entity", npcTag.getString("Entity"));
        	}
        }
        
        this.setMinCommandInteractionTime(npcTag.getLong("MinCommandInteractionTime"));
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

        // Boolean tags
        CompoundTag tags = new CompoundTag();

        for (Map.Entry<String, Boolean> entry : this.npcData.booleanTags.entrySet()) {
            tags.putBoolean(entry.getKey(), entry.getValue());
        }

        npcTag.put("Tags", tags);

        // Skin layers
        npcTag.putByte("SkinLayers", this.npcData.skinLayers);

        // Sounds
        ListTag ambientSounds = new ListTag();
        this.npcData.ambientSounds.forEach(snd -> ambientSounds.add(StringTag.valueOf(snd)));
        npcTag.put("AmbientSounds", ambientSounds);

        ListTag hurtSounds = new ListTag();
        this.npcData.hurtSounds.forEach(snd -> hurtSounds.add(StringTag.valueOf(snd)));
        npcTag.put("HurtSounds", hurtSounds);

        ListTag deathSounds = new ListTag();
        this.npcData.deathSounds.forEach(snd -> deathSounds.add(StringTag.valueOf(snd)));
        npcTag.put("DeathSounds", deathSounds);

        // Commands
        var cmds = new CompoundTag();
        this.commandGroups.toTag(cmds);
        npcTag.put("CommandGroups", cmds);

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
        npcTag.putInt("CurrentMoveTarget", this.npcData.currentMoveTarget);

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

        if (this.npcData.follow.targetUuid != null)
            followTag.putUUID("UUID", this.npcData.follow.targetUuid);

        npcTag.put("Follow", followTag);

        npcTag.putString("Pose", this.getPose().toString());


        // Locking
        if (this.lockedUuid != null)
            npcTag.putUUID("LockedBy", this.lockedUuid);

        npcTag.putLong("MinCommandInteractionTime", this.npcData.minCommandInteractionTime);


        getLogger("Taterzens").info("[Taterzens]: The Game is paused or saving. We're setting NPC entity to {}", npcData.playerEntity.get("Entity"));
       
        // We take whatever the npc has been changed to and shove it in the tag.
    	// Only end up in here on pausing or saving to quit
    	if (npcData.playerEntity.get("Entity") != null) {
    		npcTag.putString("Entity", npcData.playerEntity.get("Entity"));
    	}

        tag.put("TaterzenNPCTag", npcTag);
    }

    /**
     * Loads Taterzen data from preset file.
     *
     * @param tag tag containing preset name.
     */
    private void loadPresetTag(CompoundTag tag) {
        String preset = tag.getString("PresetOverride") + ".json";
        File presetFile = new File(Taterzens.getInstance().getPresetDirectory() + "/" + preset);

        if (presetFile.exists()) {
            this.loadFromPresetFile(presetFile, preset);
        }
    }

    /**
     * Loads Taterzen data from preset file. Loads team data as well.
     *
     * @param presetFile file containing a taterzen preset.
     * @param presetName name of the preset.
     */
    public void loadFromPresetFile(File presetFile, String presetName) {
        CompoundTag saveTag = TaterzensAPI.loadPresetTag(presetFile);
        saveTag.putString("UUID", this.getStringUUID());

        // Avoid looping if user has messed with preset
        if (!presetName.isEmpty() && presetName.equals(saveTag.getString("PresetOverride"))) {
            saveTag.remove("PresetOverride");
            LOGGER.warn("Preset override loop detected in {}. Aborting it.", presetName);
        }

        Vec3 savedPos = this.getPosition(0);
        this.load(saveTag);
        this.setPos(savedPos);

        CompoundTag npcTag = (CompoundTag) saveTag.get("TaterzenNPCTag");
        if (npcTag != null) {
            // Team stuff
            String savedTeam = npcTag.getString("SavedTeam");
            PlayerTeam team = this.level().getScoreboard().getPlayerTeam(savedTeam);
            if (team != null) {
                this.level().getScoreboard().addPlayerToTeam(this.getScoreboardName(), team);
            }
        }
    }

    /**
     * Sets player as equipment editor.
     *
     * @param player player that will be marked as equipment editor.
     */
    public void setEquipmentEditor(@Nullable Player player) {
        this.npcData.equipmentEditor = player;
    }

    /**
     * Sets player as equipment editor.
     *
     * @param player player to check.
     * @return true if player is equipment editor of the NPC, otherwise false.
     */
    public boolean isEquipmentEditor(@NotNull Player player) {
        return player.equals(this.npcData.equipmentEditor);
    }

    /**
     * Handles interaction (right clicking on the NPC).
     *
     * @param player player interacting with NPC
     * @param hand   player's interacting hand
     * @return {@link InteractionResult#PASS} if NPC has a right click action, otherwise {@link InteractionResult#FAIL}
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) return InteractionResult.PASS;

        ITaterzenPlayer ipl = (ITaterzenPlayer) player;
        long lastAction = ((ServerPlayer) player).getLastActionTime();

        // As weird as it sounds, this gets triggered twice, first time with the item stack player is holding
        // then with "air" if fake type is player / armor stand
        if (lastAction - ipl.getLastInteractionTime() < 50) {
            return InteractionResult.FAIL;
        }
        ipl.setLastInteraction(lastAction);


        for (TaterzenProfession profession : this.professions.values()) {
            InteractionResult professionResult = profession.interactAt(player, player.position(), hand);
            if (professionResult != InteractionResult.PASS)
                return professionResult;
        }

        if (this.isEquipmentEditor(player)) {
            ItemStack stack = player.getItemInHand(hand).copy();

            if (stack.isEmpty() && player.isShiftKeyDown()) {
                // TODO fix the Custom DeathLoot drops.  They've changed with 1.21.
                // this.dropCustomDeathLoot(this.damageSources().playerAttack(player), 1, this.isEquipmentDropsAllowed());
            } else if (player.isShiftKeyDown()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, stack);
            } else {
                EquipmentSlot slot = getEquipmentSlotForItem(stack);
                this.setItemSlotAndDropWhenKilled(slot, stack);
            }
            // Updating behaviour (if npc had a sword and now has a bow, it won't
            // be able to attack otherwise.)
            this.setBehaviour(this.npcData.behaviour);

            return InteractionResult.PASS;
        } else if (
                player.getItemInHand(hand).getItem().equals(Items.POTATO) &&
                        player.isShiftKeyDown() &&
                        Taterzens.getInstance().getPlatform().checkPermission(((ServerPlayer) player).createCommandSourceStack(), "taterzens.npc.select", config.perms.npcCommandPermissionLevel)
        ) {
            // Select this taterzen
            ((ITaterzenEditor) player).selectNpc(this);

            ((ServerPlayer) player).sendSystemMessage(successText("taterzens.command.select", this.getName().getString()));

            return InteractionResult.PASS;
        } else if (((ITaterzenEditor) player).getSelectedNpc().isPresent() && ((ITaterzenEditor) player).getSelectedNpc().get() == this) {
            // Opens GUI for editing
            Taterzens.getInstance().getPlatform().openEditorGui((ServerPlayer) player);
        }

        // Limiting command usage
        if (this.npcData.minCommandInteractionTime != -1) {
            long now = System.currentTimeMillis();
            long diff = (now - this.commandTimes.getOrDefault(player.getUUID(), 0L)) / 1000;

            if (diff > this.npcData.minCommandInteractionTime || this.npcData.minCommandInteractionTime == 0) {
                this.commandTimes.put(player.getUUID(), now);
                this.commandGroups.execute((ServerPlayer) player);
            } else {
                // Inform player about the cooldown
                ((ServerPlayer) player).sendSystemMessage(
                        errorText(this.npcData.commandCooldownMessage,
                                String.valueOf(this.npcData.minCommandInteractionTime - diff)));
            }
        }

        return this.interact(player, hand);
    }


    /**
     * Sets the cooldown message.
     *
     * @param message new cooldown message.
     */
    public void setCooldownMessage(String message) {
        this.npcData.commandCooldownMessage = message;
    }

    /**
     * Sets the minimum time between command usage.
     *
     * @param time new minimum time.
     */
    public void setMinCommandInteractionTime(long time) {
        this.npcData.minCommandInteractionTime = time;
    }

/*
    // TODO: Fix the dropCustomDeathLoot code to match 1.21.  Mainly it needs Serverlevel to be provided.
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        // Additional drop check
        if (this.isEquipmentDropsAllowed())
            super.dropCustomDeathLoot(source, lootingMultiplier, allowDrops);
        else {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                this.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }
*/
    @Override
    protected boolean shouldDropLoot() {
        return this.isEquipmentDropsAllowed();
    }

    @Override
    public boolean shouldDropExperience() {
        return this.isEquipmentDropsAllowed();
    }

    /**
     * Adds the message to taterzen's message list.
     *
     * @param text message to add
     */
    public void addMessage(Component text) {
        this.addMessage(text, config.messages.messageDelay);
    }

    /**
     * Adds the message to taterzen's message list.
     *
     * @param text  message to add
     * @param delay message delay, in ticks
     */
    public void addMessage(Component text, int delay) {
        this.npcData.messages.add(new Pair<>(text, delay));
    }

    /**
     * Edits the message from taterzen's message list at index.
     *
     * @param index index of the message to edit
     * @param text  new text message
     */
    public void editMessage(int index, Component text) {
        if (index >= 0 && index < this.npcData.messages.size())
            this.npcData.messages.set(index, new Pair<>(text, config.messages.messageDelay));
    }

    /**
     * Removes message at index.
     *
     * @param index index of message to be removed.
     * @return removed message
     */
    public Component removeMessage(int index) {
        if (index < this.npcData.messages.size()) {
            return this.npcData.messages.remove(index).getFirst();
        }
        return Component.literal("");
    }

    /**
     * Sets message delay for specified message.
     * E.g. if you want to set delay for message at index 2 (you want 3rd message to appear right after second),
     * you'd set delay for index 2 to zero.
     *
     * @param index index of the message to change delay for.
     * @param delay new delay.
     */
    public void setMessageDelay(int index, int delay) {
        if (index < this.npcData.messages.size()) {
            Pair<Component, Integer> newMsg = this.npcData.messages.get(index).mapSecond(previous -> delay);
            this.npcData.messages.set(index, newMsg);
        }
    }

    public void clearMessages() {
        this.npcData.messages.clear();
    }

    /**
     * Gets {@link ArrayList} of {@link Pair}s of messages and their delays.
     *
     * @return arraylist of pairs with texts and delays.
     */
    public ArrayList<Pair<Component, Integer>> getMessages() {
        return this.npcData.messages;
    }

    /**
     * Used for disabling pushing
     *
     * @param entity colliding entity
     */
    @Override
    public void push(Entity entity) {
        if (this.getTag("Pushable", config.defaults.pushable)) {
            super.push(entity);
        }
    }

    /**
     * Used for disabling pushing
     *
     * @param entity colliding entity
     */
    @Override
    protected void doPush(Entity entity) {
        if (this.getTag("Pushable", config.defaults.pushable)) {
            super.doPush(entity);
        }
    }

    /**
     * Sets the pushable flag
     *
     * @param pushable whether Taterzen can be pushed
     */
    public void setPushable(boolean pushable) {
        this.setTag("Pushable", pushable);
    }

    /**
     * Handles received hits.
     *
     * @param attacker entity that attacked NPC.
     * @return true if attack should be cancelled.
     */
    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        if (attacker instanceof Player pl && this.isEquipmentEditor(pl)) {
            ItemStack main = this.getMainHandItem();
            this.setItemInHand(MAIN_HAND, this.getOffhandItem());
            this.setItemInHand(InteractionHand.OFF_HAND, main);
            return true;
        }
        for (TaterzenProfession profession : this.professions.values()) {
            if (profession.handleAttack(attacker)) {
                return true;
            }
        }
        return this.isInvulnerable();
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel serverLevel, DamageSource damageSource) {
        return this.isRemoved() || this.isInvulnerable() && !damageSource.is(DamageTypes.GENERIC_KILL);
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    /*
        // TODO: Fix the isLeashed code in accordance with the 1.21 changes.
    @Override
    public boolean canBeLeashed(Player player) {
        return !this.isLeashed() && this.isLeashable();
    }
    */
    /**
     * Gets whether this NPC is leashable.
     *
     * @return whether this NPC is leashable.
     */
    private boolean isLeashable() {
        return this.getTag("Leashable", config.defaults.leashable);
    }

    /**
     * Sets whether Taterzen can be leashed.
     *
     * @param leashable Taterzen leashability.
     */
    public void setLeashable(boolean leashable) {
        this.setTag("Leashable", leashable);
    }

    @Override
    public void setLeashedTo(Entity entityIn, boolean sendAttachNotification) {
        super.setLeashedTo(entityIn, sendAttachNotification);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
    }

    /**
     * Handles death of NPC.
     *
     * @param source damage source responsible for death.
     */
    @Override
    public void die(DamageSource source) {
        Pose pose = this.getPose();
        super.die(source);
        if (this.respawnPosition != null) {
            // Taterzen should be respawned instead
            this.level().broadcastEntityEvent(this, EntityEvent.DEATH);
            this.dead = false;
            this.setHealth(this.getMaxHealth());
            this.setDeltaMovement(0.0D, 0.1D, 0.0D);
            this.setPos(this.respawnPosition);
            this.setPose(pose);
        } else {
            TATERZEN_NPCS.remove(this.getUUID());

            for (TaterzenProfession profession : this.professions.values()) {
                profession.onRemove();
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        TATERZEN_NPCS.remove(this.getUUID());

        for (TaterzenProfession profession : this.professions.values()) {
            profession.onRemove();
        }
    }

    /**
     * Sets Taterzen's {@link NPCData.Behaviour}.
     *
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

        for (TaterzenProfession profession : this.professions.values()) {
            profession.onBehaviourSet(level);
        }

        switch (level) {
            case DEFENSIVE -> {
                this.targetSelector.addGoal(2, revengeGoal);
                this.setAttackGoal();
            }
            case FRIENDLY -> {
                this.targetSelector.addGoal(2, revengeGoal);
                this.targetSelector.addGoal(3, followMonstersGoal);
                this.goalSelector.addGoal(3, attackMonstersGoal);
                this.setAttackGoal();
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

        if (mainHandStack.getItem() instanceof ProjectileWeaponItem || offHandStack.getItem() instanceof ProjectileWeaponItem) {
            this.goalSelector.addGoal(3, projectileAttackGoal);
        } else {
            this.goalSelector.addGoal(3, reachMeleeAttackGoal);
        }
    }

    /**
     * Gets the Taterzen's target selector.
     *
     * @return target selector of Taterzen.
     */
    public GoalSelector getTargetSelector() {
        return this.targetSelector;
    }

    /**
     * Gets the Taterzen's goal selector.
     *
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
    /*

    //  TODO: Fix the shootProjectile code to match the 1.21 implementation

    @Override
    public void shootCrossbowProjectile(LivingEntity target, ItemStack crossbow, Projectile projectile, float multiShotSpray) {
        var weaponHand = ProjectileUtil.getWeaponHoldingHand(this, Items.CROSSBOW);
        this.startUsingItem(weaponHand);
        // Crossbow attack
        this.shootProjectile(target, projectile, multiShotSpray);
    }
    */
    @Override
    public void onCrossbowAttackPerformed() {
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        for (TaterzenProfession profession : this.professions.values()) {
            if (profession.cancelRangedAttack(target))
                return;
        }

        // Ranged attack
        var weaponHand = ProjectileUtil.getWeaponHoldingHand(this, Items.BOW);
        var bow = this.getItemInHand(weaponHand);
        ItemStack arrowType = this.getProjectile(bow);
        if (arrowType.isEmpty())
            arrowType = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.CROSSBOW)));

        AbstractArrow projectile = ProjectileUtil.getMobArrow(this, arrowType.copy(), pullProgress, null);

        //bow.use(this.level, this.fakePlayer, weaponHand);
        this.startUsingItem(weaponHand);
        this.shootProjectile(target, projectile, 0.0F);
    }

    private void shootProjectile(LivingEntity target, Projectile projectile, float multishotSpray) {
        double deltaX = target.getX() - this.getX();
        double y = target.getY(0.3333333333333333D) - projectile.getY();
        double deltaZ = target.getZ() - this.getZ();
        double planeDistance = Mth.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));

        // TODO: Fix this projectile related code to meet the 1.21 implementation
        
        // Vector3f launchVelocity = this.getProjectileShotVector(this, new Vec3(deltaX, y + planeDistance * 0.2D, deltaZ), multishotSpray);

        // projectile.shoot(launchVelocity.x(), launchVelocity.y(), launchVelocity.z(), 1.6F, 0);

        this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 0.125F);
        this.level().addFreshEntity(projectile);
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        for (TaterzenProfession profession : this.professions.values()) {
            if (profession.cancelMeleeAttack(target))
                return false;
        }
        return super.doHurtTarget(serverLevel, target);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (!this.npcData.allowSounds || this.npcData.ambientSounds.isEmpty())
            return null;

        int rnd = this.random.nextInt(this.npcData.ambientSounds.size());
        ResourceLocation sound = ResourceLocation.parse(this.npcData.ambientSounds.get(rnd));

        return BuiltInRegistries.SOUND_EVENT.getValue(sound);
    }

    public ArrayList<String> getAmbientSoundData() {
        return this.npcData.ambientSounds;
    }

    public void setAmbientSoundData(ArrayList<String> ambientSounds) {
        this.npcData.ambientSounds = ambientSounds;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (!this.npcData.allowSounds || this.npcData.hurtSounds.isEmpty())
            return null;

        int rnd = this.random.nextInt(this.npcData.hurtSounds.size());
        ResourceLocation sound = ResourceLocation.parse(this.npcData.hurtSounds.get(rnd));

        return BuiltInRegistries.SOUND_EVENT.getValue(sound);
    }

    public ArrayList<String> getHurtSoundData() {
        return this.npcData.hurtSounds;
    }

    public void setHurtSoundData(ArrayList<String> hurtSounds) {
        this.npcData.hurtSounds = hurtSounds;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (!this.npcData.allowSounds || this.npcData.deathSounds.isEmpty())
            return null;

        int rnd = this.random.nextInt(this.npcData.deathSounds.size());
        ResourceLocation sound = ResourceLocation.parse(this.npcData.deathSounds.get(rnd));

        return BuiltInRegistries.SOUND_EVENT.getValue(sound);
    }

    public ArrayList<String> getDeathSoundData() {
        return this.npcData.deathSounds;
    }

    public void setDeathSoundData(ArrayList<String> deathSounds) {
        this.npcData.deathSounds = deathSounds;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader world) {
        return 0.0F;
    }

    @Override
    protected Component getTypeName() {
        return Component.literal("-" + config.defaults.name + "-");
    }


    /**
     * Toggles whether Taterzen will drop its equipment.
     *
     * @param drop drop rule
     */
    public void allowEquipmentDrops(boolean drop) {
        this.setTag("DropsAllowed", drop);
    }

    /**
     * Gets whether Taterzen will drop its equipment.
     *
     * @return drop rule
     */
    public boolean isEquipmentDropsAllowed() {
        return this.getTag("DropsAllowed", config.defaults.dropEquipment);
    }

    /**
     * Adds {@link TaterzenProfession} to Taterzen.
     * Profession must be registered with {@link TaterzensAPI#registerProfession(ResourceLocation, Function)}.
     *
     * @param professionId ResourceLocation of the profession
     */
    public void addProfession(ResourceLocation professionId) {
        if (PROFESSION_TYPES.containsKey(professionId)) {
            this.addProfession(professionId, PROFESSION_TYPES.get(professionId).apply(this));
        } else {
            Taterzens.LOGGER.warn("Trying to add unknown profession {} to taterzen {}.", professionId, this.getName().getString());
        }
    }

    /**
     * Adds {@link TaterzenProfession} to Taterzen.
     *
     * @param professionId ResourceLocation of the profession
     * @param profession   profession object (implementing {@link TaterzenProfession})
     */
    public void addProfession(ResourceLocation professionId, TaterzenProfession profession) {
        this.professions.put(professionId, profession);
    }

    /**
     * Gets taterzen's professions.
     *
     * @return all professions ids of taterzen's professions.
     */
    public Collection<ResourceLocation> getProfessionIds() {
        return this.professions.keySet();
    }

    /**
     * Removes Taterzen's profession and triggers the corresponding {@link TaterzenProfession#onRemove()} event.
     *
     * @param professionId id of the profession that is in Taterzen's profession map.
     */
    public void removeProfession(ResourceLocation professionId) {
        TaterzenProfession toRemove = this.professions.get(professionId);

        if (toRemove != null) {
            toRemove.onProfessionRemoved();
            this.professions.remove(professionId);
        }
    }

    /**
     * Gets Taterzen's profession.
     *
     * @param professionId id of the profession that is in Taterzen's profession map.
     */
    @Nullable
    public TaterzenProfession getProfession(ResourceLocation professionId) {
        return this.professions.get(professionId);
    }

    @Override
    public boolean canPickUpLoot() {
        return true;
    }

    /**
     * Manages item pickup.
     *
     * @param item item to pick up.
     */
    @Override
    protected void pickUpItem(ServerLevel level, ItemEntity item) {
        // Profession event
        ItemStack stack = item.getItem();
        for (TaterzenProfession profession : this.professions.values()) {
            if (profession.tryPickupItem(item)) {
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
     *
     * @param pos position of block to interact with.
     * @return true if interaction was successfull, otherwise false.
     */
    public boolean interact(BlockPos pos) {
        if (this.position().distanceTo(Vec3.atCenterOf(pos)) < 4.0D && !this.level().isClientSide()) {
            this.lookAt(pos);
            this.swing(MAIN_HAND);
            // todo
            //this.level().getBlockState(pos).use(this.level(), this, MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, false));
            //this.getMainHandItem().use(this.level(), this, MAIN_HAND);
            return true;
        }
        return false;
    }

    /**
     * Makes Taterzen look at given block position.
     *
     * @param target target block to look at.
     */
    public void lookAt(BlockPos target) {
        Vec3 vec3d = this.position();
        double d = target.getX() - vec3d.x;
        double e = target.getY() - vec3d.y;
        double f = target.getZ() - vec3d.z;
        double g = Math.sqrt(d * d + f * f);
        this.setXRot(Mth.wrapDegrees((float) (-(Mth.atan2(e, g) * 57.2957763671875D))));
        this.setYBodyRot(Mth.wrapDegrees((float) (Mth.atan2(f, d) * 57.2957763671875D) - 90.0F));
        this.setYHeadRot(this.getYHeadRot());
    }

    /**
     * Sets whether Taterzen can perform jumps when in
     * proximity of target that it is attacking.
     *
     * @param jumpWhileAttacking whether to jump during attacks.
     */
    public void setPerformAttackJumps(boolean jumpWhileAttacking) {
        this.setTag("JumpAttack", jumpWhileAttacking);
    }

    /**
     * Gets follow type for taterzen.
     *
     * @return follow type
     */
    public NPCData.FollowTypes getFollowType() {
        return this.npcData.follow.type;
    }

    /**
     * Sets the target type to follow.
     * Changes movement to {@link NPCData.Movement#PATH} as well.
     *
     * @param followType type of target to follow
     */
    public void setFollowType(NPCData.FollowTypes followType) {
        if (followType != NPCData.FollowTypes.NONE) {
            this.setMovement(NPCData.Movement.TICK);
        }
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
     * Gets the UUID of the entity that taterzen is following.
     *
     * @return entity UUID if following, otherwise null.
     */
    @Nullable
    public UUID getFollowUuid() {
        return this.npcData.follow.targetUuid;
    }

    /**
     * Sets the target uuid to follow.
     *
     * @param followUuid uuid of target to follow
     */
    public void setFollowUuid(@Nullable UUID followUuid) {
        this.npcData.follow.targetUuid = followUuid;
    }

    /**
     * Whether this Taterzen should make sound.
     *
     * @param allowSounds whether to allow sounds or not.
     */
    public void setAllowSounds(boolean allowSounds) {
        this.npcData.allowSounds = allowSounds;
    }

    /**
     * Sets which skin layers should be shown to clients
     *
     * @param skinLayers byte of skin layers, see wiki.wg for more info.
     */
    public void setSkinLayers(byte skinLayers) {
        this.npcData.skinLayers = skinLayers;
    }


    /**
     * Sets the respawn position for taterzen. Can be null to disable respawning.
     *
     * @param respawnPos new respawn position.
     */
    public void setRespawnPos(@Nullable Vec3 respawnPos) {
        this.respawnPosition = respawnPos;
    }

    /**
     * Sets whether taterzen should be able to fly.
     *
     * @param allowFlight whether to allow taterzen to fly or not.
     */
    public void setAllowFlight(boolean allowFlight) {
        this.setTag("AllowFlight", allowFlight);

        if (allowFlight) {
            this.moveControl = new FlyingMoveControl(this, 20, false);
            this.navigation = new FlyingPathNavigation(this, this.level());
            this.getNavigation().setCanFloat(true);
        } else {
            this.moveControl = new MoveControl(this);
            this.navigation = new GroundPathNavigation(this, this.level());
            ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
        }
    }

    /**
     * Whether taterzen can take fall damage.
     *
     * @param fallDistance fall distance.
     * @param multiplier   damage multiplier.
     * @param source       source of damage.
     * @return true if damage should be taken, otherwise false.
     */
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return !this.getTag("AllowFlight", config.defaults.allowFlight) && super.causeFallDamage(fallDistance, multiplier, source);
    }

    /**
     * Whether taterzen should be allowed to be edited by entity.
     *
     * @param entity entity to check.
     * @return true if taterzen can be edited by entity, otherwise false.
     */
    public boolean allowEditBy(Entity entity) {
        return this.allowEditBy(entity.getUUID());
    }

    /**
     * Whether taterzen should be allowed to be edited by provided uuid.
     *
     * @param uuid uuid to check.
     * @return true if taterzen can be edited by provided uuid, otherwise false.
     */
    public boolean allowEditBy(UUID uuid) {
        return this.lockedUuid == null || this.lockedUuid.equals(uuid) || this.getUUID().equals(uuid);
    }

    /**
     * Tries to make taterzen to ride provided entity.
     *
     * @param entity entity to ride.
     * @return true if taterzen was able to ride provided entity, otherwise false.
     */
    public boolean startRiding(Entity entity) {
        if (this.getTag("AllowRiding", config.defaults.allowRiding)) {
            return this.startRiding(entity, false);
        }
        return false;
    }

    /**
     * Whether taterzen is locked.
     *
     * @return true if taterzen is locked, otherwise false.
     */
    public boolean isLocked() {
        return this.lockedUuid != null;
    }

    /**
     * Sets taterzen to be locked by provided owner's uuid.
     *
     * @param owner entity to lock taterzen to.
     */
    public void setLocked(Entity owner) {
        this.setLocked(owner.getUUID());
    }

    /**
     * Sets taterzen to be locked by provided uuid.
     *
     * @param uuid uuid to lock taterzen to.
     */
    public void setLocked(UUID uuid) {
        this.lockedUuid = uuid;
    }

    /**
     * Sets whether taterzen should be allowed to be ride entities.
     * (Mainly used for preventing them being picked up by boats / minecarts.)
     *
     * @param allow whether to allow riding or not.
     */
    public void setAllowRiding(boolean allow) {
        this.setTag("AllowRiding", allow);
    }


    @Override
    public boolean canAttack(LivingEntity target) {
        return (!(target instanceof Player) || (this.level().getDifficulty() != Difficulty.PEACEFUL || config.combatInPeaceful)) && target.canBeSeenAsEnemy();
    }

    public void setAllowSwimming(boolean allowSwimming) {
        this.goalSelector.removeGoal(this.swimGoal);
        if (allowSwimming) {
            this.goalSelector.addGoal(0, this.swimGoal);
        }
        this.setSwimming(this.isSwimming() && allowSwimming);
        this.getNavigation().setCanFloat(allowSwimming);
        this.setTag("AllowSwimming", allowSwimming);
    }

    // Change this to public, since we want to use it elsewhere
    public void setTag(String name, boolean value) {
        this.npcData.booleanTags.put(name, value);
    }

    private void resetTag(String name) {
        this.npcData.booleanTags.remove(name);
    }

    private boolean getTag(String name, boolean defaultValue) {
        if (this.npcData.booleanTags.containsKey(name))
            return this.npcData.booleanTags.get(name);
        return defaultValue;
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        // All that other work for such a pretty little line of code
        return npcData.entityList.get(npcData.playerEntity.get("Entity"));
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        // Fake selection glow
        ((ITaterzenEditor) player).getSelectedNpc().ifPresent(npc -> {
            if (this == npc && config.glowSelectedNpc) {

                // TODO: Fix this particular line.  There's issues with what's being returned
                
                //data.removeIf(value -> value.id() == Entity.DATA_SHARED_FLAGS_ID.getId());

                // Modify Taterzen to have fake glowing effect for the player
                byte flags = this.entityData.get(Entity.DATA_SHARED_FLAGS_ID);
                flags = (byte) (flags | 1 << Entity.FLAG_GLOWING);

                SynchedEntityData.DataValue<Byte> glowingTag = SynchedEntityData.DataValue.create(Entity.DATA_SHARED_FLAGS_ID, flags);
                data.add(glowingTag);
            }
        });

        // Skin layer settings

        // TODO: Fix this for the same reasons as the above commented out line.
        // data.removeIf(value -> value.id() == getPLAYER_MODE_CUSTOMISATION().getId());

        SynchedEntityData.DataValue<Byte> skinLayerTag = SynchedEntityData.DataValue.create(getPLAYER_MODE_CUSTOMISATION(), this.npcData.skinLayers);
        data.add(skinLayerTag);
    }

    @Override
    public void onBeforeSpawnPacket(ServerPlayer player, Consumer<Packet<?>> packetConsumer) {
        var packet = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED));
        packet.entries().add(new ClientboundPlayerInfoUpdatePacket.Entry(this.uuid, this.gameProfile, false, 0, GameType.SURVIVAL, null, 0, null));
        packetConsumer.accept(packet);
    }
}
