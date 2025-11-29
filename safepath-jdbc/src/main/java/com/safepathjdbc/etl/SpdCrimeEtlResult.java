package com.safepathjdbc.etl;

public class SpdCrimeEtlResult extends EtlResult {
    private int insertedReports;
    private int insertedOffenses;
    private int newOffenseTypes;

    public SpdCrimeEtlResult(int totalRows, int insertedRows, int invalidRows, int insertedReports, int insertedOffenses, int newOffenseTypes) {
        super(totalRows, insertedRows, invalidRows);
        this.insertedReports = insertedReports;
        this.insertedOffenses = insertedOffenses;
        this.newOffenseTypes = newOffenseTypes;
    }

    public int getInsertedReports() { return insertedReports; }
    public void setInsertedReports(int insertedReports) { this.insertedReports = insertedReports; }

    public int getInsertedOffenses() { return insertedOffenses; }
    public void setInsertedOffenses(int insertedOffenses) { this.insertedOffenses = insertedOffenses; }

    public int getNewOffenseTypes() { return newOffenseTypes; }
    public void setNewOffenseTypes(int newOffenseTypes) { this.newOffenseTypes = newOffenseTypes; }
}
