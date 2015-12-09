/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package generator.calculate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;

/**
 *
 * @author Peter Boots
 */
public class KochFractal {

    private KochManager km;
    
    private int level = 1;      // The current level of the fractal
    private int nrOfEdges = 3;  // The number of edges in the current level of the fractal
    //private float hue;          // Hue value of color for next edge
    private AtomicBoolean cancelled = new AtomicBoolean(false);  // Flag to indicate that calculation has been cancelled 
    

    public KochFractal(KochManager km) {
        this.km = km;
    }

    
    
    private void drawKochEdge(double ax, double ay, double bx, double by, int n, float[] hue,char pos,KochManager.updateCallback cb) throws InterruptedException {
        if (!cancelled.get()) {
            if (n == 1) {
                hue[0] = hue[0] + 1.0f / nrOfEdges;
                Edge e = new Edge(ax, ay, bx, by, Color.hsb(hue[0]*360.0, 1.0, 1.0));
                cb.update(e);
            } else {
                double angle = Math.PI / 3.0 + Math.atan2(by - ay, bx - ax);
                double distabdiv3 = Math.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay)) / 3;
                double cx = Math.cos(angle) * distabdiv3 + (bx - ax) / 3 + ax;
                double cy = Math.sin(angle) * distabdiv3 + (by - ay) / 3 + ay;
                final double midabx = (bx - ax) / 3 + ax;
                final double midaby = (by - ay) / 3 + ay;
                drawKochEdge(ax, ay, midabx, midaby, n - 1,hue,pos,cb);
                drawKochEdge(midabx, midaby, cx, cy, n - 1,hue,pos,cb);
                drawKochEdge(cx, cy, (midabx + bx) / 2, (midaby + by) / 2, n - 1,hue,pos,cb);
                drawKochEdge((midabx + bx) / 2, (midaby + by) / 2, bx, by, n - 1,hue,pos,cb);
            }
        }else{
            System.out.println("Stopping!");
        }
    }

    public void generateLeftEdge(CountDownLatch lat, KochManager.updateCallback cb) {
        float hue = 0f;
        cancelled.set(false);
        try {
            drawKochEdge(0.5, 0.0, (1 - Math.sqrt(3.0) / 2.0) / 2, 0.75, level, new float[]{hue},'L',cb);
        } catch (InterruptedException ex) {
            Logger.getLogger(KochFractal.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        lat.countDown();
    }

    public void generateBottomEdge(CountDownLatch lat,KochManager.updateCallback cb) {
        float hue = 1f / 3f;
        cancelled.set(false);
        try {
            drawKochEdge((1 - Math.sqrt(3.0) / 2.0) / 2, 0.75, (1 + Math.sqrt(3.0) / 2.0) / 2, 0.75, level, new float[]{hue},'B',cb);
        } catch (InterruptedException ex) {
            Logger.getLogger(KochFractal.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        lat.countDown();
    }

    public void generateRightEdge(CountDownLatch lat,KochManager.updateCallback cb) {
        float hue = 2f / 3f;
        cancelled.set(false);
        try {
            drawKochEdge((1 + Math.sqrt(3.0) / 2.0) / 2, 0.75, 0.5, 0.0, level, new float[]{hue},'R',cb);
        } catch (InterruptedException ex) {
            Logger.getLogger(KochFractal.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        lat.countDown();
    }
    
    public void cancel() {
        cancelled.set(true);
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
