/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package universegame;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.ImageIO;

/**
 *
 * @author GGzonzonder
 */
public class UniverseGame extends Application {
    int c = 1;//light_speed, p = mv
    static int universe_x = 100;
    int universe_y = universe_x;
    int chunk_w = 4;
    Timeline loop;
    ATOM[][] universe;
    boolean can_draw = false;
    Universe_Chunk uni = new Universe_Chunk(universe_x);
    double frameRate = 20D;
    static long step = 1L;
    static long gen = 0L;
    int color_r_min = 0;
    int color_g_min = 0;
    int color_b_min = 0;
    int color_r_max = 255;
    int color_g_max = 255;
    int color_b_max = 255;
    double color_hue = 0D;//-360~360
    double color_sat = 0D;
    double color_bright = 0D;
    double color_cont = 0D;
    static long merge_time = 0L;
    static long total_mass = Long.MAX_VALUE/1000;
    //900_000_000L, Long.MAX_VALUE, 1_586_965_312_768L, 1837L*1837L*1837L*50L
    static long mass_unit = 0L;
    Label step_label = new Label();
    Label gen_label = new Label();
    Label r_min_label = new Label();
    Label g_min_label = new Label();
    Label b_min_label = new Label();
    Label r_max_label = new Label();
    Label g_max_label = new Label();
    Label b_max_label = new Label();
    Label totalmass_label = new Label();
    Label color_Hue_label = new Label();
    Label color_Saturation_label = new Label();
    Label color_Brightness_label = new Label();
    Label color_Contrast_label = new Label();
    Label setmass_label = new Label();
    Label timetick_label = new Label();
    long start_time;
    public static ArrayList<Integer> uni_rnd_x_arr = new ArrayList();
    public static ArrayList<Integer> uni_rnd_y_arr = new ArrayList();
    ScrollBar rgb_r_min = new ScrollBar();
    ScrollBar rgb_g_min = new ScrollBar();
    ScrollBar rgb_b_min = new ScrollBar();
    ScrollBar rgb_r_max = new ScrollBar();
    ScrollBar rgb_g_max = new ScrollBar();
    ScrollBar rgb_b_max = new ScrollBar();
    ScrollBar sb_Hue = new ScrollBar();
    ScrollBar sb_Saturation = new ScrollBar();
    ScrollBar sb_Brightness = new ScrollBar();
    ScrollBar sb_Contrast = new ScrollBar();
    ColorAdjust colorAdjust = new ColorAdjust();
    TextField custom_mass = new TextField ();
    Thread t;
    Pattern dig_pattern = Pattern.compile("^\\d+$");;
    static Stage alert_msg = new Stage();
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Universe Game");
        //primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.getIcons().add(new Image(UniverseGame.class.getResourceAsStream("/res/UG_icon.png")));
        alert_msg.initModality(Modality.APPLICATION_MODAL);
        alert_msg.initStyle(StageStyle.UTILITY);
        primaryStage.setResizable(false);
        GridPane root = new GridPane();
        //root.setGridLinesVisible(true);
        
        for (int i = 0; i < 28; i++) {
            RowConstraints row = new RowConstraints(20);
            root.getRowConstraints().add(row);
        }
        for (int i = 0; i < 9; i++) {
            ColumnConstraints col = new ColumnConstraints(40);
            root.getColumnConstraints().add(col);
        }
        root.setHgap(5);
        root.setVgap(5);
        root.setAlignment(Pos.CENTER);
        Canvas canvas = new Canvas(uni.getChunk_Size()*this.chunk_w, uni.getChunk_Size()*this.chunk_w);
        Scene scene = new Scene(root, uni.getChunk_Size()*this.chunk_w+10, 700);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        uni.set_mass((universe_x/2) -1, (universe_x/2) -1, this.total_mass);
        universe = uni.get_cell();
        this.mass_unit = (long) Math.pow((double)this.total_mass/((double)this.universe_x/2D), 1D/3D) + 1L;
        //System.out.println(this.mass_unit);
        CALC c = new CALC(uni);
        t = new Thread(c);
        start_time = System.currentTimeMillis();
        t.start();
        //fps, capture screen, set mass, universe size, step, start & over time.
        
        fps(root);
        scrollbar_tool(this.rgb_r_min, this.r_min_label, 0, 0);
        scrollbar_tool(this.rgb_g_min, this.g_min_label, 1, 0);
        scrollbar_tool(this.rgb_b_min, this.b_min_label, 2, 0);
        
        scrollbar_tool(this.rgb_r_max, this.r_max_label, 3, 255);
        scrollbar_tool(this.rgb_g_max, this.g_max_label, 4, 255);
        scrollbar_tool(this.rgb_b_max, this.b_max_label, 5, 255);
        
        scrollbar_tool(this.sb_Hue, this.color_Hue_label, 10, 0);
        scrollbar_tool(this.sb_Saturation, this.color_Saturation_label, 11, 0);
        scrollbar_tool(this.sb_Brightness, this.color_Brightness_label, 12, 0);
        scrollbar_tool(this.sb_Contrast, this.color_Contrast_label, 13, 0);
        
        Button screenshot_btn = new Button();
        screenshot_btn.setText("Screenshot");
        screenshot_btn.setPrefWidth(85);
        screenshot_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                WritableImage snapshot = canvas.snapshot(params, null);
                BufferedImage image = SwingFXUtils.fromFXImage(snapshot, null);
                final java.io.File file = new java.io.File(System.currentTimeMillis()+".png");
                try {
                    ImageIO.write(image, "png", file);
                } catch (java.io.IOException e) {
                    Logger.getGlobal().warning("png save error!");
                }
            }
        });
        Button setmass_btn = new Button();
        setmass_btn.setText("Set Mass");
        setmass_btn.setPrefWidth(85);
        setmass_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                t.suspend();
                String cm = custom_mass.getText();
                if (dig_pattern.matcher(cm).matches()) {
                    //reset all
                    UniverseGame.gen = 0;
                    UniverseGame.step = 0;
                    uni.reset();
                    mass_unit = Math.max(Math.abs((long) Math.pow(Double.valueOf(cm)/((double)universe_x/2D), 1D/3D) + 1L), 1);
                    uni.set_mass((universe_x/2) -1, (universe_x/2) -1, Math.abs(Long.valueOf(cm)));
                    start_time = System.currentTimeMillis();
                } else {
                    if (!alert_msg.isShowing()) {
                        alert_msg.setTitle("Warning!");
                        Label msg_label= new Label("Please enter a positive integer.");
                        Button ok_btn= new Button("OK");
                        ok_btn.setOnAction(e -> alert_msg.hide());
                        VBox layout= new VBox(5);
                        layout.getChildren().addAll(msg_label, ok_btn);
                        layout.setAlignment(Pos.CENTER);
                        Scene scene1= new Scene(layout, 200, 100);
                        alert_msg.setScene(scene1);
                        alert_msg.showAndWait();
                    }
                }
                t.resume();
            }
        });
        
        //labels
        r_min_label.setText("Red Min("+(int)rgb_r_min.getValue()+"): ");
        r_min_label.setMaxWidth(90);
        r_min_label.setAlignment(Pos.CENTER_RIGHT);
        g_min_label.setText("Green Min("+(int)rgb_g_min.getValue()+"): ");
        g_min_label.setMaxWidth(90);
        g_min_label.setAlignment(Pos.CENTER_RIGHT);
        b_min_label.setText("Blue Min("+(int)rgb_b_min.getValue()+"): ");
        b_min_label.setMaxWidth(90);
        b_min_label.setAlignment(Pos.CENTER_RIGHT);

        r_max_label.setText("Max("+(int)this.rgb_r_max.getValue()+"): ");
        r_max_label.setMaxWidth(90);
        r_max_label.setAlignment(Pos.CENTER_RIGHT);
        g_max_label.setText("Max("+(int)this.rgb_g_max.getValue()+"): ");
        g_max_label.setMaxWidth(90);
        g_max_label.setAlignment(Pos.CENTER_RIGHT);
        b_max_label.setText("Max("+(int)this.rgb_b_max.getValue()+"): ");
        b_max_label.setMaxWidth(90);
        b_max_label.setAlignment(Pos.CENTER_RIGHT);

        color_Hue_label.setText("Hue("+(int)this.sb_Hue.getValue()+":) ");
        color_Hue_label.setMaxWidth(90);
        color_Hue_label.setAlignment(Pos.CENTER_RIGHT);
        color_Saturation_label.setText("Sat("+(int)this.sb_Saturation.getValue()+":) ");
        color_Saturation_label.setMaxWidth(90);
        color_Saturation_label.setAlignment(Pos.CENTER_RIGHT);
        color_Brightness_label.setText("Bright("+(int)this.sb_Brightness.getValue()+":) ");
        color_Brightness_label.setMaxWidth(90);
        color_Brightness_label.setAlignment(Pos.CENTER_RIGHT);
        color_Contrast_label.setText("Cont("+(int)this.sb_Contrast.getValue()+":) ");
        color_Contrast_label.setMaxWidth(90);
        color_Contrast_label.setAlignment(Pos.CENTER_RIGHT);
        step_label.setText("Step: ");
        totalmass_label.setText("Toatal Mass: " + this.total_mass);
        setmass_label.setText("Enter a Integer: ");
        setmass_label.setMaxWidth(135);
        setmass_label.setAlignment(Pos.CENTER_RIGHT);
        
        //add to root scene
        root.add(step_label, 0, 1, 4, 1);
        root.add(screenshot_btn, 7, 1, 2, 1);
        root.add(setmass_btn, 4, 1, 2, 1);
        root.add(r_min_label, 0, 19, 2, 1);
        root.add(g_min_label, 0, 20, 2, 1);
        root.add(b_min_label, 0, 21, 2, 1);
        root.add(r_max_label, 4, 19, 2, 1);
        root.add(g_max_label, 4, 20, 2, 1);
        root.add(b_max_label, 4, 21, 2, 1);
        root.add(color_Hue_label, 0, 23, 2, 1);
        root.add(color_Saturation_label, 0, 24, 2, 1);
        root.add(color_Brightness_label, 4, 23, 2, 1);
        root.add(color_Contrast_label, 4, 24, 2, 1);
        
        root.add(canvas, 0, 3, 9, 16);//0, 2, 4, 1);
        root.add(rgb_r_min, 2, 19, 2, 1);
        root.add(rgb_g_min, 2, 20, 2, 1);
        root.add(rgb_b_min, 2, 21, 2, 1);
        root.add(this.sb_Hue, 2, 23, 2, 1);
        root.add(this.sb_Saturation, 2, 24, 2, 1);
        root.add(this.sb_Brightness, 6, 23, 2, 1);
        root.add(this.sb_Contrast, 6, 24, 2, 1);
        root.add(rgb_r_max, 6, 19, 2, 1);
        root.add(rgb_g_max, 6, 20, 2, 1);
        root.add(rgb_b_max, 6, 21, 2, 1);
        
        root.add(totalmass_label, 2, 0, 6, 1);
        root.add(custom_mass, 4, 2, 6, 1);
        root.add(setmass_label, 1, 2, 3, 1);
        root.add(timetick_label, 0, 27, 3, 1);
        colorAdjust.setHue(0D);//-1~1
        colorAdjust.setSaturation(0D);
        colorAdjust.setBrightness(0D);
        colorAdjust.setContrast(0D);
        canvas.setEffect(colorAdjust);
        
        
        AnimationTimer frameRateMeter = new AnimationTimer() {
        long t = System.nanoTime();
            @Override
            public void handle(long now) {
                if (now - t >= 1_000_000*16) {//nano sec
                    drawShapes(gc);
                    t = now;
                }
            }
        };
        
        frameRateMeter.start();
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    //ATOM at = new ATOM();
    private void drawShapes(GraphicsContext gc) {
        if (this.can_draw && mass_unit > 0L) {
            IntStream.range(0, universe_x).forEach(x -> {
                IntStream.range(0, universe_y).forEach(y -> {
                    long c = this.universe[x][y].getMass();
                    int r, g, b;
                    r = 0;
                    g = 0;
                    b = 0;
                    if (c > 0) {
                        if (c >= Math.pow(this.mass_unit, 3)) {
                            r = 255;
                            g = 255;
                            b = 255;
                        } else {
                            
                            r = (int)((c/mass_unit/(Math.max(mass_unit/7, 1))/2L) % (this.color_r_max-this.color_r_min)) + this.color_r_min;
                            g = (int)((c/mass_unit/(Math.max(mass_unit/14, 1))/3L) % (this.color_g_max-this.color_g_min)) + this.color_g_min;
                            if (r > this.color_r_max) {
                                r = this.color_r_max;
                            }
                            if (g > this.color_g_max) {
                                g = this.color_g_max;
                            }
                            if (c > Math.pow(this.mass_unit, 2)*(mass_unit/7)) {
                                if (r <= 100 && g <= 100) {
                                    b = 50;
                                } else {
                                    b = 255;
                                }
                            } else {
                                b = (int)(c % (this.color_b_max-this.color_b_min)) + this.color_b_min;
                                if (b > this.color_b_max) {
                                    b = this.color_b_max;
                                }
                            }
                        }

                        if (c < 0) {
                            System.out.println(c);
                        }
                    }
                    gc.setFill(Color.rgb(r, g, b));
                    gc.fillRect(x*chunk_w, y*chunk_w, chunk_w, chunk_w);
                });
            });
            can_draw = false;
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        IntStream.range(0, universe_x).forEach(uc -> {
            uni_rnd_x_arr.add(uc);
            uni_rnd_y_arr.add(uc);
        });
        launch(args);
    }
    
    private final long[] frameTimes = new long[100];
    private int frameTimeIndex = 0 ;
    private boolean arrayFilled = false ;
    long HH, mm, ss, dft;
    NumberFormat formatter = new DecimalFormat("00");
    
    private void fps(GridPane root) {
        Label label = new Label();
        
        AnimationTimer frameRateMeter = new AnimationTimer() {
        
            @Override
            public void handle(long now) {
                long oldFrameTime = frameTimes[frameTimeIndex] ;
                frameTimes[frameTimeIndex] = now ;
                frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length ;
                if (frameTimeIndex == 0) {
                    arrayFilled = true ;
                }
                if (arrayFilled) {
                    long elapsedNanos = now - oldFrameTime ;
                    long elapsedNanosPerFrame = elapsedNanos / frameTimes.length ;
                    frameRate = 1_000_000_000D / elapsedNanosPerFrame ;
                    label.setText(String.format("FPS: %.1f", frameRate));
                }
                step_label.setText("Gen: " + Long.toString(gen) + "   Step: " + Long.toString(step));
                totalmass_label.setText("Toatal Mass: " + total_mass);
                dft = System.currentTimeMillis() - start_time;
                HH = (dft/1000/60/60) % 60;
                mm = (dft/1000/60) % 60;
                ss = (dft/1000) % 60;
                timetick_label.setText(formatter.format(HH)+":"+formatter.format(mm)+":"+formatter.format(ss));
            }
        };
        
        frameRateMeter.start();
        root.add(label, 0, 0, 2, 1);
    }
    
    public void scrollbar_tool(ScrollBar sb, Label label, int c_num, int init) {
        switch (c_num) {
            case 0:
            case 1:
            case 2:
                sb.setMin(0);
                sb.setMax(254);
                break;
            case 3:
            case 4:
            case 5:
                sb.setMin(1);
                sb.setMax(255);
                break;
            case 10:
            case 11:
            case 12:
            case 13:
                sb.setMin(-360);
                sb.setMax(360);
                break;
        }
        sb.setValue(init);
        sb.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                switch (c_num) {
                    case 0:
                        if ((int)rgb_r_max.getValue() - new_val.intValue() < 1 && new_val.intValue() < 255) {
                            color_r_min = new_val.intValue();
                            color_r_max = color_r_min + 1;
                            rgb_r_min.setValue(color_r_min);
                            rgb_r_max.setValue(color_r_max);
                            label.setText("Red Min("+new_val.intValue()+"): ");
                            r_max_label.setText("Max("+color_r_max+"): ");
                        } else {
                            color_r_min = new_val.intValue();
                            rgb_r_min.setValue(color_r_min);
                            label.setText("Red Min("+new_val.intValue()+"): ");
                        }
                        break;
                    case 1:
                        if ((int)rgb_g_max.getValue() - new_val.intValue() < 1 && new_val.intValue() < 255) {
                            color_g_min = new_val.intValue();
                            color_g_max = color_g_min + 1;
                            rgb_g_min.setValue(color_g_min);
                            rgb_g_max.setValue(color_g_max);
                            label.setText("Green Min("+new_val.intValue()+"): ");
                            g_max_label.setText("Max("+color_g_max+"): ");
                        } else {
                            color_g_min = new_val.intValue();
                            rgb_g_min.setValue(color_g_min);
                            label.setText("Green Min("+new_val.intValue()+"): ");
                        }
                        break;
                    case 2:
                        if ((int)rgb_b_max.getValue() - new_val.intValue() < 1 && new_val.intValue() < 255) {
                            color_b_min = new_val.intValue();
                            color_b_max = color_b_min + 1;
                            rgb_b_min.setValue(color_b_min);
                            rgb_b_max.setValue(color_b_max);
                            label.setText("Blue Min("+new_val.intValue()+"): ");
                            b_max_label.setText("Max("+color_b_max+"): ");
                        } else {
                            color_b_min = new_val.intValue();
                            rgb_b_min.setValue(color_b_min);
                            label.setText("Blue Min("+new_val.intValue()+"): ");
                        }
                        break;
                    case 3:
                        if (new_val.intValue() <= (int)rgb_r_min.getValue()) {
                            color_r_max = new_val.intValue();
                            color_r_min = color_r_max - 1;
                            rgb_r_min.setValue(color_r_min);
                            rgb_r_max.setValue(color_r_max);
                            label.setText("Max("+new_val.intValue()+"): ");
                            r_min_label.setText("Red Min("+color_r_min+"): ");
                        } else {
                            color_r_max = new_val.intValue();
                            rgb_r_max.setValue(color_r_max);
                            rgb_r_min.setValue(color_r_min);
                            label.setText("Max("+new_val.intValue()+"): ");
                            r_min_label.setText("Red Min("+color_r_min+"): ");
                        }
                        break;
                    case 4:
                        if (new_val.intValue() <= (int)rgb_g_min.getValue() && new_val.intValue() > 0) {
                            color_g_max = new_val.intValue();
                            color_g_min = color_g_max - 1;
                            rgb_g_min.setValue(color_g_min);
                            rgb_g_max.setValue(color_g_max);
                            label.setText("Max("+new_val.intValue()+"): ");
                            g_min_label.setText("Red Min("+new_val.intValue()+"): ");
                        } else {
                            if (new_val.intValue() > 0) {
                                color_g_max = new_val.intValue();
                                rgb_g_max.setValue(color_g_max);
                                label.setText("Max("+new_val.intValue()+"): ");
                            }
                        }
                        break;
                    case 5:
                        if (new_val.intValue() <= (int)rgb_b_min.getValue() && new_val.intValue() > 0) {
                            color_b_max = new_val.intValue();
                            color_b_min = color_b_max - 1;
                            rgb_b_min.setValue(color_b_min);
                            rgb_b_max.setValue(color_b_max);
                            label.setText("Max("+new_val.intValue()+"): ");
                            b_min_label.setText("Red Min("+new_val.intValue()+"): ");
                        } else {
                            if (new_val.intValue() > 0) {
                                color_b_max = new_val.intValue();
                                rgb_b_max.setValue(color_b_max);
                                label.setText("Max("+new_val.intValue()+"): ");
                            }
                        }
                        break;
                    case 10:
                        sb_Hue.setValue(new_val.intValue());
                        label.setText("Hue("+new_val.intValue()+":) ");
                        colorAdjust.setHue(new_val.doubleValue()/360D);
                        break;
                    case 11:
                        sb_Saturation.setValue(new_val.intValue());
                        label.setText("Sat("+new_val.intValue()+":) ");
                        colorAdjust.setSaturation(new_val.doubleValue()/360D);
                        break;
                    case 12:
                        sb_Brightness.setValue(new_val.intValue());
                        label.setText("Bright("+new_val.intValue()+":) ");
                        colorAdjust.setBrightness(new_val.doubleValue()/360D);
                        break;
                    case 13:
                        sb_Contrast.setValue(new_val.intValue());
                        label.setText("Cont("+new_val.intValue()+":) ");
                        colorAdjust.setContrast(new_val.doubleValue()/360D);
                        break;
                }
            }
        });
    }
    
    public void textarea_tool(TextArea ta, String text) {
        ta.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    //do nothing temporary
                }
            }
        });
    }
    
    class CALC implements Runnable {
        Universe_Chunk uni;
        
        public CALC(Universe_Chunk uni) {
            this.uni = uni;
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    if (!can_draw) {
                        this.uni.update();
                        can_draw = true;
                        step ++;
                    }
                    Thread.sleep(1L);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UniverseGame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
}
