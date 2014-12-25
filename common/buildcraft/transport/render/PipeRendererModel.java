package buildcraft.transport.render;

import javax.vecmath.Vector3f;
import tv.twitch.Core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.google.common.primitives.Ints;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import buildcraft.BuildCraftTransport;
import buildcraft.core.CoreConstants;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe;

public class PipeRendererModel implements IBakedModel, ISmartBlockModel, ISmartItemModel {
	private IExtendedBlockState state;
	private ItemStack stack;
	private boolean isItem;

	public PipeRendererModel() {

	}

	private PipeRendererModel(IBlockState state) {
		this.state = (IExtendedBlockState) state;
		isItem = false;
	}

	private PipeRendererModel(ItemStack stack) {
		this.stack = stack;
		isItem = true;
	}

	@Override
	public IBakedModel handleBlockState(IBlockState state) {
		return new PipeRendererModel(state);
	}

	private int[] vertexToInts(float x, float y, float z, int color, TextureAtlasSprite texture, float u, float v) {
		return new int[] {
				Float.floatToRawIntBits(x),
				Float.floatToRawIntBits(y),
				Float.floatToRawIntBits(z),
				color,
				Float.floatToRawIntBits(texture.getInterpolatedU(u)),
				Float.floatToRawIntBits(texture.getInterpolatedV(v)),
				0
		};
	}
	
	@Override
	public List getFaceQuads(EnumFacing p_177551_1_) {
		return Collections.emptyList();
	}

	private boolean[] isConnected = new boolean[6];
	private TextureAtlasSprite[] textures = new TextureAtlasSprite[7];

	private float[][][] quadVertexes = new float[][][]{
			{
					{0.25F, 0.75F, 0.25F},
					{0.75F, 0.75F, 0.25F},
					{0.75F, 1.00F, 0.25F},
					{0.25F, 1.00F, 0.25F},
			},
			{
					{0.25F, 0.75F, 0.25F},
					{0.25F, 0.75F, 0.75F},
					{0.25F, 1.00F, 0.75F},
					{0.25F, 1.00F, 0.25F},
			},
			{
					{0.25F, 0.75F, 0.75F},
					{0.75F, 0.75F, 0.75F},
					{0.75F, 1.00F, 0.75F},
					{0.25F, 1.00F, 0.75F},
			},
			{
					{0.75F, 0.75F, 0.25F},
					{0.75F, 0.75F, 0.75F},
					{0.75F, 1.00F, 0.75F},
					{0.75F, 1.00F, 0.25F},
			}
	};

	private void createTwoSidedBakedQuad(List<BakedQuad> quads, float x1, float x2, float z1, float z2, float y, TextureAtlasSprite texture, EnumFacing side)
	{
		Vec3 v1 = rotate(new Vec3(x1 - .5, y - .5, z1 - .5), side).addVector(.5, .5, .5);
		Vec3 v2 = rotate(new Vec3(x1 - .5, y - .5, z2 - .5), side).addVector(.5, .5, .5);
		Vec3 v3 = rotate(new Vec3(x2 - .5, y - .5, z2 - .5), side).addVector(.5, .5, .5);
		Vec3 v4 = rotate(new Vec3(x2 - .5, y - .5, z1 - .5), side).addVector(.5, .5, .5);
		int[] data = Ints.concat(
				vertexToInts((float) v1.xCoord, (float) v1.yCoord, (float) v1.zCoord, -1, texture, x1 * 16, z1 * 16),
				vertexToInts((float) v2.xCoord, (float) v2.yCoord, (float) v2.zCoord, -1, texture, x1 * 16, z2 * 16),
				vertexToInts((float) v3.xCoord, (float) v3.yCoord, (float) v3.zCoord, -1, texture, x2 * 16, z2 * 16),
				vertexToInts((float) v4.xCoord, (float) v4.yCoord, (float) v4.zCoord, -1, texture, x2 * 16, z1 * 16)
		);
		quads.add(new BakedQuad(data, -1, side));
		quads.add(new BakedQuad(data, -1, side.getOpposite()));
	}

	private static Vec3 rotate(Vec3 vec, EnumFacing side)
	{
		switch(side)
		{
			case DOWN: return new Vec3( vec.xCoord, -vec.yCoord, -vec.zCoord);
			case UP: return new Vec3( vec.xCoord, vec.yCoord, vec.zCoord);
			case NORTH: return new Vec3( vec.xCoord, vec.zCoord, -vec.yCoord);
			case SOUTH: return new Vec3( vec.xCoord, -vec.zCoord, vec.yCoord);
			case WEST: return new Vec3(-vec.yCoord, vec.xCoord, vec.zCoord);
			case EAST: return new Vec3( vec.yCoord, -vec.xCoord, vec.zCoord);
		}
		return null;
	}

	private EnumFacing getSide(Vec3 a, Vec3 b, Vec3 c) {
		int dir = a.yCoord == b.yCoord && b.yCoord == c.yCoord ? 0 : (a.xCoord == b.xCoord && b.xCoord == c.xCoord ? 2 : 4);
		if (dir == 0) {
			dir += (c.yCoord >= 0.5) ? 1 : 0;
		} else if (dir == 2) {
			dir += (c.xCoord >= 0.5) ? 1 : 0;
		} else if (dir == 4) {
			dir += (c.zCoord >= 0.5) ? 1 : 0;
		}
		return EnumFacing.getFront(dir);
	}

	@Override
	public List getGeneralQuads() {
		LinkedList<BakedQuad> quads = new LinkedList<BakedQuad>();

		float min = CoreConstants.PIPE_MIN_POS;
		float max = CoreConstants.PIPE_MAX_POS;

		if (this.state != null) {
			PipeRenderState renderState = this.state.getValue(TileGenericPipe.RENDER_STATE_PROP);
			if (renderState != null) {
				for (int i = 0; i < 6; i++) {
					isConnected[i] = renderState.pipeConnectionMatrix.isConnected(EnumFacing.getFront(i));
					if (isConnected[i]) {
						textures[i] = BuildCraftTransport.pipeIconProvider.getIcon(renderState.textureMatrix.getTextureIndex(EnumFacing.getFront(i)));
					}
				}
				textures[6] = BuildCraftTransport.pipeIconProvider.getIcon(renderState.textureMatrix.getTextureIndex(null));
			} else {
				textures[6] = BuildCraftTransport.pipeIconProvider.getIcon(0);
				for (int i = 0; i < 6; i++) {
					isConnected[i] = false;
				}
			}
		} else {
			for (int i = 0; i < 6; i++) {
				isConnected[i] = i < 2;
			}
			textures[0] = BuildCraftTransport.pipeIconProvider.getIcon(((ItemPipe) stack.getItem()).getPipeIconIndex());
			textures[1] = BuildCraftTransport.pipeIconProvider.getIcon(((ItemPipe) stack.getItem()).getPipeIconIndex());
			textures[6] = BuildCraftTransport.pipeIconProvider.getIcon(((ItemPipe) stack.getItem()).getPipeIconIndex());
		}

		for (EnumFacing f : EnumFacing.values()) {
			if (!isConnected[f.ordinal()]) {
				createTwoSidedBakedQuad(quads, min, max, min, max, max, textures[6], f);
			} else {
				// This renderer is so bad...
				TextureAtlasSprite sprite = textures[f.ordinal()];
				for (float[][] v : quadVertexes) {
					Vec3 v1 = rotate(new Vec3(v[0][0] - .5, v[0][1] - .5, v[0][2] - .5), f).addVector(.5, .5, .5);
					Vec3 v2 = rotate(new Vec3(v[1][0] - .5, v[1][1] - .5, v[1][2] - .5), f).addVector(.5, .5, .5);
					Vec3 v3 = rotate(new Vec3(v[2][0] - .5, v[2][1] - .5, v[2][2] - .5), f).addVector(.5, .5, .5);
					Vec3 v4 = rotate(new Vec3(v[3][0] - .5, v[3][1] - .5, v[3][2] - .5), f).addVector(.5, .5, .5);
					EnumFacing side = getSide(v1, v2, v3);
					int[] data = Ints.concat(
							vertexToInts((float) v1.xCoord, (float) v1.yCoord, (float) v1.zCoord, -1, sprite, 4, 0),
							vertexToInts((float) v2.xCoord, (float) v2.yCoord, (float) v2.zCoord, -1, sprite, 12, 0),
							vertexToInts((float) v3.xCoord, (float) v3.yCoord, (float) v3.zCoord, -1, sprite, 12, 4),
							vertexToInts((float) v4.xCoord, (float) v4.yCoord, (float) v4.zCoord, -1, sprite, 4, 4)
					);
					quads.add(new BakedQuad(data, -1, side));
					//quads.add(new BakedQuad(data, -1, side.getOpposite()));
				}
				if (isItem && f.ordinal() < 2) {
					createTwoSidedBakedQuad(quads, min, max, min, max, 1.0F, textures[f.ordinal()], f);
				}
			}
		}
		return quads;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getTexture() {
		return PipeIconProvider.TYPE.PipeFluidsCobblestone.getIcon();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		return new PipeRendererModel(stack);
	}
}
