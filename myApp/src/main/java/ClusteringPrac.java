/*******************************************************************************
 * Copyright (c) 2010 Haifeng Li
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

import smile.data.*;
import smile.data.parser.DelimitedTextParser;
import smile.plot.ScatterPlot;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public abstract class ClusteringPrac extends JPanel implements Runnable, ActionListener, AncestorListener {

    private static final String ERROR = "Error";
    private static String[] datasetName = {
        "GPS_Station",
        "GPS_Station_Part1"
    };

    private static String[] datasource = {
        "resources/gps_station_hexCI_data.csv",
        "resources/gps_station_hexCI_part1.csv"
    };

    static double[][][] dataset = null;
    static int datasetIndex = 0;
    static int clusterNumber = 2;

    JPanel optionPane;
    JComponent canvas;
    private JTextField clusterNumberField;
    private JButton startButton;
    private JComboBox<String> datasetBox;
    char pointLegend = '.';

    /**
     * Constructor.
     */
    public ClusteringPrac() {
        if (dataset == null) {
            dataset = new double[datasetName.length][][];
            DelimitedTextParser csvParser = new DelimitedTextParser();
            csvParser.setDelimiter("[\t ]+");
            try {
                Attribute[] attributes = null;
                csvParser.setColumnNames(true);
                csvParser.setDelimiter(",");

                attributes = new Attribute[5];
                attributes[0] = new NominalAttribute("V0");
                attributes[1] = new NominalAttribute("V1");
                attributes[2] = new NumericAttribute("LAT");
                attributes[3] = new NumericAttribute("LNG");
                attributes[4] = new StringAttribute("V4");

                AttributeDataset data = csvParser.parse(datasetName[datasetIndex], attributes, smile.data.parser.IOUtils.getPracDataFile(datasource[datasetIndex]));

                double[][] tmpOrigiDataSet = data.toArray(new double[data.size()][]);
                double[][] tmpLatLng = new double[data.size()][2];
                for(int i = 0; i < data.size(); i++) {
                    for(int j = 0; j < 2; j++) {
                        tmpLatLng[i][j] = tmpOrigiDataSet[i][j+2];
                    }
                }
                dataset[datasetIndex] = tmpLatLng;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to load dataset.", "ERROR", JOptionPane.ERROR_MESSAGE);
                System.err.println(e);
            }
        }

        addAncestorListener(this);

        startButton = new JButton("Start");
        startButton.setActionCommand("startButton");
        startButton.addActionListener(this);

        datasetBox = new JComboBox<>();
        for (int i = 0; i < datasetName.length; i++) {
            datasetBox.addItem(datasetName[i]);
        }
        datasetBox.setSelectedIndex(0);
        datasetBox.setActionCommand("datasetBox");
        datasetBox.addActionListener(this);

        clusterNumberField = new JTextField(Integer.toString(clusterNumber), 5);

        optionPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionPane.setBorder(BorderFactory.createRaisedBevelBorder());
        optionPane.add(startButton);
        optionPane.add(new JLabel("Dataset:"));
        optionPane.add(datasetBox);
        optionPane.add(new JLabel("K:"));
        optionPane.add(clusterNumberField);

        setLayout(new BorderLayout());
        add(optionPane, BorderLayout.NORTH);

        canvas = ScatterPlot.plot(dataset[datasetIndex], '.');
        add(canvas, BorderLayout.CENTER);
    }

    /**
     * Execute the clustering algorithm and return a swing JComponent representing
     * the clusters.
     */
    public abstract JComponent learn();

    @Override
    public void run() {
        startButton.setEnabled(false);
        datasetBox.setEnabled(false);

        try {
        	JComponent plot = learn();
        	if (plot != null) {
        		remove(canvas);
        		canvas = plot;
        		add(canvas, BorderLayout.CENTER);
        	}
        	validate();
        } catch (Exception ex) {
        	System.err.println(ex);
        }

        startButton.setEnabled(true);
        datasetBox.setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("startButton".equals(e.getActionCommand())) {
            try {
                clusterNumber = Integer.parseInt(clusterNumberField.getText().trim());
                if (clusterNumber < 2) {
                    JOptionPane.showMessageDialog(this, "Invalid K: " + clusterNumber, ERROR, JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (clusterNumber > dataset[datasetIndex].length / 2) {
                    JOptionPane.showMessageDialog(this, "Too large K: " + clusterNumber, ERROR, JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid K: " + clusterNumberField.getText(), ERROR, JOptionPane.ERROR_MESSAGE);
                return;
            }

            Thread thread = new Thread(this);
            thread.start();
        } else if ("datasetBox".equals(e.getActionCommand())) {
            datasetIndex = datasetBox.getSelectedIndex();
            
            if (dataset[datasetIndex] == null) {
                DelimitedTextParser csvParser = new DelimitedTextParser();
                csvParser.setDelimiter("[\t ]+");
                try {
                    Attribute[] attributes = null;
                    csvParser.setColumnNames(true);
                    csvParser.setDelimiter(",");

                    attributes = new Attribute[5];
                    attributes[0] = new NominalAttribute("V0");
                    attributes[1] = new NominalAttribute("V1");
                    attributes[2] = new NumericAttribute("LAT");
                    attributes[3] = new NumericAttribute("LNG");
                    attributes[4] = new StringAttribute("V4");

                    AttributeDataset data = csvParser.parse(datasetName[datasetIndex], attributes, smile.data.parser.IOUtils.getPracDataFile(datasource[datasetIndex]));

                    double[][] tmpOrigiDataSet = data.toArray(new double[data.size()][]);
                    double[][] tmpLatLng = new double[data.size()][2];
                    for(int i = 0; i < data.size(); i++) {
                        for(int j = 0; j < 2; j++) {
                            tmpLatLng[i][j] = tmpOrigiDataSet[i][j+2];
                        }
                    }
                    dataset[datasetIndex] = tmpLatLng;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Failed to load dataset.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    System.err.println(ex);
                }
            }

            remove(canvas);
            if (dataset[datasetIndex].length < 500) {
                pointLegend = 'o';
            } else {
                pointLegend = '.';
            }
            canvas = ScatterPlot.plot(dataset[datasetIndex], pointLegend);
            add(canvas, BorderLayout.CENTER);
            validate();
        }
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
        clusterNumberField.setText(Integer.toString(clusterNumber));
        
        if (datasetBox.getSelectedIndex() != datasetIndex) {
            datasetBox.setSelectedIndex(datasetIndex);
            remove(canvas);
            if (dataset[datasetIndex].length < 500) {
                pointLegend = 'o';
            } else {
                pointLegend = '.';
            }
            canvas = ScatterPlot.plot(dataset[datasetIndex], pointLegend);
            add(canvas, BorderLayout.CENTER);
            validate();
        }
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
    }
}
