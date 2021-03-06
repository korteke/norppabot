package net.kvak.model;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by korteke on 12/05/2017.
 */
@Component
public class NorppaStatus {

    private static AtomicReference<NorppaStatus> INSTANCE = new AtomicReference<NorppaStatus>();

    private boolean norppaDetected = false;

    public NorppaStatus() {
        final NorppaStatus previous = INSTANCE.getAndSet(this);
        if(previous != null)
            throw new IllegalStateException("Something went wrong");
    }

    public static NorppaStatus getInstance() {
        return INSTANCE.get();
    }

    public boolean isNorppaDetected() {
        return norppaDetected;
    }

    public void setNorppaDetected(boolean norppaDetected) {
        this.norppaDetected = norppaDetected;
    }
}
