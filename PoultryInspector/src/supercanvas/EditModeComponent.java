package supercanvas;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class EditModeComponent extends SuperCanvasComponent {

    private static final long serialVersionUID = -9102993613096088676L;

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        g.setColor(Color.WHITE);
        String countReport = getPanel().editmode ? "EDIT MODE" : "OPERATE MODE";
        g.drawString(countReport, 0, screenHeight - fontMetrics.getDescent());
    }

    @Override
    public boolean contains(int x, int y) {
        return x < 200 && y >= getPanel().getHeight() - 20;
    }

    @Override
    public boolean onInteract(int x, int y) {
        getPanel().editmode = !getPanel().editmode;
        return true;
    }

    @Override
    public boolean onSelect(int x, int y) {
        getPanel().editmode = !getPanel().editmode;
        return true;
    }

}
