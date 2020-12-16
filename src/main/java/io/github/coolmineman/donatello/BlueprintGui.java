package io.github.coolmineman.donatello;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class BlueprintGui extends LightweightGuiDescription {

    public BlueprintGui(ItemStack stack) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        WButton slab = new WButton(new TextureIcon(new Identifier("donatello:textures/gui/slab.png")));
        slab.setOnClick(() -> setBlueprintId(stack, 0));
        root.add(slab, 0, 0);

        WButton stair = new WButton(new TextureIcon(new Identifier("donatello:textures/gui/stair.png")));
        stair.setOnClick(() -> setBlueprintId(stack, 1));
        root.add(stair, 2, 0);

        WButton stair_invert = new WButton(new TextureIcon(new Identifier("donatello:textures/gui/stair_invert.png")));
        stair_invert.setOnClick(() -> setBlueprintId(stack, 2));
        root.add(stair_invert, 0, 2);

        root.validate(this);
    }

    private static void setBlueprintId(ItemStack stack, int i) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeInt(i);
        ClientSidePacketRegistry.INSTANCE.sendToServer(BlueprintItem.PACKET_ID_2, passedData);
    }

}
