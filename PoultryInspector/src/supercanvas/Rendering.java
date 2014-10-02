package supercanvas;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Rendering {
    public static void drawBody(Color bg, Graphics2D g, int centerX, int centerY, int width, int height) {
        g.setColor(bg);
        Shape s = new RoundRectangle2D.Float(centerX - width / 2, centerY - height / 2, width, height, 15, 15);
        g.fill(s);
        g.setColor(Color.BLACK);
        g.draw(s);
    }
    
    public static void drawBody(Color bg, Graphics2D g, DraggableBoxComponent component) {
        drawBody(bg, g, component.centerX, component.centerY, component.halfWidth * 2, component.halfHeight * 2);
    }
    
    public static void drawScrollbar(Graphics2D g, boolean active, int x, int y) {
        g.setColor(active ? Color.BLUE : Color.GREEN);
        Shape s = new Rectangle(x - 3, y - 3, 6, 6);
        g.fill(s);
        g.setColor(Color.BLACK);
        g.draw(s);
    }
}
