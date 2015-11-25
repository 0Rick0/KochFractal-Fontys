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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import jsf31kochfractalfx.JSF31KochFractalFX;
import timeutil.TimeStamp;

/**
 *
 * @author rick-
 */
public class KochManager implements Observer{
    public static interface updateCallback{
        public void update(Edge e) throws InterruptedException;
    }
    
    private JSF31KochFractalFX app;
    private int level = 1;
    private KochFractal kf;
    private BlockingQueue<Edge> edgesQ = new LinkedBlockingQueue<>();
    
    private TimeStamp ts = new TimeStamp();
    
    private ExecutorService pool;
    private CountDownLatch lat;
    
    private AtomicBoolean anybodyDrawing = new AtomicBoolean(false);
    
    Task lt,rt,bt;
    Task rEnd;
    
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
        if(kf.getNrOfEdges() == edgesQ.size()){
            app.requestDrawEdges(true);
            return;
        }
        
        //starte timing the calculating
        ts = new TimeStamp();
        ts.setBegin();
        //stop any previous calculation and clear the list
        kf.cancel();
        if(lt!=null){
            lt.cancel(true);
            rt.cancel(true);
            bt.cancel(true);
            rEnd.cancel(true);
            while(lt.isRunning()||rt.isRunning()||bt.isRunning()||rEnd.isRunning())try {
                Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
        
        
        edgesQ.clear();
        
        //set the number of edges
        app.setTextNrEdges(kf.getNrOfEdges() + "");
        //clear the calc times
        app.setTextCalc("");
        app.setTextDraw("");
        //create 3 threads for the calculations
        lat = new CountDownLatch(3);
        
        lt = new Task() {
            private final List<Edge> pEdges = new LinkedList<>();
            @Override
            protected Object call() throws Exception {
                kf.generateLeftEdge(lat, (Edge e) -> {
                    pEdges.add(e);
                    updateProgress(pEdges.size(),kf.getNrOfEdges()/3);
                    updateMessage("Left: " + pEdges.size() + "/"+kf.getNrOfEdges()/3);
                    edgesQ.add(e);
                    Platform.runLater(()->app.drawEdge(new Edge(e.X1,e.Y1,e.X2,e.Y2,Color.WHITE)));
                    
                    if(kf.getLevel()<6){
                        Thread.sleep(1);
                    }else if(kf.getLevel()>=6 && kf.getLevel()<8){
                        Thread.sleep(0, 10);
                    }else{
                        Thread.sleep(0,1);
                    }
                });
                return null;
            }
        };
        rt = new Task() {
            private final List<Edge> pEdges = new LinkedList<>();
            @Override
            protected Object call() throws Exception {
                kf.generateRightEdge(lat, (Edge e) -> {
                    pEdges.add(e);
                    updateProgress(pEdges.size(),kf.getNrOfEdges()/3);
                    updateMessage("Right: " + pEdges.size() + "/"+kf.getNrOfEdges()/3);
                    edgesQ.add(e);
                    Platform.runLater(()->app.drawEdge(new Edge(e.X1,e.Y1,e.X2,e.Y2,Color.WHITE)));
                    
                    if(kf.getLevel()<6){
                        Thread.sleep(1);
                    }else if(kf.getLevel()>=6 && kf.getLevel()<8){
                        Thread.sleep(0, 10);
                    }else{
                        Thread.sleep(0,1);
                    }
                });
                return null;
            }
        };
        bt = new Task() {
            private final List<Edge> pEdges = new LinkedList<>();
            @Override
            protected Object call() throws Exception {
                kf.generateBottomEdge(lat, (Edge e) -> {
                    pEdges.add(e);
                    updateProgress(pEdges.size(),kf.getNrOfEdges()/3);
                    updateMessage("Bottom: " + pEdges.size() + "/"+kf.getNrOfEdges()/3);
                    edgesQ.add(e);
                    Platform.runLater(()->app.drawEdge(new Edge(e.X1,e.Y1,e.X2,e.Y2,Color.WHITE)));
                    
                    if(kf.getLevel()<6){
                        Thread.sleep(1);
                    }else if(kf.getLevel()>=6 && kf.getLevel()<8){
                        Thread.sleep(0, 10);
                    }else{
                        Thread.sleep(0,1);
                    }
                });
                return null;
            }
        };
        
        app.BindPropB(bt);
        app.BindPropL(lt);
        app.BindPropR(rt);
        
        pool.execute(bt);
        pool.execute(lt);
        pool.execute(rt);
//        pool.execute(()->kf.generateBottomEdge(lat));
//        pool.execute(()->kf.generateLeftEdge(lat));
//        pool.execute(()->kf.generateRightEdge(lat));
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
//            edgesQ.addAll(fbe.get());
//            app.requestDrawEdges();
//        });
//</editor-fold>
        rEnd = new Task() {
            @Override
            protected Object call() throws Exception {
                try{
                    lat.await();
                } catch (InterruptedException ex) {
                    Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
                    if(this.isCancelled())return null;
                }
                ts.setEnd();
                app.requestDrawEdges(true);
                return null;
                }
            };
        pool.execute(rEnd);
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
        edgesQ.forEach((e)->app.drawEdge(e));
        //end the time and put it on the screen
        ts2.setEnd();
        app.setTextDraw(ts2.toString());
        app.doneDrawing();
    }
    
    public void drawEdgesPreview(){
        app.clearKochPanel();
        new LinkedBlockingQueue<>(edgesQ).forEach((e)->app.drawEdge(new Edge(e.X1,e.Y1,e.X2,e.Y2,Color.WHITE)));
        anybodyDrawing.set(false);
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof KochFractal && arg instanceof Edge){
            edgesQ.add((Edge)arg);
        }
    }
    
    public void addEdge(Edge e,char pos){
        edgesQ.add(e);
    }
    
    public void stop(){
        kf.cancel();
        pool.shutdown();
    }
    
    
}
