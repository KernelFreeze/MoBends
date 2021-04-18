package goblinbob.mobends.mixin;

import goblinbob.mobends.interfaces.IModelBox;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBox.class)
public class MixinModelBox implements IModelBox {
    private int texU;
    private int texV;

    @Inject(method = "<init>(Lnet/minecraft/client/model/ModelRenderer;IIFFFIIIFZ)V", at = @At("RETURN"))
    public void onInit(ModelRenderer renderer, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror, CallbackInfo ci) {
        this.texU = texU;
        this.texV = texV;
    }

    @Override
    public int getTexU() {
        return texU;
    }

    @Override
    public int getTexV() {
        return texV;
    }
}
