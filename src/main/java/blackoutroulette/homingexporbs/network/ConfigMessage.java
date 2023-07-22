package blackoutroulette.homingexporbs.network;

import blackoutroulette.homingexporbs.ConfigHandler;
import blackoutroulette.homingexporbs.HomingExpOrbs;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ConfigMessage implements IMessage {

    protected int homingMaxRange;

    public ConfigMessage(){}

    public ConfigMessage(int homingMaxRange){
        this.homingMaxRange = homingMaxRange;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.homingMaxRange = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.homingMaxRange);
    }

    public static class ConfigMessageHandler  implements IMessageHandler<ConfigMessage, IMessage> {

        @Override
        public IMessage onMessage(ConfigMessage message, MessageContext ctx) {

            final ConfigHandler config = ConfigHandler.getInstance();
            config.setHomingMaxRange(message.homingMaxRange);
            HomingExpOrbs.logger.info("Set homing range to: " + message.homingMaxRange);

            return null;
        }
    }

}
