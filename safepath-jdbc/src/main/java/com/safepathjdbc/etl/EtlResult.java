package com.safepathjdbc.etl;

public class EtlResult {
    private int totalRows;
    private int insertedRows;
    private int invalidRows;

    public EtlResult(int totalRows, int insertedRows, int invalidRows) {
        this.totalRows = totalRows;
        this.insertedRows = insertedRows;
        this.invalidRows = invalidRows;
    }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getInsertedRows() { return insertedRows; }
    public void setInsertedRows(int insertedRows) { this.insertedRows = insertedRows; }

    public int getInvalidRows() { return invalidRows; }
    public void setInvalidRows(int invalidRows) { this.invalidRows = invalidRows; }
}
