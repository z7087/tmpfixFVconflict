package me.z7087.tmpfixfvconflict.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ParticleS2CPacket.class)
public class MixinParticleS2CPacket {
    @Unique
    private boolean shouldIgnore;

    @WrapOperation(method = "<init>(Lnet/minecraft/network/RegistryByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;decode(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private Object tmpfixfvconflict$wrapReadParticle(PacketCodec<RegistryByteBuf, ParticleEffect> instance, Object _buf, Operation<ParticleEffect> original) {
        final Object result = original.call(instance, _buf);
        final RegistryByteBuf buf = (RegistryByteBuf) _buf;
        final int readableBytes = buf.readableBytes();
        if (readableBytes != 0) {
            // we got a broken packet from viaversion, skip the remaining bytes to avoid kicking the player
            buf.skipBytes(readableBytes);
            this.shouldIgnore = true;
            return null;
        }
        return result;
    }

    @Inject(method = "apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V", at = @At("HEAD"), cancellable = true)
    private void tmpfixfvconflict$cancelCallingHandler(ClientPlayPacketListener clientPlayPacketListener, CallbackInfo ci) {
        if (this.shouldIgnore)
            ci.cancel();
    }
}