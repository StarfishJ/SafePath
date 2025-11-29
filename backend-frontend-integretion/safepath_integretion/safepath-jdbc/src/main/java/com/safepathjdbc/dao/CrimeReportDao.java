package com.safepathjdbc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.safepathjdbc.model.CrimeReport;
import com.safepathjdbc.util.ConnectionManager;

public class CrimeReportDao {
    public CrimeReport create(CrimeReport r) throws SQLException {
        String sql = "INSERT INTO crime_reports (report_number, report_datetime, precinct, sector, beat, mcpp_neighborhood, blurred_address, blurred_latitude, blurred_longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getReportNumber());
            ps.setTimestamp(2, r.getReportDatetime() == null ? null : Timestamp.valueOf(r.getReportDatetime()));
            ps.setString(3, r.getPrecinct());
            ps.setString(4, r.getSector());
            ps.setString(5, r.getBeat());
            ps.setString(6, r.getMcppNeighborhood());
            ps.setString(7, r.getBlurredAddress());
            if (r.getBlurredLatitude() == null) ps.setNull(8, Types.DOUBLE); else ps.setDouble(8, r.getBlurredLatitude());
            if (r.getBlurredLongitude() == null) ps.setNull(9, Types.DOUBLE); else ps.setDouble(9, r.getBlurredLongitude());
            ps.executeUpdate();
            return r;
        }
    }

    public CrimeReport findById(String reportNumber) throws SQLException {
        String sql = "SELECT report_number, report_datetime, precinct, sector, beat, mcpp_neighborhood, blurred_address, blurred_latitude, blurred_longitude FROM crime_reports WHERE report_number = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reportNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CrimeReport r = new CrimeReport();
                    r.setReportNumber(rs.getString("report_number"));
                    Timestamp ts = rs.getTimestamp("report_datetime");
                    r.setReportDatetime(ts == null ? null : ts.toLocalDateTime());
                    r.setPrecinct(rs.getString("precinct"));
                    r.setSector(rs.getString("sector"));
                    r.setBeat(rs.getString("beat"));
                    r.setMcppNeighborhood(rs.getString("mcpp_neighborhood"));
                    r.setBlurredAddress(rs.getString("blurred_address"));
                    double lat = rs.getDouble("blurred_latitude"); r.setBlurredLatitude(rs.wasNull() ? null : lat);
                    double lon = rs.getDouble("blurred_longitude"); r.setBlurredLongitude(rs.wasNull() ? null : lon);
                    return r;
                }
                return null;
            }
        }
    }

    public List<CrimeReport> findAll() throws SQLException {
        String sql = "SELECT cr.report_number, cr.report_datetime, cr.precinct, cr.sector, cr.beat, cr.mcpp_neighborhood, cr.blurred_address, cr.blurred_latitude, cr.blurred_longitude, " +
                     "GROUP_CONCAT(DISTINCT ot.offense_parent_group SEPARATOR ', ') as offense_types_agg " +
                     "FROM crime_reports cr " +
                     "LEFT JOIN report_offenses ro ON cr.report_number = ro.report_number " +
                     "LEFT JOIN offense_types ot ON ro.offense_code = ot.offense_code " +
                     "GROUP BY cr.report_number, cr.report_datetime, cr.precinct, cr.sector, cr.beat, cr.mcpp_neighborhood, cr.blurred_address, cr.blurred_latitude, cr.blurred_longitude " +
                     "ORDER BY cr.report_datetime DESC";
        List<CrimeReport> out = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CrimeReport r = new CrimeReport();
                r.setReportNumber(rs.getString("report_number"));
                Timestamp ts = rs.getTimestamp("report_datetime");
                r.setReportDatetime(ts == null ? null : ts.toLocalDateTime());
                r.setPrecinct(rs.getString("precinct"));
                r.setSector(rs.getString("sector"));
                r.setBeat(rs.getString("beat"));
                r.setMcppNeighborhood(rs.getString("mcpp_neighborhood"));
                r.setBlurredAddress(rs.getString("blurred_address"));
                double lat = rs.getDouble("blurred_latitude"); r.setBlurredLatitude(rs.wasNull() ? null : lat);
                double lon = rs.getDouble("blurred_longitude"); r.setBlurredLongitude(rs.wasNull() ? null : lon);
                r.setOffenseType(rs.getString("offense_types_agg"));
                out.add(r);
            }
        }
        return out;
    }

    public boolean update(CrimeReport r) throws SQLException {
        String sql = "UPDATE crime_reports SET report_datetime = ?, precinct = ?, sector = ?, beat = ?, mcpp_neighborhood = ?, blurred_address = ?, blurred_latitude = ?, blurred_longitude = ? WHERE report_number = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, r.getReportDatetime() == null ? null : Timestamp.valueOf(r.getReportDatetime()));
            ps.setString(2, r.getPrecinct());
            ps.setString(3, r.getSector());
            ps.setString(4, r.getBeat());
            ps.setString(5, r.getMcppNeighborhood());
            ps.setString(6, r.getBlurredAddress());
            if (r.getBlurredLatitude() == null) ps.setNull(7, Types.DOUBLE); else ps.setDouble(7, r.getBlurredLatitude());
            if (r.getBlurredLongitude() == null) ps.setNull(8, Types.DOUBLE); else ps.setDouble(8, r.getBlurredLongitude());
            ps.setString(9, r.getReportNumber());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String reportNumber) throws SQLException {
        String sql = "DELETE FROM crime_reports WHERE report_number = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reportNumber);
            return ps.executeUpdate() > 0;
        }
    }

    public List<CrimeReport> search(String reportNumber, String precinct, String neighborhood) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT report_number, report_datetime, precinct, sector, beat, mcpp_neighborhood, blurred_address, blurred_latitude, blurred_longitude FROM crime_reports WHERE 1=1");
        List<String> params = new ArrayList<>();
        
        if (reportNumber != null && !reportNumber.trim().isEmpty()) {
            sql.append(" AND report_number LIKE ?");
            params.add("%" + reportNumber.trim() + "%");
        }
        if (precinct != null && !precinct.trim().isEmpty()) {
            sql.append(" AND precinct LIKE ?");
            params.add("%" + precinct.trim() + "%");
        }
        if (neighborhood != null && !neighborhood.trim().isEmpty()) {
            sql.append(" AND mcpp_neighborhood LIKE ?");
            params.add("%" + neighborhood.trim() + "%");
        }
        sql.append(" ORDER BY report_datetime DESC");
        
        List<CrimeReport> out = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CrimeReport r = new CrimeReport();
                    r.setReportNumber(rs.getString("report_number"));
                    Timestamp ts = rs.getTimestamp("report_datetime");
                    r.setReportDatetime(ts == null ? null : ts.toLocalDateTime());
                    r.setPrecinct(rs.getString("precinct"));
                    r.setSector(rs.getString("sector"));
                    r.setBeat(rs.getString("beat"));
                    r.setMcppNeighborhood(rs.getString("mcpp_neighborhood"));
                    r.setBlurredAddress(rs.getString("blurred_address"));
                    double lat = rs.getDouble("blurred_latitude"); r.setBlurredLatitude(rs.wasNull() ? null : lat);
                    double lon = rs.getDouble("blurred_longitude"); r.setBlurredLongitude(rs.wasNull() ? null : lon);
                    out.add(r);
                }
            }
        }
        return out;
    }

    public List<CrimeReport> searchByGeoAndTime(Double centerLat, Double centerLon, Integer radiusM, Integer daysBack) throws SQLException {
        // Simple bounding-box then filter in-memory by haversine distance; H2 has no spatial
        if (centerLat == null || centerLon == null || radiusM == null) {
            return findAll();
        }
        double lat = centerLat;
        double lon = centerLon;
        double radiusKm = radiusM / 1000.0;
        // 1 deg lat ~ 111 km; 1 deg lon ~ 111 km * cos(lat)
        double latDelta = radiusKm / 111.0;
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat == 0 ? 1e-6 : lat)));
        Double minLat = lat - latDelta;
        Double maxLat = lat + latDelta;
        Double minLon = lon - lonDelta;
        Double maxLon = lon + lonDelta;

        StringBuilder sql = new StringBuilder(
            "SELECT cr.report_number, cr.report_datetime, cr.precinct, cr.sector, cr.beat, cr.mcpp_neighborhood, cr.blurred_address, cr.blurred_latitude, cr.blurred_longitude, " +
            "GROUP_CONCAT(DISTINCT ot.offense_parent_group SEPARATOR ', ') as offense_types_agg " +
            "FROM crime_reports cr " +
            "LEFT JOIN report_offenses ro ON cr.report_number = ro.report_number " +
            "LEFT JOIN offense_types ot ON ro.offense_code = ot.offense_code " +
            "WHERE cr.blurred_latitude IS NOT NULL AND cr.blurred_longitude IS NOT NULL " +
            "AND cr.blurred_latitude BETWEEN ? AND ? AND cr.blurred_longitude BETWEEN ? AND ?"
        );
        if (daysBack != null && daysBack > 0) {
            sql.append(" AND cr.report_datetime >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL ? DAY)");
        }
        sql.append(" GROUP BY cr.report_number, cr.report_datetime, cr.precinct, cr.sector, cr.beat, cr.mcpp_neighborhood, cr.blurred_address, cr.blurred_latitude, cr.blurred_longitude ");
        sql.append(" ORDER BY cr.report_datetime DESC");

        List<CrimeReport> pre = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setDouble(1, minLat);
            ps.setDouble(2, maxLat);
            ps.setDouble(3, minLon);
            ps.setDouble(4, maxLon);
            if (daysBack != null && daysBack > 0) {
                ps.setInt(5, daysBack);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CrimeReport r = new CrimeReport();
                    r.setReportNumber(rs.getString("report_number"));
                    Timestamp ts = rs.getTimestamp("report_datetime");
                    r.setReportDatetime(ts == null ? null : ts.toLocalDateTime());
                    r.setPrecinct(rs.getString("precinct"));
                    r.setSector(rs.getString("sector"));
                    r.setBeat(rs.getString("beat"));
                    r.setMcppNeighborhood(rs.getString("mcpp_neighborhood"));
                    r.setBlurredAddress(rs.getString("blurred_address"));
                    double rlat = rs.getDouble("blurred_latitude"); r.setBlurredLatitude(rs.wasNull() ? null : rlat);
                    double rlon = rs.getDouble("blurred_longitude"); r.setBlurredLongitude(rs.wasNull() ? null : rlon);
                    r.setOffenseType(rs.getString("offense_types_agg"));
                    pre.add(r);
                }
            }
        }
        // In-memory distance filter
        List<CrimeReport> filtered = new ArrayList<>();
        for (CrimeReport r : pre) {
            if (r.getBlurredLatitude() == null || r.getBlurredLongitude() == null) continue;
            double d = haversine(lat, lon, r.getBlurredLatitude(), r.getBlurredLongitude());
            if (d <= radiusKm) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    public List<CrimeReport> getCrimesByFilter(double lat, double lon, int radiusInMeters, List<String> crimeTypes, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime, int limit) throws SQLException {
        // Use GROUP_CONCAT to aggregate offense types for each report
        StringBuilder sql = new StringBuilder("SELECT cr.*, GROUP_CONCAT(ot.offense_parent_group SEPARATOR ', ') as offense_types_agg ");
        sql.append("FROM crime_reports cr ");
        sql.append("LEFT JOIN report_offenses ro ON cr.report_number = ro.report_number ");
        sql.append("LEFT JOIN offense_types ot ON ro.offense_code = ot.offense_code ");
        sql.append("WHERE 1=1 ");

        if (radiusInMeters > 0) {
            // Spherical Law of Cosines: d = R * ACOS(SIN(lat1)*SIN(lat2) + COS(lat1)*COS(lat2)*COS(lon2-lon1))
            // R = 6371000 meters
            sql.append("AND (6371000 * ACOS(SIN(RADIANS(?)) * SIN(RADIANS(cr.blurred_latitude)) + COS(RADIANS(?)) * COS(RADIANS(cr.blurred_latitude)) * COS(RADIANS(cr.blurred_longitude) - RADIANS(?)))) <= ? ");
        }

        if (crimeTypes != null && !crimeTypes.isEmpty()) {
            // We need to filter by having AT LEAST ONE of the requested crime types
            // Since we are grouping, we can use HAVING or filter in WHERE before grouping.
            // Filtering in WHERE is more efficient.
            // Note: The LEFT JOIN above effectively becomes an INNER JOIN for the matching rows if we filter on the joined table here.
            // However, to ensure we get the report even if it has other offenses, we should be careful.
            // But the requirement is usually "show reports that contain these crimes".
            sql.append("AND ot.offense_parent_group IN (");
            for (int i = 0; i < crimeTypes.size(); i++) {
                sql.append(i == 0 ? "?" : ", ?");
            }
            sql.append(") ");
        }

        if (startTime != null) {
            sql.append("AND cr.report_datetime >= ? ");
        }
        if (endTime != null) {
            sql.append("AND cr.report_datetime <= ? ");
        }

        sql.append("GROUP BY cr.report_number ");
        sql.append("LIMIT ?");

        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (radiusInMeters > 0) {
                ps.setDouble(idx++, lat);
                ps.setDouble(idx++, lat);
                ps.setDouble(idx++, lon);
                ps.setInt(idx++, radiusInMeters);
            }
            if (crimeTypes != null && !crimeTypes.isEmpty()) {
                for (String type : crimeTypes) {
                    ps.setString(idx++, type);
                }
            }
            if (startTime != null) {
                ps.setTimestamp(idx++, Timestamp.valueOf(startTime));
            }
            if (endTime != null) {
                ps.setTimestamp(idx++, Timestamp.valueOf(endTime));
            }
            ps.setInt(idx++, limit);

            try (ResultSet rs = ps.executeQuery()) {
                List<CrimeReport> list = new ArrayList<>();
                while (rs.next()) {
                    CrimeReport r = new CrimeReport();
                    r.setReportNumber(rs.getString("report_number"));
                    Timestamp ts = rs.getTimestamp("report_datetime");
                    r.setReportDatetime(ts == null ? null : ts.toLocalDateTime());
                    r.setPrecinct(rs.getString("precinct"));
                    r.setSector(rs.getString("sector"));
                    r.setBeat(rs.getString("beat"));
                    r.setMcppNeighborhood(rs.getString("mcpp_neighborhood"));
                    r.setBlurredAddress(rs.getString("blurred_address"));
                    double rLat = rs.getDouble("blurred_latitude");
                    r.setBlurredLatitude(rs.wasNull() ? null : rLat);
                    double rLon = rs.getDouble("blurred_longitude");
                    r.setBlurredLongitude(rs.wasNull() ? null : rLon);
                    
                    // Set the aggregated offense types
                    r.setOffenseType(rs.getString("offense_types_agg"));
                    
                    list.add(r);
                }
                return list;
            }
        }
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }}