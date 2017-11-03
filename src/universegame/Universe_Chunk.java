/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package universegame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;


/**
 *
 * @author GGzonzonder
 */
public class Universe_Chunk {
    private int chunk_size;
    ATOM[][] cell;
    public static ArrayList<Long> mass_arr = new ArrayList();
    Queue<ATOM> update_queue = new ConcurrentLinkedQueue<ATOM>();
    
    
    public Universe_Chunk(int chunk_size) {
        init(chunk_size);
    }
    
    public void init(int chunk_size) {
        this.chunk_size = chunk_size;
        cell = new ATOM[chunk_size][chunk_size];
        for (int x = 0; x < chunk_size ; x++) {
            for (int y = 0; y < chunk_size ; y++) {
                cell[x][y] = new ATOM(x, y);
            }
        }
        IntStream.range(0, 50).forEach(arr -> mass_arr.add(1837L*arr));
        IntStream.range(0, 850).forEach(arr -> mass_arr.add(1L));
        IntStream.range(0, 50).forEach(arr -> mass_arr.add(1836L));
        IntStream.range(0, 50).forEach(arr -> mass_arr.add(1837L));
        Collections.shuffle(mass_arr);
    }
    
    public void reset() {
        for (int x = 0; x < chunk_size ; x++) {
            for (int y = 0; y < chunk_size ; y++) {
                cell[x][y].reset();
            }
        }
        mass_arr.clear();
        IntStream.range(0, 50).forEach(arr -> mass_arr.add(1837L*arr));
        IntStream.range(0, 850).forEach(arr -> mass_arr.add(1L));
        IntStream.range(0, 50).forEach(arr -> mass_arr.add(1836L));
        IntStream.range(0, 50).forEach(arr -> mass_arr.add(1837L));
        Collections.shuffle(mass_arr);
    }
    
    public ATOM[][] get_cell() {
        return this.cell;
    }
    
    public void set_mass(int x, int y, long mass) {
        cell[x][y].setMass(mass);
    }
    
    public Queue<ATOM> get_update_Queue() {
        return this.update_queue;
    }
    
    double merge_mass_max = 2D;
    double merge_mass_min = 0.5D;
    public void update() {
        IntStream.range(0, this.chunk_size).parallel().forEach(x -> {
            IntStream.range(0, this.chunk_size).parallel().forEach(y -> {
                cell[x][y].calc_gravity_pos();
            });
        });
        Collections.shuffle(mass_arr);
        //fiss or fus
        //fiss 1837L*1837L*1837L~1837L*1837L*1837L*1837L
        //fus 1837L*1837L*1837L*1837L~
        int fiss_width = 3;
        IntStream.range(0, this.chunk_size).parallel().forEach(x -> {
            IntStream.range(0, this.chunk_size).parallel().forEach(y -> {
                int gp = cell[x][y].get_gravity_pos();
                long gm = cell[x][y].getMass();
                if (gm >= Math.pow(UniverseGame.mass_unit, 3) && gm <= (long)(Math.pow(UniverseGame.mass_unit, 3)*(((double)this.chunk_size/2D)-0.001D)) && gp == 5) {//fus
                    long tgm = gm/(long)(Math.pow(fiss_width*2+1, 2));
                    cell[x][y].set_fission(true);
                    UniverseGame.gen ++;
                    for (int sx = -fiss_width; sx <= fiss_width ; sx ++) {
                        for (int sy = -fiss_width; sy <= fiss_width; sy ++) {
                            //skip if it is out of bound or in the mid
                            //side check
                            if ((x + sx >= 0 && x + sx <= this.chunk_size-1) && (y + sy >= 0 && y + sy <= this.chunk_size-1)) {
                                cell[x][y].setPreDecayMass(tgm);
                                cell[x + sx][y + sy].setPreMergeMass(tgm);
                                cell[x + sx][y + sy].set_fission(true);
                            }
                        }
                    }
                }
            });
        });
        
        //decay
        IntStream.range(0, this.chunk_size).parallel().forEach(x -> {
            IntStream.range(0, this.chunk_size).parallel().forEach(y -> {
                int gp = cell[x][y].get_gravity_pos();
                long gm = cell[x][y].getMass();
                if (gm > 1 && gp != 5 && !cell[x][y].get_fission()) {
                    ArrayList<Integer> rndsx = new ArrayList();
                    rndsx.add(-1);
                    rndsx.add(0);
                    rndsx.add(1);
                    ArrayList<Integer> rndsy = new ArrayList();
                    rndsy.add(1);
                    rndsy.add(0);
                    rndsy.add(-1);
                    Collections.shuffle(rndsx);
                    Collections.shuffle(rndsy);
                    for (int cx = 0; cx < 3 ; cx ++) {
                        for (int cy = 0; cy < 3; cy ++) {
                            //skip if it is out of bound or in the mid
                            int sx = rndsx.get(cx);
                            int sy = rndsy.get(cy);
                            //side check
                            if ((x + sx >= 0 && x + sx <= this.chunk_size-1) && (y + sy >= 0 && y + sy <= this.chunk_size-1) && gm > cell[x + sx][y + sy].getMass()) {
                                //check others gravity pos
                                long ogm = cell[x + sx][y + sy].getMass();
                                //do merge or decay
                                long dm = cell[x][y].Decay(ogm);
                                if (dm > 0 && !cell[x + sx][y + sy].get_fission()) {
                                    cell[x][y].setPreDecayMass(dm);
                                    cell[x + sx][y + sy].setPreMergeMass(dm);
                                }
                            }
                        }
                    }
                }
            });
        });
               
        //merge
        Collections.shuffle(UniverseGame.uni_rnd_x_arr);
        Collections.shuffle(UniverseGame.uni_rnd_y_arr);
        IntStream.range(0, this.chunk_size).parallel().forEach(x -> {
            IntStream.range(0, this.chunk_size).parallel().forEach(y -> {
                //side check
                double center_g_x = 0;
                double center_g_y = 0;
                int xx = UniverseGame.uni_rnd_x_arr.get(x);
                int yy = UniverseGame.uni_rnd_y_arr.get(y);
                
                int gp = cell[xx][yy].get_gravity_pos();
                long gm = cell[xx][yy].getLeftMass();
                //System.out.println("---------------------");
                ArrayList<Integer> rndsx = new ArrayList();
                rndsx.add(-1);
                rndsx.add(0);
                rndsx.add(1);
                ArrayList<Integer> rndsy = new ArrayList();
                rndsy.add(1);
                rndsy.add(0);
                rndsy.add(-1);
                Collections.shuffle(rndsx);
                Collections.shuffle(rndsy);
                if (gm > 1) {
                    for (int cx = 0; cx < 3; cx ++) {
                        for (int cy = 0; cy < 3; cy ++) {
                            //skip if it is out of bound or in the mid
                            int sx = rndsx.get(cx);
                            int sy = rndsy.get(cy);
                            if ((xx + sx >= 0 && xx + sx <= this.chunk_size-1) && (yy + sy >= 0 && yy + sy <= this.chunk_size-1) && (!cell[xx][yy].get_fission() || !cell[xx + sx][yy + sy].get_fission())) {
                                //check others gravity pos
                                //System.out.println((x + sx)+","+(y + sy));
                                int ogp = cell[xx + sx][yy + sy].get_gravity_pos();
                                long ogm = cell[xx + sx][yy + sy].getLeftMass();
                                //do merge
                                
                                boolean is_merge = false;
                                double merge_rate = 0D;
                                
                                
                                if (ogm > 0) {
                                    merge_rate = (double)gm/(double)ogm;
                                    switch (ogp) {
                                        case 1:
                                            center_g_x += (double)(sx*3D-1D)*(double)ogm;
                                            center_g_y += (double)(sy*3D-1D)*(double)ogm;
                                            if (cell[xx][yy].Merge(sx*3-1, sy*3-1, ogp) && (merge_rate > merge_mass_max || merge_rate <= merge_mass_min)) {
                                                is_merge = true;
                                            }
                                            break;
                                        case 2:
                                            center_g_x += (double)sx*3D*(double)ogm;
                                            center_g_y += (double)(sy*3D-1D)*(double)ogm;
                                            if (cell[xx][yy].Merge(sx*3, sy*3-1, ogp) && (merge_rate > merge_mass_max || merge_rate <= merge_mass_min)) {
                                                is_merge = true;
                                            }
                                            break;
                                        case 3:
                                            center_g_x += (double)(sx*3D+1D)*(double)ogm;
                                            center_g_y += (double)(sy*3D-1D)*(double)ogm;
                                            if (cell[xx][yy].Merge(sx*3+1, sy*3-1, ogp) && (merge_rate > merge_mass_max || merge_rate <= merge_mass_min)) {
                                                is_merge = true;
                                            }
                                            break;
                                        case 4:
                                            center_g_x += (double)(sx*3D-1D)*(double)ogm;
                                            center_g_y += (double)sy*3D*(double)ogm;
                                            if (cell[xx][yy].Merge(sx*3-1, sy*3, ogp) && (merge_rate > merge_mass_max || merge_rate <= merge_mass_min)) {
                                                is_merge = true;
                                            }
                                            break;
                                        case 5:
                                            
                                            center_g_x += (double)sx*3D*(double)ogm;
                                            center_g_y += (double)sy*3D*(double)ogm;
                                            if (cell[xx][yy].Merge(sx*3, sy*3, ogp) && (merge_rate > merge_mass_max || merge_rate <= merge_mass_min)) {
                                                is_merge = true;
                                            }
                                            break;
                                        case 6:
                                            center_g_x += (double)(sx*3D+1D)*(double)ogm;
                                            center_g_y += (double)sy*3D*(double)ogm;
                                            if (cell[xx][yy].Merge(sx*3+1, sy*3, ogp) && (merge_rate > merge_mass_max || merge_rate <= merge_mass_min)) {
                                                is_merge = true;
                                            }
                                            break;
                                        case 7:
                                            center_g_x += (double)(sx*3D-1D)*(double)ogm;
                                            center_g_y += (double)(sy*3D+1D)*(double)ogm;
                                            if (cell[xx][yy].Merge(sx*3-1, sy*3+1, ogp) && (merge_rate > merge_mass_max || merge_rate <= merge_mass_min)) {
                                                is_merge = true;
                                            }
                                            break;
                                        case 8:
                                            center_g_x += (double)sx*3D*(double)ogm;
                                            center_g_y += (double)(sy*3D+1D)*(double)ogm;
                                            if (cell[xx][yy].Merge(sx*3, sy*3+1, ogp) && (merge_rate > merge_mass_max || merge_rate <= merge_mass_min)) {
                                                is_merge = true;
                                            }
                                            break;
                                        case 9:
                                            center_g_x += (double)(sx*3D+1D)*(double)ogm;
                                            center_g_y += (double)(sy*3D+1D)*(double)ogm;
                                            if (cell[xx][yy].Merge(sx*3+1, sy*3+1, ogp) && (merge_rate > merge_mass_max || merge_rate <= merge_mass_min)) {
                                                is_merge = true;
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                    if (is_merge) {
                                        UniverseGame.merge_time ++;
                                        if (merge_rate > merge_mass_max && merge_rate < merge_mass_max*10D) {
                                            cell[xx][yy].setPreDecayMass(ogm);
                                            cell[xx + sx][yy + sy].setPreMergeMass(ogm);
                                        } else {
                                            cell[xx][yy].setPreMergeMass(ogm);
                                            cell[xx + sx][yy + sy].setPreDecayMass(ogm);
                                        }
                                    }
                                }
                                
                            }
                        }
                    }
                }
            });
        });
        long ttmass = 0L;
        
        
        for (int x = 0; x < this.chunk_size; x++) {
            for (int y = 0; y < this.chunk_size; y++) {
                this.cell[x][y].update();
                ttmass += this.cell[x][y].getMass();
            }
        }
        UniverseGame.total_mass = ttmass;
        //System.out.println("------------------");
        
    }
    
    public int getChunk_Size() {
        return this.chunk_size;
    }
}
