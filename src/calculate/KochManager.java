/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsf31kochfractalfx.JSF31KochFractalFX;
import timeutil.TimeStamp;

/**
 *
 * @author rick-
 */
public class KochManager implements Observer{
    private JSF31KochFractalFX app;
    private int level = 1;
    private KochFractal kf;
    private BlockingQueue<Edge> EdgesQ = new LinkedBlockingQueue<>();
    
    private TimeStamp ts = new TimeStamp();
    
    private ExecutorService pool;
    private CountDownLatch lat;
    
    public KochManager(JSF31KochFractalFX app){
        pool = Executors.newFixedThreadPool(3);
               
        //save the data
        this.app = app;
        kf = new KochFractal(this);
        //create the koch fractal
        kf.addObserver(this);
        kf.setLevel(level);
        
        //set the value
        changeLevel(level);
    }
    
    public void changeLevel(int value){
        //set the level
        level=value;
        kf.setLevel(level);
        
        //check  if there are new edges or the amount is the same
        if(kf.getNrOfEdges() == EdgesQ.size()){
            app.requestDrawEdges();
            return;
        }
        
        //starte timing the calculating
        ts = new TimeStamp();
        ts.setBegin();
        //stop any previous calculation and clear the list
        kf.cancel();
        EdgesQ.clear();
        
        //set the number of edges
        app.setTextNrEdges(kf.getNrOfEdges() + "");
        //clear the calc times
        app.setTextCalc("");
        app.setTextDraw("");
        //create 3 threads for the calculations
        lat = new CountDownLatch(3);
        pool.execute(()->kf.generateBottomEdge(lat));
        pool.execute(()->kf.generateLeftEdge(lat));
        pool.execute(()->kf.generateRightEdge(lat));
//<editor-fold defaultstate="collapsed" desc="Via Callable and Future">
//        //Requires change in KochFractal
//        //*3
//        Future<List<Edge>> fbe = pool.submit(new Callable<List<Edge>>(){
//
//            private List<Edge> edges = new LinkedList<>();
//
//            @Override
//            public List<Edge> call() throws Exception {
//                //generate the edges
//                kf.generateBottomEdge(edges);
//                //wait for evry calculation
//                lat.await();
//                return edges;
//            }
//        });
//        //in render
//        pool.execute(()->{
//            try{lat.await();}
//            catch(InterruptedException ex){
//                Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            ts.setEnd();
//            EdgesQ.addAll(fbe.get());
//            app.requestDrawEdges();
//        });
//</editor-fold>
        pool.execute(()->{
            try {
                lat.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            ts.setEnd();
            app.requestDrawEdges();
        });
    }
    
    public void drawEdges(){
        //clear the pannel
        app.clearKochPanel();
        //set the calc time
        app.setTextCalc(ts.toString());
        //time the drawing
        TimeStamp ts2 = new TimeStamp();
        ts2.setBegin();
        //draw each edge
        EdgesQ.forEach((e)->app.drawEdge(e));
        //end the time and put it on the screen
        ts2.setEnd();
        app.setTextDraw(ts2.toString());
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof KochFractal && arg instanceof Edge){
            EdgesQ.add((Edge)arg);
        }
    }
    
    public void addEdge(Edge e){
        EdgesQ.add(e);
    }
    
    public void stop(){
        kf.cancel();
        pool.shutdown();
    }
    
    
}
