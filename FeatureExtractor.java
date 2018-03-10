import be.tarsos.dsp.*;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class FeatureExtractor
{
    private AudioDispatcher audioDispatcher;
    private int audioBufferSize = 2048;
    private int bufferOverlap = 0;
    private int sampleRate = 44100;
    ArrayList a = new ArrayList();
    int count = 0;
    int silences = 0;
    float zeroCrossingRate = 0;

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
                audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
                features = features + silenceDetector(audioDispatcher);
                System.out.println(features);
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
                    //System.out.println("pitch " + pitch + " probability " + probability + " rms " + rms);
                    count++;
                }
            }
        };
        PitchProcessor pp = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, sampleRate, audioBufferSize, handler);
        audioDispatcher.addAudioProcessor(pp);
        audioDispatcher.run();
        output = output + Integer.toString(count) + ",";
        //System.out.println(output);
        count = 0;
        //audioDispatcher.removeAudioProcessor(pp);
        return output;
    }

    public String ZeroCrossingRate(AudioDispatcher audioDispatcher)
    {
        String output = "";
        ZeroCrossingRateProcessor z = new ZeroCrossingRateProcessor()
        {
            @Override
            public void processingFinished() {
                zeroCrossingRate = getZeroCrossingRate();
            }
        };
        audioDispatcher.addAudioProcessor(z);
        audioDispatcher.run();
        output = output + Float.toString(zeroCrossingRate) + ",";
        //audioDispatcher.removeAudioProcessor(z);
        return output;
    }

    public String silenceDetector(AudioDispatcher audioDispatcher)
    {
        String output = "";
        SilenceDetector sd = new SilenceDetector()
        {
            @Override
            public boolean process(AudioEvent audioEvent) {
                boolean isSilence = isSilence(audioEvent.getFloatBuffer());
                if (isSilence)
                {
                    silences++;
                }
                return true;
            }
        };
        audioDispatcher.addAudioProcessor(sd);
        audioDispatcher.run();
        //audioDispatcher.removeAudioProcessor(z);
        output = output + Integer.toString(silences) + ",";
        silences = 0;
        return output;
    }

}
