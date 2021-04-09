package swen30006.simulation;

import swen30006.automail.MailItem;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * @program Automail
 * @description since this calculator will be called by MailPool & Robot, so make this as a Util class and couple with
 * Simulation class
 * @create 2021-04-06 12:21
 */
public class ChargeCalculator {

    private static ModemAdapter modemAdapter;
    private static double CHARGE_THRESHOLD;

    // these values are configurable according to the spec
    // user can configure them with setters in Simulation class
    private static double activityUnitPrice;
    private static double markupPercentage;

    private static double movementActivityUnits;
    private static double lookupActivityUnits;

    // something may need in the future:
    private static double weightCharge;
    private static double penalty;

    // calculate Charge, it can be reused by MailPool (for priority sorting) and Robot(when arrive to the client)
    public static double CalcCharge(MailItem mailItem){

        int destinationFloor = mailItem.getDestFloor();
        int mailRoomFloor = Building.MAILROOM_LOCATION;
        double floorServiceFee = -1D;

        long numLookupsForThisTime = 0;

        ModemQueryResult<Double> queryResult =  modemAdapter.lookupFloorServiceFee(destinationFloor);
        floorServiceFee = queryResult.getQueryResult();
        numLookupsForThisTime = queryResult.getNumLookups();
        queryResult = null;

        // Mailroom -> DestinationFloor -> Mailroom. (see Discussion on Canvas)
        double movementTotalUnits = ((destinationFloor - mailRoomFloor) * 2) * movementActivityUnits;

        // user will be charged for only one look up fee per mail (see spec)
        double chargedLookUpTotalUnits = 1 * lookupActivityUnits;

        double realLookUpTotalUnitsForThisTime = numLookupsForThisTime * lookupActivityUnits;

        double billableActivityCost = (movementTotalUnits + chargedLookUpTotalUnits) * activityUnitPrice;

        double cost = floorServiceFee + billableActivityCost;

        double charge = cost * (1 + markupPercentage);

        // update infomation in the mailItem object with the latest data
        mailItem.updateMailBillInfo(floorServiceFee, movementTotalUnits, realLookUpTotalUnitsForThisTime,
                cost, charge, billableActivityCost);

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

    public static void setActivityUnitPrice(double activityUnitPrice) {
        ChargeCalculator.activityUnitPrice = activityUnitPrice;
    }

    public static void setMarkupPercentage(double markupPercentage) {
        ChargeCalculator.markupPercentage = markupPercentage;
    }

    public static void setModemAdapter(ModemAdapter modemAdapter) {
        ChargeCalculator.modemAdapter = modemAdapter;
    }

    public static void setWeightCharge(double weightCharge) {
        ChargeCalculator.weightCharge = weightCharge;
    }

    public static void setPenalty(double penalty) {
        ChargeCalculator.penalty = penalty;
    }

    public static double getChargeThreshold() {
        return CHARGE_THRESHOLD;
    }

    public static void setChargeThreshold(double chargeThreshold) {
        CHARGE_THRESHOLD = chargeThreshold;
    }

    public static void setMovementActivityUnits(double movementActivityUnits) {
        ChargeCalculator.movementActivityUnits = movementActivityUnits;
    }

    public static void setLookupActivityUnits(double lookupActivityUnits) {
        ChargeCalculator.lookupActivityUnits = lookupActivityUnits;
    }
}