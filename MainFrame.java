import java.awt.EventQueue;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JButton;

import java.awt.GridBagConstraints;

import javax.swing.JList;

import java.awt.Insets;

import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JLabel;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.matrix.*;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.LWL;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.meta.RegressionByDiscretization;
import weka.classifiers.rules.OneR;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.HoeffdingTree;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.M5P;

import java.awt.Font;

/*
 * MainFrame class:
 * -responsible for GUI drawing and Machine Learning
 * Use logistic model tree as it generated the best model
 */
public class MainFrame
{

    private JFrame frame;
    private DefaultListModel listModel = new DefaultListModel();
    private int numFiles = 12;
    private Instances trainDataSet;
    private int selected;
    //List of all attempted models and their results
    //IBk - error
    //NaiveBayes - 7/12
    //J48 - 8/12
    //LMT 9/12
    //M5P - error
    //HoeffdingTree - 7/12
    //SMO - 6/12
    //Logistic - 7/12
    //OneR - 6/12
    //AdaBoostM1 - 8/12
    //LogitBoost - 8/12
    //DecisionStump - 6/12
    //LinearRegression - error
    //LWL - 8/12 + errors
    //RegressionByDiscretization - error
    private LMT nb; //Logistic Model Tree
    private JLabel lblFeatures;
    private JLabel featuresDisplay;
    public static FeatureExtractor featureExtractor;
    private JLabel resultLabel;
    private JButton btnPlayAudio;
    private JLabel labelCorrect;

    /**
     * Launch the application.
     */
    public static void main(String[] args) throws IOException, UnsupportedAudioFileException
    {
    	
        featureExtractor = new FeatureExtractor();
        // parameter is string path to folder with audio files
        ArrayList<String> trainingFilesFeatures = featureExtractor.extractFeatures("resources/training");
        //ArrayList testingFilesFeatures = featureExtractor.extractFeatures("resources/testing");
        //System.out.println(trainingFilesFeatures);
        //System.out.println(testingFilesFeatures);

        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    MainFrame window = new MainFrame();
                    
                    window.frame.setVisible(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MainFrame()
    {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        frame = new JFrame();
        frame.getContentPane().setFont(new Font("Tahoma", Font.PLAIN, 18));
        frame.setBounds(100, 100, 615, 377);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
        frame.getContentPane().setLayout(gridBagLayout);

        JButton btnParse = new JButton("Parse");


        GridBagConstraints gbc_btnParse = new GridBagConstraints();
        gbc_btnParse.insets = new Insets(0, 0, 5, 5);
        gbc_btnParse.gridx = 2;
        gbc_btnParse.gridy = 1;
        frame.getContentPane().add(btnParse, gbc_btnParse);

        for (int i = 0; i < numFiles; i++)
        {
            listModel.addElement("Audio File: " + i);
        }

        lblFeatures = new JLabel("Features");
        lblFeatures.setFont(new Font("Tahoma", Font.PLAIN, 36));
        GridBagConstraints gbc_lblFeatures = new GridBagConstraints();
        gbc_lblFeatures.insets = new Insets(0, 0, 5, 5);
        gbc_lblFeatures.gridx = 4;
        gbc_lblFeatures.gridy = 1;
        frame.getContentPane().add(lblFeatures, gbc_lblFeatures);
        
        btnPlayAudio = new JButton("Play Audio");
        GridBagConstraints gbc_btnPlayAudio = new GridBagConstraints();
        gbc_btnPlayAudio.insets = new Insets(0, 0, 5, 5);
        gbc_btnPlayAudio.gridx = 5;
        gbc_btnPlayAudio.gridy = 1;
        frame.getContentPane().add(btnPlayAudio, gbc_btnPlayAudio);

        final JList<JCheckBox> list = new JList(listModel);
        GridBagConstraints gbc_list = new GridBagConstraints();
        gbc_list.gridheight = 2;
        gbc_list.insets = new Insets(0, 0, 0, 5);
        gbc_list.fill = GridBagConstraints.BOTH;
        gbc_list.gridx = 2;
        gbc_list.gridy = 2;
        frame.getContentPane().add(list, gbc_list);
        
        resultLabel = new JLabel("New label");
        resultLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
        GridBagConstraints gbc_resultLabel = new GridBagConstraints();
        gbc_resultLabel.insets = new Insets(0, 0, 5, 5);
        gbc_resultLabel.gridx = 4;
        gbc_resultLabel.gridy = 2;
        frame.getContentPane().add(resultLabel, gbc_resultLabel);

        featuresDisplay = new JLabel("No Image Proccessed");
        featuresDisplay.setFont(new Font("Tahoma", Font.PLAIN, 18));
        GridBagConstraints gbc_featuresDisplay = new GridBagConstraints();
        gbc_featuresDisplay.insets = new Insets(0, 0, 0, 5);
        gbc_featuresDisplay.gridx = 4;
        gbc_featuresDisplay.gridy = 3;
        frame.getContentPane().add(featuresDisplay, gbc_featuresDisplay);
        
        this.resultLabel.setText("No Image Processed");
        
        labelCorrect = new JLabel("");
        GridBagConstraints gbc_labelCorrect = new GridBagConstraints();
        gbc_labelCorrect.insets = new Insets(0, 0, 0, 5);
        gbc_labelCorrect.gridx = 5;
        gbc_labelCorrect.gridy = 3;
        frame.getContentPane().add(labelCorrect, gbc_labelCorrect);
        
        createCSVandArff(featureExtractor);
        buildModel();
        
        /*
         * Parse button handler
         * Takes selected file and runs it against the machine learning model
         * updates label to display if algorithm was correct or not
         */
        btnParse.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	try {
            		if(list.isSelectionEmpty()){
            			return;
            		}
                    selected = list.getSelectedIndex();
                    String path = convertToImagePath();
                    
					FeatureExtractor fe = new FeatureExtractor();
	            	Double[] features =  fe.extractFeaturesForTest("resources/testing/" + path);

	                Instance inst = new DenseInstance(features.length);
	                inst.setDataset(trainDataSet);
	                
	                for(int i = 0; i < features.length; i++){
	                	inst.setValue(i, features[i]);
	                }
	                double result = nb.classifyInstance(inst);
	                String predStr = trainDataSet.classAttribute().value((int)result);
	                String actual = convertToImagePath();
	                resultLabel.setText("Selected File: " + actual);
	                featuresDisplay.setText("Algorithm Prediction: " + predStr);
	                if(predStr.substring(0, 1).toLowerCase().equals(actual.substring(0, 1).toLowerCase())){
	                	labelCorrect.setText("Yes, Prediction is correct");
	                }
	                else{
	                	labelCorrect.setText("No, Prediction is wrong");
	                }
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (UnsupportedAudioFileException e1) {
					e1.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}


            }
        });
        
        /*
         * Plays selected audio file on click, NOTE, file has to parsed first
         */
        btnPlayAudio.addActionListener(new ActionListener(){
        	 public void actionPerformed(ActionEvent e)
             {
        		 File file = new File("resources/testing/" + convertToImagePath());
        		 AudioInputStream audioIn;
				try {
					audioIn = AudioSystem.getAudioInputStream(file);
	        		 Clip clip = AudioSystem.getClip();
	        		 clip.open(audioIn);
	        		 clip.start();
				} catch (UnsupportedAudioFileException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (LineUnavailableException e1) {
					e1.printStackTrace();
				}

             }
        });

    }
    
    /*
     * converts from value to appropriate filename
     * @return: file name as a string
     */
    public String convertToImagePath(){
    	if(selected < 0){
    		return null;
    	}
    	String temp = "";
    	if(selected < 6){
    		temp += "mu";
    		temp += (selected + 15);
    	}
    	else{
    		temp += "sp";
    		temp += (selected + 9);
    	}
    	temp += ".wav";
    	
    	return temp;
    };
    
    //convert our weird arraylist to a sick ARFF file and save it
    public void createCSVandArff(FeatureExtractor FE){
    	  try {
    		     File file = new File("csv.txt");
    		     	if(!file.exists()){
    		     		file.createNewFile();
    		     		PrintWriter writer = new PrintWriter(file);
    		     		for(String s : FE.getFileFeatures()){
    		     			writer.println(s);
    		     		}
    		     		writer.close();
    		     	}
    		     File arff = new File("data.arff");
    		     	if(!arff.exists()){
    		     		CSVLoader loader = new CSVLoader();
    		     		loader.setSource(file);
    		     		Instances data = loader.getDataSet();
    		     		
    		     		ArffSaver saver = new ArffSaver();
    		     		saver.setInstances(data);
    		     		saver.setFile(arff);
    		     		saver.writeBatch();
    		     	}
    		     
    	    	} catch (IOException e) {
    	    		System.out.println("Exception Occurred:");
    		        e.printStackTrace();
    		  }
    };
    
    /*
     * Build a LMT model from our .arff file
     */
    public void buildModel(){
    	try {
			DataSource source = new DataSource("data.arff");
			trainDataSet = source.getDataSet();						
			trainDataSet.setClassIndex(trainDataSet.numAttributes()-1);
			
			nb = new LMT();
			nb.buildClassifier(trainDataSet);

		} catch (Exception e) {
			e.printStackTrace();
		}
    };

}
