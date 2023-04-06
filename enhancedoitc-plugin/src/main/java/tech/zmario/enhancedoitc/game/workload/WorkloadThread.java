package tech.zmario.enhancedoitc.game.workload;

import org.bukkit.scheduler.BukkitRunnable;
import tech.zmario.enhancedoitc.game.EnhancedOITC;

import java.util.ArrayDeque;
import java.util.Deque;

public class WorkloadThread extends BukkitRunnable {

    private static final int MAX_MS_PER_TICK = 3;
    private static final int MAX_NANOS_PER_TICK = (int) (MAX_MS_PER_TICK * 1E6);
    private final Deque<Workload> workloads = new ArrayDeque<>();

    public void addWorkload(Workload workload) {
        workloads.add(workload);
    }
    
    @Override
    public void run() {
        long stopTime = System.nanoTime() + MAX_NANOS_PER_TICK;
        Workload next;
        
        while (System.nanoTime() <= stopTime && (next = workloads.poll()) != null) {
            next.compute();
        }
    }
    
    public void register(EnhancedOITC plugin) {
        runTaskTimer(plugin, 0L, 1L);
    }
}