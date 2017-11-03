/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package universegame;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author GGzonzonder
 */
public class ATOM {
    Random rnd_pos = new Random();
    Random rnd_decay = new Random();
    
    int gravity_pos;//1~9
    int pre_gravity_pos;//1~9
    long id;//nanotime
    long x, y;
    //Vector atom = new Vector();
    long neutron = 0L;//1837
    long proton = 0L;//1836
    long electron = 0L;//1
    //long mass = neutron*1837L+proton*1836L+electron;
    long mass = 0L;
    long left_mass = 0L;
    //long total_space = 100_000_000_000_000_000L;
    int decay_prob = 1_000_000_000;
    Queue<Long> decay_queue = new ConcurrentLinkedQueue<>();
    Queue<Long> merge_queue = new ConcurrentLinkedQueue<>();
    boolean is_fission = false;
    
    //double density = (double)mass/(double)total_space;
    //density max = 0.00000000000371074
    
    /*Neutron neutron;
    Proton proton;
    Electron electron;*/
    /*would be in these places every frame
    1  2  3 
    4  5  6
    7  8  9 
    
    10
    11 12 13 14 15
    16 17 18 19 20
    21 22 23 24 25
    */
    /*
    if gravity_poses are in relative position like up down cells at 2, 8 and mass diff >= 400, gravity effect cells. smaller mass flow to the bigger one.
    when at pos 5, 1-(0.85x^10+0.15x) to decay in mass proportion;
    */
    
    public ATOM() {
        this.id = System.nanoTime();
    }
    
    public ATOM(long x, long y) {
        this.x = x;
        this.y = y;
        this.id = System.nanoTime();
    }
    
    public void set_fission(boolean tf) {
        this.is_fission = tf;
    }
    
    public void reset() {
        mass = 0L;
        left_mass = 0L;
        decay_queue.clear();
        merge_queue.clear();
        is_fission = false;
    }
    
    public boolean get_fission() {
        return this.is_fission;
    }
    
    public void update() {
        //set state to new state
        while (!this.merge_queue.isEmpty()) {
            this.addMass(this.merge_queue.poll());
        }
        while (!this.decay_queue.isEmpty()) {
            this.subMass(this.decay_queue.poll());
        }
        
        this.left_mass = this.mass;
        this.is_fission = false;
    }
    
    public void setPreDecayMass(long pre_d_mass) {
        //sub mass
        this.left_mass -= pre_d_mass;
        this.decay_queue.add(pre_d_mass);
    }
    
    public void setPreMergeMass(long pre_m_mass) {
        this.merge_queue.add(pre_m_mass);
    }
    
    public void calc_gravity_pos() {
        if (this.mass <= 0L) {
            this.set_gravity_pos(5);
        } else {
            this.set_gravity_pos((int)(System.nanoTime() % 9L)+1);
            //this.set_gravity_pos(rnd_pos.nextInt(9)+1);
        }
        
        //this.set_gravity_pos(rnd_pos.nextInt(9)+1);
    }
    
    public void set_gravity_pos(int pos) {
        this.gravity_pos = pos;
    }
    
    public int get_gravity_pos() {
        return this.gravity_pos;
    }
    
    public void addMass(long m) {
        this.mass += m;
    }
    
    public void subMass(long m) {
        this.mass -= m;
    }
    
    public void setMass(long m) {
        this.mass = m;
        this.left_mass = m;
    }
    
    public long getMass() {
        return this.mass;
    }
    
    public long getLeftMass() {
        return this.left_mass;
    }
    
    public long getDecayProb(long om) {
        double ratio;
        if (om <= 0L) {
            ratio = 1D;
        } else {
            ratio = (double)this.mass / (double)om;
            ratio = ratio / Math.ceil(ratio);
            ratio = (0.95D*Math.pow(ratio, 100)+0.05D*ratio);
        }
        return (long)(ratio* (double)decay_prob);
    }
    
    public boolean Merge(int ox, int oy, int op) {
        if ((ox == -1 || ox == -2) && (oy == -1 || oy == -2) && this.gravity_pos == 1 && op == 9) {
            return true;
        }
        if (ox == 0 && oy == -2 && this.gravity_pos == 2 && op == 8) {
            return true;
        }
        if ((ox == 2 || ox == 1) && (oy == -1 || oy == -2) && this.gravity_pos == 3 && op == 7) {
            return true;
        }
        if (ox == -2 && oy == 0 && this.gravity_pos == 4 && op == 6) {
            return true;
        }
        if (ox == 0 && oy == 0 && this.gravity_pos == 5 && op == 5) {
            return true;
        }
        if (ox == 2 && oy == 0 && this.gravity_pos == 6 && op == 4) {
            return true;
        }
        if ((ox == -1 || ox == -2) && (oy == 2 || oy == 1) && this.gravity_pos == 7 && op == 3) {
            return true;
        }
        if (ox == 0 && oy == 2 && this.gravity_pos == 8 && op == 2) {
            return true;
        }
        if ((ox == 2 || ox == 1) && (oy == 2 || oy == 1) && this.gravity_pos == 9 && op == 1) {
            return true;
        }
        return false;
    }
    
    //not functional
    public boolean Split(int ox, int oy, int op) {
        if ((ox == -1 || ox == -2) && (oy == -1 || oy == -2) && this.gravity_pos == 9 && op == 1) {
            return true;
        }
        if (ox == 0 && oy == -2 && this.gravity_pos == 8 && op == 2) {
            return true;
        }
        if ((ox == 2 || ox == 1) && (oy == -1 || oy == -2) && this.gravity_pos == 7 && op == 3) {
            return true;
        }
        if (ox == -2 && oy == 0 && this.gravity_pos == 6 && op == 4) {
            return true;
        }
        
        if ((ox == -1 || ox == -2) && (oy == 2 || oy == 1) && this.gravity_pos == 3 && op == 7) {
            return true;
        }
        if (ox == 0 && oy == 2 && this.gravity_pos == 2 && op == 8) {
            return true;
        }
        if ((ox == 2 || ox == 1) && (oy == 2 || oy == 1) && this.gravity_pos == 1 && op == 9) {
            return true;
        }
        return false;
    }
    
    
    public long Decay(long ogm) {
        if (this.left_mass > 1L || this.left_mass % 2 == 1) {//even is stable
            long temp_mass = Math.min(Universe_Chunk.mass_arr.get(this.rnd_decay.nextInt(Universe_Chunk.mass_arr.size())), this.left_mass);
            if (temp_mass > this.left_mass) {
                long tlm = this.left_mass - 1L;
                return tlm;
            } else {
                return temp_mass;
            }
        }
        return 0L;
    }
    
    //not functional
    public void setDecayDir() {
        //setDecayDir 
    }
}
