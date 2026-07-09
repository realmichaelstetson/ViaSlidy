package cc.kamshi.viaslidy.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AddServerScreen.class, priority = 9999)
public abstract class AddServerScreenMixin extends Screen {

    protected AddServerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        ScreenAccessor accessor = (ScreenAccessor) (Object) this;

        // Hide default ViaFabricPlus button/widgets based on class name or text
        for (net.minecraft.client.gui.Drawable drawable : accessor.getDrawables()) {
            if (drawable instanceof ClickableWidget widget) {
                String className = widget.getClass().getName();
                String name = className.toLowerCase();
                String text = widget.getMessage().getString().toLowerCase();
                if (name.contains("viafabric") || name.contains("viaversion") || name.contains("florianmichael") || name.contains("vialegacy") || name.contains("viabedrock")
                        || text.contains("viafabric") || text.contains("viaversion") || text.contains("version")) {
                    widget.visible = false;
                    widget.active = false;
                }
            }
        }
    }
}
