package com.jayfella.terrain.builder;

import java.util.Comparator;

/**
 * Created by James on 29/04/2017.
 */
public class PriorityComparator implements Comparator<Runnable> {

    @Override
    public int compare(Runnable o1, Runnable o2) {

        if (o1 == null && o2 == null) {
            return 0;
        }
        else if (o1 == null) {
            return -1;
        }
        else if (o2 == null) {
            return 1;
        }

        int p1 = ((PriorityFuture<?>) o1).getPriority();
        int p2 = ((PriorityFuture<?>) o2).getPriority();

        return p1 > p2 ? 1 : (p1 == p2 ? 0 : -1);
    }

}
