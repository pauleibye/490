import java.awt.EventQueue;

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
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JLabel;


public class MainFrame
{

    private JFrame frame;
    private DefaultListModel listModel = new DefaultListModel();
    private int numFiles = 13;
    private JLabel lblFeatures;
    private JLabel featuresDisplay;
    public static FeatureExtractor featureExtractor;

    /**
     * Launch the application.
     */
    public static void main(String[] args) throws IOException, UnsupportedAudioFileException
    {
        featureExtractor = new FeatureExtractor();
        // parameter is string path to folder with audio files
        ArrayList trainingFilesFeatures = featureExtractor.extractFeatures("resources/training");
        ArrayList testingFilesFeatures = featureExtractor.extractFeatures("resources/testing");
        System.out.println(trainingFilesFeatures);
        System.out.println(testingFilesFeatures);

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
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
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
        GridBagConstraints gbc_lblFeatures = new GridBagConstraints();
        gbc_lblFeatures.insets = new Insets(0, 0, 5, 5);
        gbc_lblFeatures.gridx = 4;
        gbc_lblFeatures.gridy = 1;
        frame.getContentPane().add(lblFeatures, gbc_lblFeatures);

        final JList<JCheckBox> list = new JList(listModel);
        GridBagConstraints gbc_list = new GridBagConstraints();
        gbc_list.insets = new Insets(0, 0, 0, 5);
        gbc_list.fill = GridBagConstraints.BOTH;
        gbc_list.gridx = 2;
        gbc_list.gridy = 3;
        frame.getContentPane().add(list, gbc_list);

        featuresDisplay = new JLabel("No Image Proccessed");
        GridBagConstraints gbc_featuresDisplay = new GridBagConstraints();
        gbc_featuresDisplay.insets = new Insets(0, 0, 0, 5);
        gbc_featuresDisplay.gridx = 4;
        gbc_featuresDisplay.gridy = 3;
        frame.getContentPane().add(featuresDisplay, gbc_featuresDisplay);

        /*
         * Add code to do on button press
         */
        btnParse.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                featuresDisplay.setText("" + list.getSelectedIndex());
            }
        });

    }

}
