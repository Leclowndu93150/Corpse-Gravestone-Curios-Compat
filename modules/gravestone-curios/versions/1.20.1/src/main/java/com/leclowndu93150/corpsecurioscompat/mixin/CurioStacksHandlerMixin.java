package com.leclowndu93150.corpsecurioscompat.mixin;

import com.leclowndu93150.corpsecurioscompat.duck.ICuriosAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

@Mixin(value = CurioStacksHandler.class, remap = false)
public abstract class CurioStacksHandlerMixin implements ICuriosAccessor {

    @Shadow
    private boolean update;

    @Shadow
    public abstract void update();

    @Override
    public void gravestonecurioscompat$forceSlotRebuild() {
        this.update = true;
        this.update();
    }
}
