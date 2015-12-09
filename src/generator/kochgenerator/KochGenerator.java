/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package generator.calculate.kochgenerator;

import generator.calculate.KochManager;

import java.io.IOException;

/**
 *
 * @author Rick Rongen, www.R-Ware.tk
 */
public class KochGenerator {

    private static Boolean WAIT_EDGE = true;
    private static int LEVEL = 1;
    
    public static final Boolean getWaitEdge(){
        return WAIT_EDGE;
    }
    
    public static final int getLevel(){
        return LEVEL;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        for(int i = 0; i<args.length; i++){
            if(args[i].equalsIgnoreCase("-W")){
                WAIT_EDGE = false;
            }
            if(args[i].equalsIgnoreCase("-l")){
                if(i==args.length-1){
                    System.out.println("Error parsing Level Missing argument");
                    System.exit(-1);
                }
                try{
                    LEVEL = Integer.valueOf(args[++i]);
                }catch(NumberFormatException e){
                    System.out.println("Error parsing Level NaN");
                    System.exit(-2);
                }
            }
        }
        KochManager km = new KochManager();
        km.changeLevel(LEVEL);
        km.setFile("outFile.edg");
        
        km.calculateAndSave();
        
        System.out.println("Took: " + km.getTime());
    }
    
}
