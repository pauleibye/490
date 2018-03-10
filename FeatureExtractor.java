import be.tarsos.dsp.*;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
/*
extracts features from files in a folder
 */
public class FeatureExtractor
{
    private AudioDispatcher audioDispatcher;
    private int audioBufferSize = 2048;
    private int bufferOverlap = 0;
    private int sampleRate = 44100;
    ArrayList fileFeatures = new ArrayList();
    int count = 0;
    int silences = 0;
    float zeroCrossingRate = 0;
    float[] fileAmplitudeBins = new float[1024];
    float[] fileFrequencyBins = new float[1024];

    FeatureExtractor() throws IOException, UnsupportedAudioFileException
    {
    }

    /*
    Parses through the files in the path string provided
    Generates and stores comma separated features
    Returns an ArrayList with each index being the features for a file in the folder
     */
    public ArrayList extractFeatures(String path) throws IOException, UnsupportedAudioFileException
    {
        File[] audioFiles = new File(path).listFiles();

        // iterates through all the files in the directory
        for (File file : audioFiles)
        {
            // calls the feature creation methods to generate features, and appends those features to the output string
            if(!file.isDirectory())
            {
                String features = "";
                features = features + file + ",";
                audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
                features = features + YINPitch(audioDispatcher);
                audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
                features = features + ZeroCrossingRate(audioDispatcher);
                audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
                features = features + silenceDetector(audioDispatcher);
                audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
                features = features + spectralCentroid(audioDispatcher);
                fileFeatures.add(features);
            }
        }
        return fileFeatures;
    }

    public ArrayList getFileFeatures()
    {
        return fileFeatures;
    }

    /*
    Generates YIN human voice pitch tracking feature
    http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf
     */
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

    /*
    Generates the zero crossing rate feature
     */
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

    /*
    Calculates how many times silence is detected in the file
    Speech files generally have more silence in them compared to music files
     */
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

    /*
    Generates the spectral centroid of the file
     */
    public String spectralCentroid(AudioDispatcher audioDispatcher)
    {
        String output = "";
        // Does a Fast Fourier transform on a buffer of the file
        // Adds up the buffers, and gets the average weight for each 1024 frequency bins
        AudioProcessor getFFT = new AudioProcessor()
        {
            FFT fft = new FFT(audioBufferSize);
            float[] amplitudes = new float[audioBufferSize/2];
            int count = 0;

            @Override
            public boolean process(AudioEvent audioEvent)
            {
                float[] fb = audioEvent.getFloatBuffer();
                float[] tb = fb;
                fft.forwardTransform(tb);
                fft.modulus(tb, amplitudes);
                for (int i = 0; i < amplitudes.length; i++)
                {
                    fileAmplitudeBins[i] += amplitudes[i];
                    fileFrequencyBins[i] = (float) fft.binToHz(i, sampleRate);
                }
                count++;
                return true;
            }

            @Override
            public void processingFinished()
            {
                for (int i = 0; i < fileAmplitudeBins.length; i++)
                    fileAmplitudeBins[i] /= count;
            }
        };
        audioDispatcher.addAudioProcessor(getFFT);
        audioDispatcher.run();

        // calculates the spectral centroid by using the amplitude of the fft at each bin as a weight
        // multiplies the weight by the frequency of the bin, and sums each bin
        // divide everything by the sum of the weights to get the centroid
        float top = 0;
        float bottom = 0;

        for (int i = 0; i < fileAmplitudeBins.length; i++)
        {
            top += (fileFrequencyBins[i] * fileAmplitudeBins[i]);
        }
        for (float f : fileAmplitudeBins)
        {
            bottom += f;
        }
        float centroid = top / bottom;
        output += centroid;
        return output;
    }
}
