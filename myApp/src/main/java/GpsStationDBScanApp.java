

import smile.data.Attribute;
import smile.data.AttributeDataset;
import smile.data.NumericAttribute;
import smile.data.parser.DelimitedTextParser;
import smile.demo.clustering.ClusteringDemo;
import smile.demo.clustering.DBScanDemo;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Yang on 2017/5/17.
 */
public class GpsStationDBScanApp {

    DelimitedTextParser csvParser = new DelimitedTextParser();
    AttributeDataset origGpsStationDataset;
    Attribute[] attributes;


    public void setUp() {
        try {
            csvParser.setColumnNames(true);
            csvParser.setDelimiter(",");
            origGpsStationDataset = csvParser.parse("GpsStation", smile.data.parser.IOUtils.getTestDataFile("myApp/src/main/java/resources/gps_station_hexCI_data.csv"));
        } catch (Exception e) {
            System.err.println(e);
        }

        /**
         * LAC,CI,LAT,LNG,NET_TYPE
         * 34343,203042,25.444039,99.832686,ps
         */
        attributes = new Attribute[2];
        attributes[0] = new NumericAttribute("V" + 3);
        attributes[1] = new NumericAttribute("LNG");
    }

    public static void main(String argv[]) {
        ClusteringDemo demo = new DBScanDemo();
        JFrame f = new JFrame("DBScan");
        f.setSize(new Dimension(1000, 1000));
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(demo);
        f.setVisible(true);
    }
}
