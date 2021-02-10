package org.samo_lego.taterzens.mixin.accessors;


import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stat.ServerStatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerStatHandler.class)
public interface ServerStatHandlerAccessor {

	@Invoker("jsonToCompound")
	static CompoundTag json2Compound(JsonObject jsonObject) {
		throw new AssertionError();
	}
}
