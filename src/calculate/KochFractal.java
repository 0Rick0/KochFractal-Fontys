/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.Observable;
import java.util.concurrent.CountDownLatch;
import javafx.scene.paint.Color;

/**
 *
 * @author Peter Boots
 */
public class KochFractal extends Observable {

    private KochManager km;
    
    private int level = 1;      // The current level of the fractal
    private int nrOfEdges = 3;  // The number of edges in the current level of the fractal
    //private float hue;          // Hue value of color for next edge
    private volatile boolean cancelled;  // Flag to indicate that calculation has been cancelled 

    public KochFractal(KochManager km) {
        this.km = km;
    }

    
    
    private void drawKochEdge(double ax, double ay, double bx, double by, int n, float[] hue) {
        if (!cancelled) {
            if (n == 1) {
                hue[0] = hue[0] + 1.0f / nrOfEdges;
                Edge e = new Edge(ax, ay, bx, by, Color.hsb(hue[0]*360.0, 1.0, 1.0));
                km.addEdge(e);
            } else {
                double angle = Math.PI / 3.0 + Math.atan2(by - ay, bx - ax);
                double distabdiv3 = Math.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay)) / 3;
                double cx = Math.cos(angle) * distabdiv3 + (bx - ax) / 3 + ax;
                double cy = Math.sin(angle) * distabdiv3 + (by - ay) / 3 + ay;
                final double midabx = (bx - ax) / 3 + ax;
                final double midaby = (by - ay) / 3 + ay;
                drawKochEdge(ax, ay, midabx, midaby, n - 1,hue);
                drawKochEdge(midabx, midaby, cx, cy, n - 1,hue);
                drawKochEdge(cx, cy, (midabx + bx) / 2, (midaby + by) / 2, n - 1,hue);
                drawKochEdge((midabx + bx) / 2, (midaby + by) / 2, bx, by, n - 1,hue);
            }
        }else{
            System.out.println("Stopping!");
        }
    }

    public void generateLeftEdge(CountDownLatch lat) {
        float hue = 0f;
        cancelled = false;
        drawKochEdge(0.5, 0.0, (1 - Math.sqrt(3.0) / 2.0) / 2, 0.75, level, new float[]{hue});
        lat.countDown();
    }

    public void generateBottomEdge(CountDownLatch lat) {
        float hue = 1f / 3f;
        cancelled = false;
        drawKochEdge((1 - Math.sqrt(3.0) / 2.0) / 2, 0.75, (1 + Math.sqrt(3.0) / 2.0) / 2, 0.75, level, new float[]{hue});
        lat.countDown();
    }

    public void generateRightEdge(CountDownLatch lat) {
        float hue = 2f / 3f;
        cancelled = false;
        drawKochEdge((1 + Math.sqrt(3.0) / 2.0) / 2, 0.75, 0.5, 0.0, level, new float[]{hue});
        lat.countDown();
    }
    
    public void cancel() {
        cancelled = true;
    }

    public void setLevel(int lvl) {
        level = lvl;
        nrOfEdges = (int) (3 * Math.pow(4, level - 1));
    }

    public int getLevel() {
        return level;
    }

    public int getNrOfEdges() {
        return nrOfEdges;
    }
}
