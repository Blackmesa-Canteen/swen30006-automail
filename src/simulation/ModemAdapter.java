package simulation;

import com.unimelb.swen30006.wifimodem.WifiModem;

/**
 * @program Automail
 * @description ModemAdapter is a class to handle information about connections and provide query services
 * @create 2021-04-07 23:47
 */
public class ModemAdapter {

    private WifiModem wifiModem;

    // total lookups for final statistics
    private long totalFailedLookups = 0;
    private long totalSuccessfulLookups = 0;

    public ModemAdapter(int mailRoomLocation) {
        try {
            this.wifiModem = WifiModem.getInstance(mailRoomLocation);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ModemQueryResult<Double> lookupFloorServiceFee(int destinationFloor) {
        double floorServiceFee = -1D;

        // number of lookups for only one request
        long lookupsForThisTime = 0;

        do {
            floorServiceFee = wifiModem.forwardCallToAPI_LookupPrice(destinationFloor);

            lookupsForThisTime++;

            if(floorServiceFee < 0) {
                totalFailedLookups++;
            } else {
                totalSuccessfulLookups++;
            }
            /* check whether query is successful */
        } while (floorServiceFee < 0);

        return new ModemQueryResult<>(lookupsForThisTime, floorServiceFee);
    }

    public void wifiTurnOn() throws Exception {
        System.out.println(wifiModem.Turnon());
    }

    public void wifiTurnOff() {
        System.out.println(wifiModem.Turnoff());
    }

    public WifiModem getWifiModem() {
        return wifiModem;
    }

    public void setWifiModem(WifiModem wifiModem) {
        this.wifiModem = wifiModem;
    }

    public long getTotalFailedLookups() {
        return totalFailedLookups;
    }

    public long getTotalSuccessfulLookups() {
        return totalSuccessfulLookups;
    }
}