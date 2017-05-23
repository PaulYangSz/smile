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

import smile.clustering.DBScan;
import smile.demo.clustering.ClusteringDemo;
import smile.math.distance.EuclideanDistance;
import smile.plot.Palette;
import smile.plot.PlotCanvas;
import smile.plot.ScatterPlot;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Yang on 2017/5/17.
 * @author Yang
 */
@SuppressWarnings("serial")
public class GpsStationDBScanApp extends ClusteringPrac {
    JTextField minPtsField;
    JTextField rangeField;
    JTextField fenceField;
    int minPts = 10;
    double range = 0.02;
    double fence = 0.0002;

    public GpsStationDBScanApp() {
        // Remove K TextFile
        optionPane.remove(optionPane.getComponentCount() - 1);
        optionPane.remove(optionPane.getComponentCount() - 1);

        minPtsField = new JTextField(Integer.toString(minPts), 5);
        optionPane.add(new JLabel("MinPts:"));
        optionPane.add(minPtsField);

        rangeField = new JTextField(Double.toString(range), 5);
        optionPane.add(new JLabel("Range(0.01 = 1KM):"));
        optionPane.add(rangeField);

        fenceField = new JTextField(Double.toString(fence), 7);
        optionPane.add(new JLabel("Fence(0.00001 = 1M):"));
        optionPane.add(fenceField);
    }

    @Override
    public JComponent learn() {
        try {
            minPts = Integer.parseInt(minPtsField.getText().trim());
            if (minPts < 1) {
                JOptionPane.showMessageDialog(this, "Invalid MinPts: " + minPts, "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid MinPts: " + minPtsField.getText(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            range = Double.parseDouble(rangeField.getText().trim());
            if (range <= 0) {
                JOptionPane.showMessageDialog(this, "Invalid Range: " + range, "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid range: " + rangeField.getText(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        long clock = System.currentTimeMillis();
        DBScan<double[]> dbscan = new DBScan<>(dataset[datasetIndex], new CosXEuclideanDistance(), minPts, range);
        System.out.format("DBSCAN clusterings %d samples in %dms\n", dataset[datasetIndex].length, System.currentTimeMillis()-clock);
        System.out.format("Result: %d cluster\n", dbscan.getNumClusters());
        for(int k = 0; k < dbscan.getNumClusters(); k++) {
            System.out.println("["+ k +"]'s"+ " size " +dbscan.getClusterSize()[k]);
        }
        System.out.println("[OUTLIER]'s"+ " size " +dbscan.getClusterSize()[dbscan.getNumClusters()]);
        System.out.println();

        /* Calculate the GPS of station */
        int maxK = -1, maxSize = 0;
        int[] labelResult = dbscan.getClusterLabel();
        for(int k = 0; k < dbscan.getNumClusters(); k++) {
            if(dbscan.getClusterSize()[k] > maxSize) {
                maxK = k;
                maxSize = dbscan.getClusterSize()[k];
            }
        }
        double sumLat = 0, sumLng = 0;
        double sumTotalLat = 0, sumTotalLng = 0;
        for(int i = 0; i < dataset[datasetIndex].length; i++) {
            if(labelResult[i] == maxK) {
                sumLat += dataset[datasetIndex][i][0];
                sumLng += dataset[datasetIndex][i][1];
            }
            sumTotalLat += dataset[datasetIndex][i][0];
            sumTotalLng += dataset[datasetIndex][i][1];
        }
        double stationLat = sumLat / maxSize;
        double stationLng = sumLng / maxSize;
        System.out.format("===GPS of Station: (%f, %f)\n", stationLat, stationLng);
        sumTotalLat = sumTotalLat / dataset[datasetIndex].length;
        sumTotalLng = sumTotalLng / dataset[datasetIndex].length;
        System.out.format("~~~Total average: (%f, %f)\n\n", sumTotalLat, sumTotalLng);

        JPanel pane = new JPanel(new GridLayout(1, 2));
        PlotCanvas plot = ScatterPlot.plot(dataset[datasetIndex], pointLegend);
        for (int k = 0; k < dbscan.getNumClusters(); k++) {
            double[][] cluster = new double[dbscan.getClusterSize()[k]][];
            for (int i = 0, j = 0; i < dataset[datasetIndex].length; i++) {
                if (dbscan.getClusterLabel()[i] == k) {
                    cluster[j++] = dataset[datasetIndex][i];
                }
            }

            plot.points(cluster, pointLegend, Palette.COLORS[k % Palette.COLORS.length]);
        }
        pane.add(plot);

        return pane;
    }

    @Override
    public String toString() {
        return "DBScan";
    }

    public static void main(String argv[]) {
        ClusteringPrac demo = new GpsStationDBScanApp();
        JFrame f = new JFrame("DBScan");
        f.setSize(new Dimension(1000, 1000));
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(demo);
        f.setVisible(true);
    }
}
