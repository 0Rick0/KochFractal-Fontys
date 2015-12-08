/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package generator.calculate;

import timeutil.TimeStamp;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rick-
 */
public class KochManager {


    public static interface updateCallback {
        public void update(Edge e) throws InterruptedException;
    }

    private int level = 1;
    private KochFractal kf;
    private BlockingQueue<Edge> edgesQ = new LinkedBlockingQueue<>();

    private TimeStamp ts = new TimeStamp();

    private ExecutorService pool;
    private CountDownLatch lat;

    private File outputFile;

    Runnable lt, rt, bt;
    Runnable rEnd;

    public KochManager() {
        pool = Executors.newFixedThreadPool(3);

        //save the data
        kf = new KochFractal(this);
        //create the koch fractal
        kf.setLevel(level);

        //set the value
        changeLevel(level);
    }

    public boolean setFile(String filename) {
        try {
            outputFile = new File(filename);
            if (!outputFile.exists() && !outputFile.createNewFile()) {
                outputFile = null;
                return false;
            }
            if (!outputFile.canWrite()) {
                outputFile = null;
                return false;
            }
            return true;
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
            outputFile = null;
            return false;
        }
    }

    public void changeLevel(int value) {
        //set the level
        level = value;
        kf.setLevel(level);


    }

    public void calculateAndSave() throws IOException {
        System.out.println("Generating edges " + kf.getNrOfEdges() + " for level " + kf.getLevel());
        //starte timing the calculating
        ts = new TimeStamp();
        ts.setBegin();
        //stop any previous calculation and clear the list
        //not nessesairy, only executed once
//        kf.cancel();
//        if(lt!=null){
//            lt.cancel(true);
//            rt.cancel(true);
//            bt.cancel(true);
//            rEnd.cancel(true);
//            while(lt.isRunning()||rt.isRunning()||bt.isRunning()||rEnd.isRunning())try {
//                Thread.sleep(1);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
//                }
//        }


        OutputStream str;
        try {
            str = new FileOutputStream(outputFile, false);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        BufferedOutputStream bout = new BufferedOutputStream(str);

        ObjectOutputStream dout = new ObjectOutputStream(bout);

        try {
            dout.writeInt(level);
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        edgesQ.clear();

        //create 3 threads for the calculations
        lat = new CountDownLatch(3);

        lt = new Runnable() {
            private final List<Edge> pEdges = new LinkedList<>();

            @Override
            public void run() {
                kf.generateLeftEdge(lat, (Edge e) -> {
                    pEdges.add(e);
                    edgesQ.add(e);
                    synchronized (dout) {
                        try {
                            write(e, dout);
                        } catch (IOException ex) {
                            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!generator.calculate.kochgenerator.KochGenerator.getWaitEdge()) return;
                    if (kf.getLevel() < 6) {
                        Thread.sleep(1);
                    } else if (kf.getLevel() >= 6 && kf.getLevel() < 8) {
                        Thread.sleep(0, 10);
                    } else {
                        Thread.sleep(0, 1);
                    }
                });
            }
        };
        rt = new Runnable() {
            private final List<Edge> pEdges = new LinkedList<>();

            @Override
            public void run() {
                kf.generateRightEdge(lat, (Edge e) -> {
                    pEdges.add(e);
                    edgesQ.add(e);
                    synchronized (dout) {
                        try {
                            write(e, dout);
                        } catch (IOException ex) {
                            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!generator.calculate.kochgenerator.KochGenerator.getWaitEdge()) return;
                    if (kf.getLevel() < 6) {
                        Thread.sleep(1);
                    } else if (kf.getLevel() >= 6 && kf.getLevel() < 8) {
                        Thread.sleep(0, 10);
                    } else {
                        Thread.sleep(0, 1);
                    }
                });
                return;
            }
        };
        bt = new Runnable() {
            private final List<Edge> pEdges = new LinkedList<>();

            @Override
            public void run() {
                kf.generateBottomEdge(lat, (Edge e) -> {
                    pEdges.add(e);
                    edgesQ.add(e);
                    synchronized (dout) {
                        try {
                            write(e, dout);
                        } catch (IOException ex) {
                            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!generator.calculate.kochgenerator.KochGenerator.getWaitEdge()) return;
                    if (kf.getLevel() < 6) {
                        Thread.sleep(1);
                    } else if (kf.getLevel() >= 6 && kf.getLevel() < 8) {
                        Thread.sleep(0, 10);
                    } else {
                        Thread.sleep(0, 1);
                    }
                });
            }
        };

        pool.execute(bt);
        pool.execute(lt);
        pool.execute(rt);

        try {
            lat.await();
            ts.setEnd();
            pool.shutdown();
        } catch (InterruptedException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        dout.close();
        bout.close();
        str.close();
    }

    private void write(Edge e, ObjectOutputStream dout) throws IOException {
        dout.writeDouble(e.X1);
        dout.writeDouble(e.Y1);
        dout.writeDouble(e.X2);
        dout.writeDouble(e.Y2);
        dout.writeDouble(e.color.getHue());
        dout.writeDouble(e.color.getSaturation());
        dout.writeDouble(e.color.getSaturation());
    }

    public void addEdge(Edge e, char pos) {
        edgesQ.add(e);
    }

    public void stop() {
        kf.cancel();
        pool.shutdown();
    }

    public String getTime() {
        return ts.toString();
    }


}
