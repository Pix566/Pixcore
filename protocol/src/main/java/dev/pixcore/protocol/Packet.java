package dev.pixcore.protocol;

import java.io.DataOutput;
import java.io.IOException;

/**
 * A wire packet. Implementations write their body (excluding the packet id)
 * to the given output.
 */
public interface Packet {
    int getId();

    void write(DataOutput out) throws IOException;
}
