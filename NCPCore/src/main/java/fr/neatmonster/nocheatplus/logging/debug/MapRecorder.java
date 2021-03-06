package fr.neatmonster.nocheatplus.logging.debug;

import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.FakeBlockCache;
import fr.neatmonster.nocheatplus.utilities.RayTracing;

/**
 * Record blocks along a ray (and potentially other). Changes to blocks are not
 * recorded.
 * 
 * @author asofold
 *
 */
public class MapRecorder {

    /**
     * Record all blocks including shapes with margin, using ray-tracing.<br>
     * No cleanup is done here. Might want to set maxSteps (the current
     * implementation should scale reliably with distances).
     * 
     * @author asofold
     *
     */
    protected static class TraceRecorder extends RayTracing {

        private final BlockCache worldAccess;
        private final FakeBlockCache recorder;
        private final int margin; // TODO: separated y margin(s) ?

        public TraceRecorder(BlockCache worldAccess, FakeBlockCache recorder, int margin) {
            this.worldAccess = worldAccess;
            this.recorder = recorder;
            this.margin = margin;
            // TODO: maxSteps ? [In some cases it might be better with limiting recording by the steps done.]
        }

        @Override
        protected boolean step(int blockX, int blockY, int blockZ, double oX, double oY, double oZ, double dT, boolean isPrimary) {
            if (margin > 0) {
                recordCuboid(worldAccess, recorder, blockX - margin, blockY - margin, blockZ - margin, blockX + margin, blockY + margin, blockZ + margin);
            } else {
                recorder.set(blockX, blockY, blockZ, worldAccess.getTypeId(blockX, blockY, blockZ), worldAccess.getData(blockX, blockY, blockZ), worldAccess.getBounds(blockX, blockY, blockZ));
            }
            return true;
        }

    }

    /**
     * Record the entire cuboid. Already present entries are not updated.
     * @param worldAccess
     * @param recorder
     * @param xMin
     * @param yMin
     * @param zMin
     * @param xMax
     * @param yMax
     * @param zMax
     */
    public static void recordCuboid(final BlockCache worldAccess, final FakeBlockCache recorder, 
            final int xMin, final int yMin, final int zMin, final int xMax, final int yMax, final int zMax) {
        for (int x = xMin; x <= xMax; x ++) {
            for (int z = zMin; z <= zMax; z ++) {
                for (int y = yMin; y <= yMax; y ++) {
                    if (!recorder.hasIdEntry(x, y, z)) {
                        // Use get... methods for efficiency in certain use cases..
                        recorder.set(x, y, z, worldAccess.getTypeId(x, y, z), worldAccess.getData(x, y, z), worldAccess.getBounds(x, y, z));
                    }
                }
            }
        }
    }

    /**
     * Record blocks along a ray.
     * @param worldAccess
     * @param recorder
     * @param x0
     * @param y0
     * @param z0
     * @param x1
     * @param y1
     * @param z1
     * @param margin If greater than 0, a cuboid is recorded (2 * margin + 1 length per side).
     * @param maxSteps
     */
    public static void recordTrace(final BlockCache worldAccess, final FakeBlockCache recorder,
            final double x0, final double y0, final double z0, final double x1, final double y1, final double z1,
            final int margin, final int maxSteps){
        final TraceRecorder traceRecorder = new TraceRecorder(worldAccess, recorder, margin);
        traceRecorder.setMaxSteps(maxSteps);
        traceRecorder.set(x0, y0, z0, x1, y1, z1);
        traceRecorder.loop();
    }

}
