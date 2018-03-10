import be.tarsos.dsp.*;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FeatureExtractor
{
    private AudioDispatcher audioDispatcher;
    private int audioBufferSize = 2048;
    private int bufferOverlap = 0;
    private int sampleRate = 44100;
    ArrayList a = new ArrayList();
    int count = 0;

    FeatureExtractor() throws IOException, UnsupportedAudioFileException
    {
        File[] musicFiles = new File("resources/music").listFiles();
        File[] speechFiles = new File("resources/speech").listFiles();
        File[] audioFiles = new File("resources").listFiles();

        for (File file : audioFiles)
        {
            if(!file.isDirectory())
            {
                String features = "";
                System.out.println(file);
                audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
                features = features + YINPitch(audioDispatcher);
                audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
                features = features + ZeroCrossingRate(audioDispatcher);
                System.out.println();
            }
        }
    }

    public String YINPitch(AudioDispatcher audioDispatcher) throws IOException, UnsupportedAudioFileException
    {
        String output = "";
        PitchDetectionHandler handler = new PitchDetectionHandler()
        {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent)
            {
                //System.out.println(audioEvent.getTimeStamp() + " " + pitchDetectionResult.getPitch());
                double timeStamp = audioEvent.getTimeStamp();
                float pitch = pitchDetectionResult.getPitch();
                float probability = pitchDetectionResult.getProbability();
                double rms = audioEvent.getRMS() * 100;
                if(pitchDetectionResult.isPitched())
                {
                    System.out.println("pitch " + pitch + " probability " + probability + " rms " + rms);
                    count++;
                }
            }
        };
        PitchProcessor pp = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, sampleRate, audioBufferSize, handler);
        audioDispatcher.addAudioProcessor(pp);
        audioDispatcher.run();
        output = output + Integer.toString(count);
        System.out.println(output);
        count = 0;
        audioDispatcher.removeAudioProcessor(pp);
        return output;
    }

    public String ZeroCrossingRate(AudioDispatcher audioDispatcher)
    {
        String output = "";
        ZeroCrossingRateProcessor z = new ZeroCrossingRateProcessor()
        {
            @Override
            public void processingFinished() {
                System.out.println(getZeroCrossingRate());
            }
        };
        audioDispatcher.addAudioProcessor(z);
        audioDispatcher.run();
        audioDispatcher.removeAudioProcessor(z);
        return output;
    }

}
