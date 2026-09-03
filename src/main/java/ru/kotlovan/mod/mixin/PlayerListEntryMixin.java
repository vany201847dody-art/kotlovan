package ru.kotlovan.mod.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kotlovan.mod.KotlovanMod;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {

    @Inject(method = "getProfile", at = @At("RETURN"), cancellable = true)
    private void kotlovan_spoofProfile(CallbackInfoReturnable<GameProfile> cir) {
        try {
            String name = KotlovanMod.client().getName();
            if (name == null || name.isEmpty()) return;
            GameProfile orig = cir.getReturnValue();
            if (orig == null) return;
            String origName = orig.getName();
            if (origName != null && origName.equals(name)) return;
            GameProfile spoofed = new GameProfile(orig.getId(), name);
            cir.setReturnValue(spoofed);
        } catch (Throwable ignored) {
        }
    }
}
