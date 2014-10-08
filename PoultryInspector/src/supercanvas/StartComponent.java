package supercanvas;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class StartComponent extends SuperCanvasComponent {

    private static final long serialVersionUID = 5841953202431409373L;
    
    public StartComponent() {
        super(true);
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        boolean open = getPanel().containsAny(TopLevelPaletteComponent.class);
        g.setColor(getPanel().editmode ? Color.WHITE : Color.BLACK);
        String countReport = open ? "CLOSE PALETTE" : "OPEN PALETTE";
        g.drawString(countReport, screenWidth - fontMetrics.stringWidth(countReport), screenHeight - fontMetrics.getDescent());
    }

    @Override
    public boolean contains(int x, int y) {
        return x >= getPanel().getWidth() - 100 && y >= getPanel().getHeight() - 20;
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (!getPanel().removeAll(TopLevelPaletteComponent.class)) {
            getPanel().add(new TopLevelPaletteComponent(200, 200));
        }
        return true;
    }

    @Override
    public boolean onSelect(int x, int y) {
        return onInteract(x, y);
    }

}
