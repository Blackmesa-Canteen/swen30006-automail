package swen30006.automail;

import com.unimelb.swen30006.wifimodem.WifiModem;
import swen30006.simulation.Building;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * @program Automail
 * @description since this calculator will be called by MailPool & Robot, so make this as a Util class
 * @create 2021-04-06 12:21
 */
public class ChargeCalculator {

    // setter is called in Simulation class
    private static WifiModem wifiModem;

    // these values are configurable according to the spec
    private static double activityUnitPrice = 0.224;
    private static double markupPercentage = 0.059;

    private static final double movementActivityUnits = 5;
    private static final double lookupActivityUnits  = 0.1;

    // something may need in the future:
    private static double weightCharge;
    private static double penalty;

    // calculate Charge, it can be called by MailPool (for priority sorting) and Robot(when arrive to the client)
    public static double CalcCharge(MailItem mailItem){

        /* we shouldn't add new properties to the automail.properties file */
//        try {
//            readProperties();
//        } catch (ParseException | IOException e) {
//            e.printStackTrace();
//        }

        int destinationFloor = mailItem.getDestFloor();
        int mailRoomFloor = Building.MAILROOM_LOCATION;
        double floorServiceFee = -1D;

        do {
            floorServiceFee = wifiModem.forwardCallToAPI_LookupPrice(destinationFloor);

            /* check whether query is successful */
        } while (floorServiceFee < 0);


        // Mailroom -> DestinationFloor -> Mailroom. (see Discussion on Canvas)
        double movementTotalUnits = ((destinationFloor - mailRoomFloor) * 2) * movementActivityUnits;

        // user will be charged for only one look up fee per mail (see spec)
        /* data look up fee need to ask teacher! */
        double lookUpTotalUnits = 1 * lookupActivityUnits;

        double activityCost = (movementTotalUnits + lookUpTotalUnits) * activityUnitPrice;

        double cost = floorServiceFee + activityCost;

        double charge = cost * (1 + markupPercentage);

        mailItem.charge = charge;
        mailItem.cost = cost;
        mailItem.fee = floorServiceFee;
        /* data look up fee need to ask teacher! */
        mailItem.activity = movementTotalUnits + lookUpTotalUnits;

        // this is the charge
        return charge;
    }

    // read configurable properties
    // deprecated!
    @Deprecated
    private static void readProperties() throws IOException, ParseException {
        Properties automailProperties = new Properties();

        // Read properties files
        FileReader inStream = null;
        try {
            inStream = new FileReader("automail.properties");
            automailProperties.load(inStream);
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }

        activityUnitPrice = Double.parseDouble(automailProperties.getProperty("Activity_Unit_Price"));

        // parse percentage String to Double
        NumberFormat nf=NumberFormat.getPercentInstance();
        markupPercentage = (double) nf.parse(automailProperties.getProperty("Markup_Percentage"));
    }

    public static void setWifiModem(WifiModem wifiModem) {
        ChargeCalculator.wifiModem = wifiModem;
    }

    public static void setActivityUnitPrice(double activityUnitPrice) {
        ChargeCalculator.activityUnitPrice = activityUnitPrice;
    }

    public static void setMarkupPercentage(double markupPercentage) {
        ChargeCalculator.markupPercentage = markupPercentage;
    }
}