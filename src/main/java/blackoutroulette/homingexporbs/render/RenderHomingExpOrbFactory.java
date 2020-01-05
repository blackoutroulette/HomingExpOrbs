package blackoutroulette.homingexporbs.render;

import blackoutroulette.homingexporbs.entitys.EntityHomingExpOrb;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHomingExpOrbFactory implements IRenderFactory<EntityHomingExpOrb> {

	public static final RenderHomingExpOrbFactory INSTANCE = new RenderHomingExpOrbFactory();

	@Override
	public Render<? super EntityHomingExpOrb> createRenderFor(RenderManager manager) {
		return new RenderHomingExpOrb(manager);
	}

}
