package swen30006.simulation;

/**
 * @program Automail
 * @description This is a class of the result of Modem query, it contains number of lookups of this query and
 * result of this query
 * @create 2021-04-08 14:40
 */
public class ModemQueryResult<T> {
    private final long numLookups;
    private final T queryResult;

    public ModemQueryResult(long numLookups, T queryResult) {
        this.numLookups = numLookups;
        this.queryResult = queryResult;
    }

    public long getNumLookups() {
        return numLookups;
    }

    public T getQueryResult() {
        return queryResult;
    }
}