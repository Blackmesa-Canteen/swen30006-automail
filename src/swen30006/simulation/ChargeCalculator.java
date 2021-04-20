package swen30006.simulation;

import swen30006.automail.MailItem;
import swen30006.automail.Simulation;

/**
 * @program Automail
 * @description since this calculator will be called by MailPool & Robot, so make this as a Util class and make it
 *  as information expert of charge calculation
 * @create 2021-04-06 12:21
 */
public class ChargeCalculator {

    private static ModemAdapter modemAdapter;
    private static double CHARGE_THRESHOLD;

    // these values are configurable according to the spec
    // user can configure them with setters in Simulation class
    private static double activityUnitPrice = 0.224;
    private static double markupPercentage = 0.059;

    private static double movementActivityUnits = 5;
    private static double lookupActivityUnits = 0.1;

    // something may need in the future:
    private static double weightCharge;
    private static double penalty;

    // calculate latest charge of a specific mailItem, requires movements that the mail needed, and
    // whether should the calculation update bill info of the mail
    public static double CalcCharge(MailItem mailItem, int movements, boolean updateBillInfo){
        int destinationFloor = mailItem.getDestFloor();
        double floorServiceFee = -1D;

        long numLookupsForThisTime = 0;

        ModemQueryResult<Double> queryResult =  modemAdapter.lookupFloorServiceFee(destinationFloor);
        floorServiceFee = queryResult.getQueryResult();
        numLookupsForThisTime = queryResult.getNumLookups();
        queryResult = null;

        double movementTotalUnits = (movements) * movementActivityUnits;


        // As Automail is responsible for the infrastructure, it is only reasonable to charge the
        // tenant one such lookup fee per mail item delivery
        double chargedLookUpTotalUnits = 1 * lookupActivityUnits;

        double realLookUpTotalUnitsForThisTime = numLookupsForThisTime * lookupActivityUnits;

        double billableActivityCost = (movementTotalUnits + chargedLookUpTotalUnits) * activityUnitPrice;

        double cost = floorServiceFee + billableActivityCost;

        double charge = cost * (1 + markupPercentage);

        // accumulate look up activities
        mailItem.accumulateMailLookupInfo(realLookUpTotalUnitsForThisTime);

        // update information in the mailItem object with the latest billing data
        if(updateBillInfo) {

            mailItem.accumulateMailMovements(movements);

            mailItem.setCharge(charge);
            mailItem.setFee(floorServiceFee);
            mailItem.setCost(cost);
            mailItem.setActivity(movementTotalUnits + mailItem.getRealLookupActivities());
            mailItem.setBillableActivities(movementTotalUnits + chargedLookUpTotalUnits);
            mailItem.setActivityCost(billableActivityCost);
        }

        // this is the estimated charge
        return charge;
    }

    // Estimate mailItem Charge: mailRoom->destination->mailRoom
    public static double estimateCharge(MailItem mailItem, int mailroomLocation){

        int destinationFloor = mailItem.getDestFloor();

        // Mailroom -> DestinationFloor -> Mailroom.
        // Cost the whole round trip as though the one item is being delivered on its own
        int movements = (Math.abs(destinationFloor - mailroomLocation) * 2);

        // this is the estimated charge
        double estimatedCharge = ChargeCalculator.CalcCharge(mailItem, movements, false);

        // store estimated Charge to mailItem for sorting
        mailItem.setEstimatedCharge(estimatedCharge);
        return estimatedCharge;
    }

    public static void setModemAdapter(ModemAdapter modemAdapter) {
        ChargeCalculator.modemAdapter = modemAdapter;
    }


    public static double getChargeThreshold() {
        return CHARGE_THRESHOLD;
    }

    public static void setChargeThreshold(double chargeThreshold) {
        CHARGE_THRESHOLD = chargeThreshold;
    }

}