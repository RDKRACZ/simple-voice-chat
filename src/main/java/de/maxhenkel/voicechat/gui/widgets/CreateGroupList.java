package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.corelib.client.PlayerSkins;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.gui.GroupScreen;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;
import java.util.function.Supplier;

public class CreateGroupList extends WidgetBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_create_group.png");

    protected Supplier<List<PlayerState>> playerStates;
    protected int offset;
    private VoiceChatScreenBase.HoverArea[] hoverAreas;
    private int columnHeight;
    private int columnCount;

    public CreateGroupList(VoiceChatScreenBase screen, int posX, int posY, int xSize, int ySize, Supplier<List<PlayerState>> playerStates) {
        super(screen, posX, posY, xSize, ySize);
        this.playerStates = playerStates;
        columnHeight = 22;
        columnCount = 4;

        hoverAreas = new VoiceChatScreenBase.HoverArea[columnCount];
        for (int i = 0; i < hoverAreas.length; i++) {
            hoverAreas[i] = new VoiceChatScreenBase.HoverArea(0, i * columnHeight, xSize, columnHeight);
        }
    }

    public List<Group> getGroups() {
        Map<String, Group> groups = new HashMap<>();
        List<PlayerState> playerStates = this.playerStates.get();

        for (PlayerState state : playerStates) {
            if (!state.hasGroup()) {
                continue;
            }

            Group group = groups.getOrDefault(state.getGroup(), new Group(state.getGroup()));
            group.members.add(state);
            group.members.sort(Comparator.comparing(o -> o.getGameProfile().getName()));
            groups.put(state.getGroup(), group);
        }
        return new ArrayList<>(groups.values());
    }

    @Override
    public void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        List<Group> entries = getGroups();
        for (int i = getOffset(); i < entries.size() && i < getOffset() + columnCount; i++) {
            int pos = i - getOffset();
            VoiceChatScreenBase.HoverArea hoverArea = hoverAreas[pos];
            int startY = guiTop + pos * columnHeight;
            Group group = entries.get(i);
            StringTextComponent groupName = new StringTextComponent(group.name);
            mc.font.draw(matrixStack, groupName, guiLeft + 3, startY + 7, 0);

            int textWidth = mc.font.width(groupName);

            int headsPerRow = (xSize - (3 + textWidth + 3 + 3)) / (8 + 1);

            for (int j = 0; j < group.members.size(); j++) {
                PlayerState state = group.members.get(j);
                int headXIndex = j % headsPerRow;
                int headYIndex = j / headsPerRow;

                if (headYIndex > 1) {
                    break;
                }

                int headPosX = guiLeft + xSize - 8 - 2 - headXIndex * 9;
                int headPosY = startY + 2 + 10 - 10 * headYIndex;

                matrixStack.pushPose();
                mc.getTextureManager().bind(PlayerSkins.getSkin(state.getGameProfile()));
                matrixStack.translate(headPosX, headPosY, 0);
                Screen.blit(matrixStack, 0, 0, 8, 8, 8, 8, 64, 64);
                Screen.blit(matrixStack, 0, 0, 40, 8, 8, 8, 64, 64);
                matrixStack.popPose();
            }

            if (hoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                List<IReorderingProcessor> tooltip = new ArrayList<>();
                tooltip.add(new TranslationTextComponent("message.voicechat.group_members").withStyle(TextFormatting.WHITE).getVisualOrderText());
                for (PlayerState state : group.members) {
                    tooltip.add(new StringTextComponent("- " + state.getGameProfile().getName()).withStyle(TextFormatting.GRAY).getVisualOrderText());
                }
                screen.renderTooltip(matrixStack, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        List<Group> entries = getGroups();
        for (int i = getOffset(); i < entries.size() && i < getOffset() + columnCount; i++) {
            mc.getTextureManager().bind(TEXTURE);
            int pos = i - getOffset();
            VoiceChatScreenBase.HoverArea hoverArea = hoverAreas[pos];
            boolean hovered = hoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY);
            int startY = guiTop + pos * columnHeight;
            Group group = entries.get(i);

            if (hovered) {
                Screen.blit(matrixStack, guiLeft, startY, 195, 39, 160, columnHeight, 512, 512);
            } else {
                Screen.blit(matrixStack, guiLeft, startY, 195, 17, 160, columnHeight, 512, 512);
            }
        }

        mc.getTextureManager().bind(TEXTURE);

        if (entries.size() > columnCount) {
            float h = ySize - 17;
            float perc = (float) getOffset() / (float) (entries.size() - columnCount);
            int posY = guiTop + (int) (h * perc);
            Screen.blit(matrixStack, guiLeft + xSize + 6, posY, 195, 0, 12, 17, 512, 512);
        } else {
            Screen.blit(matrixStack, guiLeft + xSize + 6, guiTop, 207, 0, 12, 17, 512, 512);
        }
    }

    public int getOffset() {
        List<Group> entries = getGroups();
        if (entries.size() <= columnCount) {
            offset = 0;
        } else if (offset > entries.size() - columnCount) {
            offset = entries.size() - columnCount;
        }
        return offset;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<Group> entries = getGroups();
        if (entries.size() > columnCount) {
            if (delta < 0D) {
                offset = Math.min(getOffset() + 1, entries.size() - columnCount);
            } else {
                offset = Math.max(getOffset() - 1, 0);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<Group> entries = getGroups();
        for (int i = 0; i < hoverAreas.length; i++) {
            if (getOffset() + i >= entries.size()) {
                break;
            }
            if (!hoverAreas[i].isHovered(guiLeft, guiTop, (int) mouseX, (int) mouseY)) {
                continue;
            }
            Group group = entries.get(getOffset() + i);
            mc.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
            Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().setGroup(group.name);
            mc.setScreen(new GroupScreen());
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public static class Group {
        private String name;
        private List<PlayerState> members;

        public Group(String name) {
            this.name = name;
            this.members = new ArrayList<>();
        }
    }

}
