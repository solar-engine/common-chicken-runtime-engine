package supercanvas;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class Rendering {
    public static void drawBody(Color bg, Graphics2D g, int centerX, int centerY, int width, int height) {
        g.setColor(bg);
        Shape s = new Rectangle2D.Float(centerX - width / 2, centerY - height / 2, width, height);
        g.fill(s);
        g.setColor(Color.BLACK);
        Stroke stroke = g.getStroke();
        //g.setStroke(new BasicStroke(1.5f));
        g.draw(s);
        g.setStroke(stroke);
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
