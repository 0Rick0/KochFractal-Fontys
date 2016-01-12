/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf31kochfractalfx;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import timeutil.TimeStamp;

/**
 *
 * @author Nico Kuijpers
 */
public class JSF31KochFractalFX extends Application {

    // Zoom and drag
    private double zoomTranslateX = 0.0;
    private double zoomTranslateY = 0.0;
    private double zoom = 1.0;
    private double startPressedX = 0.0;
    private double startPressedY = 0.0;
    private double lastDragX = 0.0;
    private double lastDragY = 0.0;

    // Current level of Koch fractal
    private int currentLevel = 1;

    // Labels for level, nr edges, calculation time, and drawing time
    private Label labelLevel;
    private Label labelNrEdges;
    private Label labelNrEdgesText;

    // Koch panel and its size
    private Canvas kochPanel;
    private final int kpWidth = 500;
    private final int kpHeight = 500;

    private RandomAccessFile result = null;

    @Override
    public void start(Stage primaryStage) {

        // Define grid pane
        GridPane grid;
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // For debug purposes
        // Make de grid lines visible
        // grid.setGridLinesVisible(true);
        // Drawing panel for Koch fractal
        kochPanel = new Canvas(kpWidth, kpHeight);
        grid.add(kochPanel, 0, 3, 25, 1);

        // Labels to present number of edges for Koch fractal
        labelNrEdges = new Label("Nr edges:");
        labelNrEdgesText = new Label();
        grid.add(labelNrEdges, 0, 0, 4, 1);
        grid.add(labelNrEdgesText, 3, 0, 22, 1);

        // Label to present current level of Koch fractal
        labelLevel = new Label("Level: " + currentLevel);
        grid.add(labelLevel, 0, 6);

        // Button to fit Koch fractal in Koch panel
        Button buttonFitFractal = new Button();
        buttonFitFractal.setText("Fit Fractal");
        buttonFitFractal.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fitFractalButtonActionPerformed(event);
            }
        });
        grid.add(buttonFitFractal, 14, 6);

        TextField level = new TextField();

        level.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("\\d*")) {
                int value = Integer.parseInt(newValue);
            } else {
                level.setText(oldValue);
            }
        });
        
        grid.add(level, 1, 6);
        
        Button btGet = new Button("get edges direct");
        btGet.setOnAction((e)->{
            try {
                System.out.println("Write direct command");
                outObj.writeByte(0x01);
                outObj.writeByte(Integer.parseInt(level.getText()));
                outObj.writeDouble(zoom);
                outObj.flush();
                System.out.println("Send!");
            } catch (IOException ex) {
                Logger.getLogger(JSF31KochFractalFX.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        grid.add(btGet,2,6);
        Button btGetC = new Button("get edges cached");
        btGetC.setOnAction((e)->{
            try {
                System.out.println("Write cached command");
                outObj.writeByte(0x02);
                outObj.writeByte(Integer.parseInt(level.getText()));
                outObj.writeDouble(zoom);
                outObj.flush();
                System.out.println("Send!");
            } catch (IOException ex) {
                Logger.getLogger(JSF31KochFractalFX.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        grid.add(btGetC,3,6);
        
        // Add mouse clicked event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                kochPanelMouseClicked(event);
            }
        });

        // Add mouse pressed event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_PRESSED,
                new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                kochPanelMousePressed(event);
            }
        });

        // Add mouse dragged event to Koch panel
        kochPanel.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                kochPanelMouseDragged(event);
            }
        });

        // Create Koch manager and set initial level
        resetZoom();
        //--------------------------------------------------------------------------------------------
//        kochManager = new KochManager(this);
//        kochManager.changeLevel(currentLevel);

        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene = new Scene(root, kpWidth + 50, kpHeight + 170);
        root.getChildren().add(grid);

        Thread socketThread = new Thread(()->{
            try {
                socketThread ();
            } catch (IOException ex) {
                Logger.getLogger(JSF31KochFractalFX.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        socketThread.start();
        
        // Define title and assign the scene for main window
        primaryStage.setTitle("Koch Fractal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private OutputStream out;
    private DataOutputStream outObj;
    
    private void socketThread() throws IOException{
        Socket socket = new Socket("localhost",4568);
        InputStream in = socket.getInputStream();
        DataInputStream inObj = new DataInputStream(in);
        out = socket.getOutputStream();
        outObj = new DataOutputStream(out);
        String server = inObj.readUTF();
        if(!server.equalsIgnoreCase("RDES")){
            Logger.getLogger(JSF31KochFractalFX.class.getName()).log(Level.SEVERE,"Invallid server!",server);
            System.exit(-1);
        }
        System.out.println("Header accepted");
        while(socket.isConnected()&&!socket.isClosed()){
            int input = inObj.readByte();
            switch (input) {
                case 0xFF:
                    System.out.println("ERROR!");
                    break;
                case 0x01:
                case 0x02:
                    int lev = inObj.readByte();
                    int nrOfEdges = (int) (3 * Math.pow(4, lev - 1));
                    Platform.runLater(()->clearKochPanel());
                    for(int i = 0; i<nrOfEdges;i++){
                        drawEdge(inObj);
                    }   break;
                default:
                    System.out.println("UNSUPPORTED " + input);
                    break;
            }
        }
    }
    
    public void clearKochPanel() {
        GraphicsContext gc = kochPanel.getGraphicsContext2D();
        gc.clearRect(0.0, 0.0, kpWidth, kpHeight);
        gc.setFill(Color.BLACK);
        gc.fillRect(0.0, 0.0, kpWidth, kpHeight);
    }

    private void drawEdge(DataInput input) throws IOException{
        Edge edg = new Edge(input.readDouble(),input.readDouble(),input.readDouble(),input.readDouble(),//XYXY
                Color.hsb(input.readDouble(),input.readDouble(),input.readDouble()));//HSB
        Platform.runLater(()->drawEdge(edg));
    }
    
    public void drawEdge(Edge e) {
        // Graphics
        GraphicsContext gc = kochPanel.getGraphicsContext2D();

        // Adjust edge for zoom and drag
        Edge e1 = edgeAfterZoomAndDrag(e);

        // Set line color
        gc.setStroke(e1.color);

        // Set line width depending on level
        if (currentLevel <= 3) {
            gc.setLineWidth(2.0);
        } else if (currentLevel <= 5) {
            gc.setLineWidth(1.5);
        } else {
            gc.setLineWidth(1.0);
        }

        // Draw line
        gc.strokeLine(e1.X1, e1.Y1, e1.X2, e1.Y2);
    }

    public void setTextNrEdges(String text) {
        labelNrEdgesText.setText(text);
    }

    private void fitFractalButtonActionPerformed(ActionEvent event) {
        resetZoom();
        //kochManager.drawEdges();------------------------------------------------------------------
    }

    private void kochPanelMouseClicked(MouseEvent event) {
        if (Math.abs(event.getX() - startPressedX) < 1.0
                && Math.abs(event.getY() - startPressedY) < 1.0) {
            double originalPointClickedX = (event.getX() - zoomTranslateX) / zoom;
            double originalPointClickedY = (event.getY() - zoomTranslateY) / zoom;
            if (event.getButton() == MouseButton.PRIMARY) {
                zoom *= 2.0;
            } else if (event.getButton() == MouseButton.SECONDARY) {
                zoom /= 2.0;
            }
            zoomTranslateX = (int) (event.getX() - originalPointClickedX * zoom);
            zoomTranslateY = (int) (event.getY() - originalPointClickedY * zoom);
            //kochManager.drawEdges();----------------------------------------------------------
        }
    }

    private void kochPanelMouseDragged(MouseEvent event) {
        zoomTranslateX = zoomTranslateX + event.getX() - lastDragX;
        zoomTranslateY = zoomTranslateY + event.getY() - lastDragY;
        lastDragX = event.getX();
        lastDragY = event.getY();
        //kochManager.drawEdges();---------------------------------------------------------------
    }

    private void kochPanelMousePressed(MouseEvent event) {
        startPressedX = event.getX();
        startPressedY = event.getY();
        lastDragX = event.getX();
        lastDragY = event.getY();
    }

    private void resetZoom() {
        int kpSize = Math.min(kpWidth, kpHeight);
        zoom = kpSize;
        zoomTranslateX = (kpWidth - kpSize) / 2.0;
        zoomTranslateY = (kpHeight - kpSize) / 2.0;
    }

    private Edge edgeAfterZoomAndDrag(Edge e) {
        return new Edge(
                e.X1 + zoomTranslateX,
                e.Y1 + zoomTranslateY,
                e.X2 + zoomTranslateX,
                e.Y2 + zoomTranslateY,
                e.color);
//        return new Edge(
//                e.X1 * zoom + zoomTranslateX,
//                e.Y1 * zoom + zoomTranslateY,
//                e.X2 * zoom + zoomTranslateX,
//                e.Y2 * zoom + zoomTranslateY,
//                e.color);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
