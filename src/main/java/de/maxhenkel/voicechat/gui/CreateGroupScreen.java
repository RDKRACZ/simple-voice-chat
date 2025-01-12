package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.gui.widgets.CreateGroupList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

public class CreateGroupScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_create_group.png");

    private CreateGroupList playerList;
    private TextFieldWidget groupName;
    private Button createGroup;

    public CreateGroupScreen() {
        super(new TranslationTextComponent("gui.voicechat.join_create_group.title"), 195, 146);
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        buttons.clear();
        children.clear();
        minecraft.keyboardHandler.setSendRepeatsToGui(true);

        playerList = new CreateGroupList(this, 9, 49, 160, 88, () -> Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().getPlayerStates());

        groupName = new TextFieldWidget(font, guiLeft + 78, guiTop + 20, 88, 10, StringTextComponent.EMPTY);

        groupName.setMaxLength(16);
        groupName.setResponder(s -> Main.GROUP_REGEX.matcher(s).matches());

        addButton(groupName);

        createGroup = new Button(guiLeft + 169, guiTop + 15, 20, 20, new StringTextComponent("+"), button -> {
            Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().setGroup(groupName.getValue());
            minecraft.setScreen(new GroupScreen());
        });
        addButton(createGroup);
    }

    @Override
    public void tick() {
        super.tick();
        groupName.tick();
        createGroup.active = !groupName.getValue().isEmpty();
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);

        playerList.drawGuiContainerBackgroundLayer(matrixStack, delta, mouseX, mouseY);

        playerList.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        for (Widget widget : buttons) {
            widget.render(matrixStack, mouseX, mouseY, delta);
        }

        font.draw(matrixStack, new TranslationTextComponent("message.voicechat.join_create_group"), guiLeft + 8, guiTop + 5, FONT_COLOR);
        font.draw(matrixStack, new TranslationTextComponent("message.voicechat.group_name"), guiLeft + 8, guiTop + 21, FONT_COLOR);
        font.draw(matrixStack, new TranslationTextComponent("message.voicechat.join_group"), guiLeft + 8, guiTop + 38, FONT_COLOR);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(null);
            return true;
        }

        return groupName.keyPressed(keyCode, scanCode, modifiers)
                || groupName.isVisible()
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (playerList.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (playerList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (playerList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        String groupNameText = groupName.getValue();
        init(client, width, height);
        groupName.setValue(groupNameText);
    }

}
