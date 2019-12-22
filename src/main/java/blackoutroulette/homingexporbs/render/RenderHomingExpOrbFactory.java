package main.java.blackoutroulette.homingexporbs.render;

import main.java.blackoutroulette.homingexporbs.entitys.EntityHomingExpOrb;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderHomingExpOrbFactory implements IRenderFactory<EntityHomingExpOrb> {

	public static final RenderHomingExpOrbFactory INSTANCE = new RenderHomingExpOrbFactory();

	@Override
	public Render<? super EntityHomingExpOrb> createRenderFor(RenderManager manager) {
		return new RenderHomingExpOrb(manager);
	}

}
