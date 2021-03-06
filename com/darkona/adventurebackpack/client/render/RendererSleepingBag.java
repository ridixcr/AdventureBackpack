package com.darkona.adventurebackpack.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.darkona.adventurebackpack.client.models.ModelSleepingBag;
import com.darkona.adventurebackpack.common.Textures;

public class RendererSleepingBag extends TileEntitySpecialRenderer {

	private final ModelSleepingBag model;
	private final ResourceLocation modelTexture = Textures.resourceRL("/textures/backpack/sleepingBag.png");

	public RendererSleepingBag() {
		model = new ModelSleepingBag();
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		int dir = tileentity.getWorldObj().getBlockMetadata(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord);
		if((dir & 8) >= 8) return;
		if((dir & 4) >= 4)dir-=4;

		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.1F, (float) z + 0.5F);
		bindTexture(modelTexture);

		GL11.glPushMatrix();
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

		GL11.glPushMatrix();
		if (dir == 0)
			GL11.glRotatef(0F, 0.0F, 1.0F, 0.0F);
		if (dir % 2 != 0)
			GL11.glRotatef(dir * (90F), 0.0F, 1.0F, 0.0F);
		if (dir == 2)
			GL11.glRotatef(-180F, 0.0F, 1.0F, 0.0F);

		model.render((Entity) null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.05F);

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}

}
