package org.meridor.perspective.worker.fetcher.impl;

//These coefficients are experimental and subject of further research 
public final class SchedulerUtils {

    public static int delayToLimit(int delay) {
        return delay * 60;
    }

    public static int getNowDelay(int fullSyncDelay) {
        return Math.floorDiv(fullSyncDelay, 40);
    }
    
    public static int getMomentsAgoLimit(int longTimeAgoLimit) {
        return Math.floorDiv(longTimeAgoLimit, 10);
    }
    
    public static int getMomentsAgoDelay(int fullSyncDelay) {
        return Math.floorDiv(fullSyncDelay, 20);
    }
    
    public static int getSomeTimeAgoLimit(int longTimeAgoLimit) {
        return Math.floorDiv(longTimeAgoLimit, 2);
    }

    public static int getSomeTimeAgoDelay(int fullSyncDelay) {
        return Math.floorDiv(fullSyncDelay, 4);
    }
    
    private SchedulerUtils() {
        
    }

}
