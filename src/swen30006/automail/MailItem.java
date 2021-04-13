package swen30006.automail;

import swen30006.simulation.ChargeCalculator;

import java.util.Map;
import java.util.TreeMap;

// import java.util.UUID;

/**
 * Represents a mail item
 */
public class MailItem {


    /* Added new attributes to be printed */
    protected double charge;
    protected double estimatedCharge;
    protected double cost;
    protected double fee;

    // robot movements required to deliver this mailitem
    protected int movements;

    // This is real total activity cost (all failed look up included)
    protected double activity;

    // this is real accumulated look up activities (failed lookup activities included)
    protected double realLookupActivities;

    // This is billable activity cost (NO failed lookup activities included)
    protected double billableActivities;

    /** Represents the destination floor to which the mail is intended to go */
    protected final int destination_floor;
    /** The mail identifier */
    protected final String id;
    /** The time the mail item arrived */
    protected final int arrival_time;
    /** The weight in grams of the mail item */
    protected final int weight;

    /**
     * Constructor for a MailItem
     * @param dest_floor the destination floor intended for this mail item
     * @param arrival_time the time that the mail arrived
     * @param weight the weight of this mail item
     */
    public MailItem(int dest_floor, int arrival_time, int weight){
        this.destination_floor = dest_floor;
        this.id = String.valueOf(hashCode());
        this.arrival_time = arrival_time;
        this.weight = weight;
        this.charge = 0;
        this.estimatedCharge = 0;
        this.cost = 0;
        this.fee = 0;
        this.activity = 0;
        this.realLookupActivities = 0;
        this.movements = 0;

    }

    @Override
    public String toString(){
        return String.format("Mail Item:: ID: %6s | Arrival: %4d | Destination: %2d | Weight: %4d", id, arrival_time, destination_floor, weight);
    }

    public String toStringWithExtraInfo() {
        return String.format("Mail Item:: ID: %6s | Arrival: %4d | Destination: %2d | Weight: %4d | Charge: %.2f | Cost: %.2f " +
                        "| Fee: %.2f | Activity: %.2f",
                id, arrival_time, destination_floor, weight, charge, cost, fee, activity);
    }

    /**
     *
     * @return the destination floor of the mail item
     */
    public int getDestFloor() {
        return destination_floor;
    }
    
    /**
     *
     * @return the ID of the mail item
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return the arrival time of the mail item
     */
    public int getArrivalTime(){
        return arrival_time;
    }

    /**
    *
    * @return the weight of the mail item
    */
   public int getWeight(){
       return weight;
   }
   
	static private int count = 0;
	static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();

	@Override
	public int hashCode() {
		Integer hash0 = super.hashCode();
		Integer hash = hashMap.get(hash0);
		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
		return hash;
	}

    public void accumulateMailLookupInfo(double realLookUpTotalUnitsForThisTime) {
        // accumulation of two lookup activities
        this.realLookupActivities += realLookUpTotalUnitsForThisTime;
    }

    public void accumulateMailMovements(double newMovements) {
        // accumulation for movements
	    this.movements += newMovements;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public double getCharge() {
        return charge;
    }

    public double getCost() {
        return cost;
    }

    public double getFee() {
        return fee;
    }

    public double getActivity() {
        return activity;
    }

    public void setActivity(double activity) {
        this.activity = activity;
    }

    public double getRealLookupActivities() {
        return realLookupActivities;
    }

    public double getBillableActivities() {
        return billableActivities;
    }

    public void setBillableActivities(double billableActivities) {
        this.billableActivities = billableActivities;
    }

    public int getDestination_floor() {
        return destination_floor;
    }

    public int getArrival_time() {
        return arrival_time;
    }

    public double getEstimatedCharge() {
        return estimatedCharge;
    }

    public void setEstimatedCharge(double estimatedCharge) {
        this.estimatedCharge = estimatedCharge;
    }

    public int getMovements() {
        return movements;
    }

    public void setMovements(int movements) {
        this.movements = movements;
    }
}
