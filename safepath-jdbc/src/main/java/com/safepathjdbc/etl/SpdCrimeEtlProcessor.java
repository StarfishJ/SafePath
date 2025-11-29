package com.safepathjdbc.etl;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.safepathjdbc.util.ConnectionManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SpdCrimeEtlProcessor implements EtlProcessor {

    @Override
    public EtlResult process(InputStream inputStream, int sourceId) throws Exception {
        int totalRows = 0;
        int insertedReports = 0;
        int insertedOffenses = 0;
        int newOffenseTypes = 0;
        int invalidRows = 0;

        // Cache existing offense types to minimize DB lookups
        Set<String> existingOffenseTypes = new HashSet<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT offense_code FROM offense_types")) {
            while (rs.next()) {
                existingOffenseTypes.add(rs.getString(1));
            }
        }

        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withSkipLines(1).build()) {
            String[] nextRecord;
            
            String insertReportSql = "INSERT IGNORE INTO crime_reports (report_number, report_datetime, precinct, sector, beat, mcpp_neighborhood, blurred_address, blurred_latitude, blurred_longitude, etl_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String insertOffenseTypeSql = "INSERT IGNORE INTO offense_types (offense_code, offense_name, offense_parent_group, group_a_b, crime_against_category) VALUES (?, ?, ?, ?, ?)";
            String insertReportOffenseSql = "INSERT INTO report_offenses (report_number, offense_code, offense_date, offense_start_time, offense_end_time, etl_id) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = ConnectionManager.getConnection();
                 PreparedStatement psReport = conn.prepareStatement(insertReportSql);
                 PreparedStatement psOffenseType = conn.prepareStatement(insertOffenseTypeSql);
                 PreparedStatement psReportOffense = conn.prepareStatement(insertReportOffenseSql)) {
                
                conn.setAutoCommit(false);
                
                int batchSize = 0;
                
                while ((nextRecord = csvReader.readNext()) != null) {
                    totalRows++;
                    
                    // Expected columns based on SPD Data:
                    // Report Number, Offense ID, Offense Start DateTime, Offense End DateTime, Report DateTime, Group A B, Crime Against Category, Offense Parent Group, Offense, Offense Code, Precinct, Sector, Beat, MCPP, 100 Block Address, Longitude, Latitude
                    if (nextRecord.length < 17) {
                        invalidRows++;
                        continue;
                    }

                    String reportNumber = nextRecord[0];
                    String offenseId = nextRecord[1]; // Not used as PK in our schema, but useful reference
                    String startDtStr = nextRecord[2];
                    String endDtStr = nextRecord[3];
                    String reportDtStr = nextRecord[4];
                    String groupAB = nextRecord[5];
                    String crimeAgainst = nextRecord[6];
                    String parentGroup = nextRecord[7];
                    String offenseName = nextRecord[8];
                    String offenseCode = nextRecord[9];
                    String precinct = nextRecord[10];
                    String sector = nextRecord[11];
                    String beat = nextRecord[12];
                    String mcpp = nextRecord[13];
                    String address = nextRecord[14];
                    String lonStr = nextRecord[15];
                    String latStr = nextRecord[16];

                    if (reportNumber == null || reportNumber.isEmpty() || offenseCode == null || offenseCode.isEmpty()) {
                        invalidRows++;
                        continue;
                    }

                    // 1. Handle Offense Type
                    if (!existingOffenseTypes.contains(offenseCode)) {
                        psOffenseType.setString(1, offenseCode);
                        psOffenseType.setString(2, offenseName);
                        psOffenseType.setString(3, parentGroup);
                        psOffenseType.setString(4, groupAB);
                        psOffenseType.setString(5, crimeAgainst);
                        psOffenseType.addBatch();
                        existingOffenseTypes.add(offenseCode);
                        newOffenseTypes++;
                    }

                    // 2. Handle Crime Report
                    LocalDateTime reportTime = parseDate(reportDtStr);
                    Double lat = parseDouble(latStr);
                    Double lon = parseDouble(lonStr);

                    psReport.setString(1, reportNumber);
                    psReport.setTimestamp(2, reportTime != null ? Timestamp.valueOf(reportTime) : null);
                    psReport.setString(3, precinct);
                    psReport.setString(4, sector);
                    psReport.setString(5, beat);
                    psReport.setString(6, mcpp);
                    psReport.setString(7, address);
                    if (lat != null) psReport.setDouble(8, lat); else psReport.setNull(8, java.sql.Types.DOUBLE);
                    if (lon != null) psReport.setDouble(9, lon); else psReport.setNull(9, java.sql.Types.DOUBLE);
                    psReport.setNull(10, java.sql.Types.INTEGER); // etl_id placeholder, will be updated later or passed in? 
                    // Actually, EtlProcessor interface passes sourceId, but we might want etlId. 
                    // For now, let's assume etl_id is not strictly required on every row or we need to change interface.
                    // Let's use a placeholder 0 or null for now as the interface doesn't provide etlId.
                    // Wait, the design says "Update ETL_Run table".
                    
                    psReport.addBatch();

                    // 3. Handle Report Offense
                    LocalDateTime startTime = parseDate(startDtStr);
                    LocalDateTime endTime = parseDate(endDtStr);

                    psReportOffense.setString(1, reportNumber);
                    psReportOffense.setString(2, offenseCode);
                    psReportOffense.setTimestamp(3, startTime != null ? Timestamp.valueOf(startTime) : (reportTime != null ? Timestamp.valueOf(reportTime) : Timestamp.valueOf(LocalDateTime.now())));
                    psReportOffense.setTimestamp(4, startTime != null ? Timestamp.valueOf(startTime) : null);
                    psReportOffense.setTimestamp(5, endTime != null ? Timestamp.valueOf(endTime) : null);
                    psReportOffense.setNull(6, java.sql.Types.INTEGER); // etl_id placeholder

                    psReportOffense.addBatch();
                    
                    batchSize++;

                    if (batchSize % 1000 == 0) {
                        psOffenseType.executeBatch();
                        int[] reportResults = psReport.executeBatch();
                        for (int r : reportResults) if (r > 0 || r == -2) insertedReports++;
                        
                        int[] offenseResults = psReportOffense.executeBatch();
                        for (int r : offenseResults) if (r > 0 || r == -2) insertedOffenses++;
                        
                        conn.commit();
                        batchSize = 0;
                    }
                }

                if (batchSize > 0) {
                    psOffenseType.executeBatch();
                    int[] reportResults = psReport.executeBatch();
                    for (int r : reportResults) if (r > 0 || r == -2) insertedReports++;
                    
                    int[] offenseResults = psReportOffense.executeBatch();
                    for (int r : offenseResults) if (r > 0 || r == -2) insertedOffenses++;
                    
                    conn.commit();
                }
            }
        }

        return new SpdCrimeEtlResult(totalRows, insertedReports, invalidRows, insertedReports, insertedOffenses, newOffenseTypes);
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            // SPD Data format: "MM/dd/yyyy hh:mm:ss a"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a", Locale.US);
            return LocalDateTime.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            return null;
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
