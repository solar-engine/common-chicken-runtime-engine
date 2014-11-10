package ccre.supercanvas.components;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.SuperCanvasComponent;

public class ChannelConfiguratorComponent<Channel> extends DraggableBoxComponent {

    private final Channel channel;
    private final Class<Channel> channelType;

    public ChannelConfiguratorComponent(int cx, int cy, String name, Channel channel, Class<Channel> channelType) {
        super(cx, cy, true);
        this.channel = channel;
        this.channelType = channelType;
    }

    private static final long serialVersionUID = -2497890491682902790L;

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        
    }
    
    @Override
    public boolean onSelect(int x, int y) {
        return onInteract(x, y) || super.onSelect(x, y);
    }

    @Override
    public boolean onInteract(int x, int y) {
        return false;
    }
}
