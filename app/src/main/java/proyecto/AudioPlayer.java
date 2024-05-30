package proyecto;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioPlayer {
    private static final int BUFFER_SIZE = 1024;
    private static final float SAMPLE_RATE = 44150.0f;
    private static final int SAMPLE_SIZE_IN_BITS = 8;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = true;

    private final BlockingQueue<double[]> sampleQueue = new LinkedBlockingQueue<>();

    public AudioPlayer() {
        new Thread(this::play).start();
    }

    public void handleIncomingAudioData(double[] samples) {
        try {
            sampleQueue.put(samples);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void play() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(format, BUFFER_SIZE);
            line.start();

            byte[] byteBuffer = new byte[BUFFER_SIZE];
            ByteBuffer byteWrapper = ByteBuffer.wrap(byteBuffer);

            while (true) {
                double[] samples = sampleQueue.take();
                byteWrapper.clear();
                for (double sample : samples) {
                    short intSample = (short) (sample * Short.MAX_VALUE);
                    byteWrapper.putShort(intSample);
                }
                line.write(byteBuffer, 0, byteWrapper.position());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
