package swen30006.simulation;

import swen30006.automail.MailItem;

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
    private static double activityUnitPrice = 0.224;
    private static double markupPercentage = 0.059;

    private static double movementActivityUnits = 0.1;
    private static double lookupActivityUnits = 5;

    // something may need in the future:
    private static double weightCharge;
    private static double penalty;

    public static double CalcCharge(MailItem mailItem, int movements, boolean updateBillInfo){
        int destinationFloor = mailItem.getDestFloor();
        double floorServiceFee = -1D;

        long numLookupsForThisTime = 0;

        ModemQueryResult<Double> queryResult =  modemAdapter.lookupFloorServiceFee(destinationFloor);
        floorServiceFee = queryResult.getQueryResult();
        numLookupsForThisTime = queryResult.getNumLookups();
        queryResult = null;

        // if the mail has been put back to the pool, calc accumulated movements
        double movementTotalUnits = (movements + mailItem.getMovements()) * movementActivityUnits;

        // user will be charged for only one look up fee per mail (see spec)
        double chargedLookUpTotalUnits = 1 * lookupActivityUnits;

        double realLookUpTotalUnitsForThisTime = numLookupsForThisTime * lookupActivityUnits;

        double billableActivityCost = (movementTotalUnits + chargedLookUpTotalUnits) * activityUnitPrice;

        double cost = floorServiceFee + billableActivityCost;

        double charge = cost * (1 + markupPercentage);

        // update information in the mailItem object with the latest billing data
        if(updateBillInfo) {

            mailItem.accumulateMailMovements(movements);

            mailItem.setCharge(charge);
            mailItem.setFee(floorServiceFee);
            mailItem.setCost(cost);
            mailItem.setActivity(movementTotalUnits + mailItem.getRealLookupActivities());
            mailItem.setBillableActivities(movementTotalUnits + chargedLookUpTotalUnits);
        }

        mailItem.accumulateMailLookupInfo(realLookUpTotalUnitsForThisTime);

        // this is the estimated charge
        return charge;
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