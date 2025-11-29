package com.safepathjdbc.etl;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.safepathjdbc.util.ConnectionManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class RealTimeEtlProcessor implements EtlProcessor {

    @Override
    public EtlResult process(InputStream inputStream, int sourceId) throws Exception {
        int totalRows = 0;
        int insertedRows = 0;
        int invalidRows = 0;

        // Assuming the first line is header and skipping it
        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withSkipLines(1).build()) {
            String[] nextRecord;
            
            // Using INSERT IGNORE to handle duplicates (requires H2 in MySQL mode or MySQL)
            String sql = "INSERT IGNORE INTO realtime_incidents (incident_id, incident_type, event_datetime, address, latitude, longitude, report_location, source_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = ConnectionManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                conn.setAutoCommit(false);
                
                int batchSize = 0;
                
                while ((nextRecord = csvReader.readNext()) != null) {
                    totalRows++;
                    
                    // Basic validation of column count (assuming at least 6 columns: ID, Type, Date, Address, Lat, Lon)
                    if (nextRecord.length < 6) {
                        invalidRows++;
                        continue;
                    }

                    String incidentId = nextRecord[0];
                    String type = nextRecord[1];
                    String datetimeStr = nextRecord[2];
                    String address = nextRecord[3];
                    String latStr = nextRecord[4];
                    String lonStr = nextRecord[5];
                    String reportLoc = nextRecord.length > 6 ? nextRecord[6] : "";

                    if (incidentId == null || incidentId.trim().isEmpty()) {
                        invalidRows++;
                        continue;
                    }

                    LocalDateTime eventTime = parseDate(datetimeStr);
                    Double lat = parseDouble(latStr);
                    Double lon = parseDouble(lonStr);

                    if (eventTime == null || lat == null || lon == null) {
                        invalidRows++;
                        continue;
                    }

                    ps.setString(1, incidentId);
                    ps.setString(2, type);
                    ps.setTimestamp(3, Timestamp.valueOf(eventTime));
                    ps.setString(4, address);
                    ps.setDouble(5, lat);
                    ps.setDouble(6, lon);
                    ps.setString(7, reportLoc);
                    ps.setInt(8, sourceId);
                    
                    ps.addBatch();
                    batchSize++;
                    
                    if (batchSize % 1000 == 0) {
                        int[] results = ps.executeBatch();
                        for (int r : results) {
                            // 1 means inserted, 0 means ignored (duplicate), Statement.SUCCESS_NO_INFO (-2) means success but count unknown
                            if (r > 0 || r == -2) {
                                insertedRows++;
                            }
                        }
                        conn.commit();
                        batchSize = 0;
                    }
                }
                
                if (batchSize > 0) {
                    int[] results = ps.executeBatch();
                    for (int r : results) {
                        if (r > 0 || r == -2) {
                            insertedRows++;
                        }
                    }
                    conn.commit();
                }
            }
        }

        return new EtlResult(totalRows, insertedRows, invalidRows);
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            // Try standard format "MM/dd/yyyy hh:mm:ss a" often used in US government data
            // e.g. "11/26/2025 02:30:00 PM"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a", Locale.US);
            return LocalDateTime.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            try {
                // Try ISO format
                return LocalDateTime.parse(dateStr);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private Double parseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return null;
        }
    }
}
