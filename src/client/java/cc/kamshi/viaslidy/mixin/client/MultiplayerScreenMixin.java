package cc.kamshi.viaslidy.mixin.client;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = MultiplayerScreen.class, priority = 9999)
public abstract class MultiplayerScreenMixin extends Screen {

    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        ScreenAccessor accessor = (ScreenAccessor) (Object) this;

        // Hide default ViaFabricPlus button/widgets or any other widgets based on class name or text
        for (net.minecraft.client.gui.Drawable drawable : accessor.getDrawables()) {
            if (drawable instanceof ClickableWidget widget) {
                if (widget instanceof VersionSliderWidget) {
                    continue;
                }
                String className = widget.getClass().getName();
                if (className.contains("ServerList")) {
                    continue;
                }
                String name = className.toLowerCase();
                String text = widget.getMessage().getString().toLowerCase();
                if (name.contains("viafabric") || name.contains("viaversion") || name.contains("florianmichael") || name.contains("vialegacy") || name.contains("viabedrock")
                        || text.contains("viafabric") || text.contains("viaversion") || text.contains("version")) {
                    widget.visible = false;
                    widget.active = false;
                }
            }
        }

        // Initialize the version list
        List<ProtocolVersion> protocols = new ArrayList<>();
        protocols.add(ProtocolTranslator.AUTO_DETECT_PROTOCOL);

        for (ProtocolVersion pv : ProtocolVersion.getProtocols()) {
            if (pv.isKnown() && (pv.getVersionType() == VersionType.RELEASE || pv.getVersionType() == VersionType.RELEASE_INITIAL)) {
                protocols.add(pv);
            }
        }

        if (protocols.size() > 1) {
            int sliderWidth = 150;
            int sliderHeight = 20;
            int x = this.width - sliderWidth - 5;
            int y = 5;

            this.addDrawableChild(new VersionSliderWidget(x, y, sliderWidth, sliderHeight, protocols));
        }
    }

    private static class VersionSliderWidget extends SliderWidget {
        private final List<ProtocolVersion> protocols;
        private ProtocolVersion lastAppliedVersion;

        public VersionSliderWidget(int x, int y, int width, int height, List<ProtocolVersion> protocols) {
            super(x, y, width, height, Text.empty(), getInitialValue(protocols));
            this.protocols = protocols;
            int index = (int) Math.round(this.value * (protocols.size() - 1));
            this.lastAppliedVersion = protocols.get(index);
            updateMessage();
        }

        private static double getInitialValue(List<ProtocolVersion> protocols) {
            ProtocolVersion current = ProtocolTranslator.getTargetVersion();
            int index = protocols.indexOf(current);
            if (index == -1) {
                index = 0;
            }
            return (double) index / (protocols.size() - 1);
        }

        @Override
        protected void updateMessage() {
            int index = (int) Math.round(this.value * (protocols.size() - 1));
            if (index >= 0 && index < protocols.size()) {
                ProtocolVersion pv = protocols.get(index);
                this.setMessage(Text.literal("Version: " + pv.getName()));
            }
        }

        @Override
        protected void applyValue() {
            // Do not apply version change here to prevent lag while dragging.
            // Version will be applied on release.
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            super.onRelease(mouseX, mouseY);
            applyCurrentVersion();
        }

        private void applyCurrentVersion() {
            int index = (int) Math.round(this.value * (protocols.size() - 1));
            if (index >= 0 && index < protocols.size()) {
                ProtocolVersion pv = protocols.get(index);
                if (pv != lastAppliedVersion) {
                    ProtocolTranslator.setTargetVersion(pv);
                    lastAppliedVersion = pv;
                }
            }
        }
    }
}
