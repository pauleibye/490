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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
extracts features from files in a folder
 */
public class FeatureExtractor
{
    private AudioDispatcher audioDispatcher;
    private int audioBufferSize = 2048;
    private int bufferOverlap = 0;
    private int sampleRate = 44100;
    ArrayList<String> fileFeatures = new ArrayList<String>();
    int YINcount = 0;
    float YINprobability = 0;
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
    public ArrayList<String> extractFeatures(String path) throws IOException, UnsupportedAudioFileException
    {
        File[] audioFiles = new File(path).listFiles();

        // iterates through all the files in the directory
        for (File file : audioFiles)
        {
            // calls the feature creation methods to generate features, and appends those features to the output string
            if(!file.isDirectory())
            {
                String features = "";
                //features = features + file + ",";
                audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
                features = features + YINPitch(audioDispatcher);
                audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
                features = features + ZeroCrossingRate(audioDispatcher);
                audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
                features = features + silenceDetector(audioDispatcher);
                audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
                features = features + spectralCentroid(audioDispatcher) + ",";
                String fileName = getFileName(file);
                features = features + fileName;
                fileFeatures.add(features);
            }
        }
        return fileFeatures;
    }
    
    public Double[] extractFeaturesForTest(String path) throws IOException, UnsupportedAudioFileException{
    	File file = new File(path);
    		if(!file.isDirectory())
    		{
    			Double[] features = new Double[4];
    			//features = features + file + ",";
    			audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
    				String str = YINPitch(audioDispatcher);
    				
    				Pattern pattern = Pattern.compile(", *");
    				Matcher matcher = pattern.matcher(str);
    				str = str.replaceAll("[^\\d.]", "");
    				
    				String str1 = "";
    				String str2 = "";
    				if (matcher.find()) {
    				    str1 = str.substring(0, matcher.start());
    				    str2 = str.substring(matcher.end());
    				}
    				/*

    			features[0] = Double.parseDouble(str1);
    			features[1] = Double.parseDouble(str2);
    		*/
    			features[0] = Double.parseDouble(str1) * Double.parseDouble(str2);
    			
    			audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
    				str = ZeroCrossingRate(audioDispatcher);
    				str = str.replaceAll("[^\\d.]", "");
    			features[1] = Double.parseDouble(str);
    			
    			audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
    				str = silenceDetector(audioDispatcher);
    				str = str.replaceAll("[^\\d.]", "");
    			features[2] = Double.parseDouble(str);
    			
    			audioDispatcher = AudioDispatcherFactory.fromFile(file, audioBufferSize, bufferOverlap);
    				str = spectralCentroid(audioDispatcher);
    				str = str.replaceAll("[^\\d.]", "");
    			features[3] = Double.parseDouble(str);
    			return features;
    		}
    	return null;
    };

    //gets rid of all digits
    private String getFileName(File file) {
		String temp = file.getName();
		if(temp.substring(0, 2).equals("mu")){
			return "Music";
		}
		else{
			return "Speech";
		}
	}

	public ArrayList<String> getFileFeatures()
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
                //double timeStamp = audioEvent.getTimeStamp();
                //float pitch = pitchDetectionResult.getPitch();
                //double rms = audioEvent.getRMS() * 100;
                if(pitchDetectionResult.isPitched())
                {
                    //System.out.println("pitch " + pitch + " probability " + probability + " rms " + rms);
                    YINprobability += pitchDetectionResult.getProbability();
                    YINcount++;
                }
            }
        };
        PitchProcessor pp = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, sampleRate, audioBufferSize, handler);
        audioDispatcher.addAudioProcessor(pp);
        audioDispatcher.run();
        output = output + Float.toString(YINprobability);
        //System.out.println(output);
        YINcount = 0;
        YINprobability = 0;
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