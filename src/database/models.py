"""
database model definition file
defines the table structure for storing Seattle Police Department crime data
"""
from sqlalchemy import Column, Integer, String, DateTime, Float
from sqlalchemy.ext.declarative import declarative_base
from geoalchemy2 import Geometry

Base = declarative_base()

# crime data table model
class Crime(Base):
    """crime data table model"""
    __tablename__ = 'crimes'

    # primary key and unique identifier
    id = Column(Integer, primary_key=True)
    report_number = Column(String, unique=True, nullable=False)
    
    # crime information
    offense_id = Column(String)
    offense_type = Column(String)
    offense_code = Column(String)
    
    # time information
    date_reported = Column(DateTime)
    occurred_date = Column(DateTime)
    
    # area information
    district = Column(String)
    precinct = Column(String)
    sector = Column(String)
    beat = Column(String)
    
    # geographical location
    location = Column(Geometry('POINT'))  # PostGIS geographical point
    latitude = Column(Float)
    longitude = Column(Float)
    
    # return the string representation of the object
    def __repr__(self):
        """return the string representation of the object"""
        return f"<Crime(report_number='{self.report_number}',
            offense_type='{self.offense_type}')>"

