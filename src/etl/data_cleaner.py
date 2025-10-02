"""
data cleaning module
used to clean and standardize Seattle Police Department crime data
"""
import pandas as pd
from datetime import datetime
from typing import Dict, Any, Tuple

def clean_date(date_str: str) -> datetime:
    """
    convert string date to datetime object
    
    Args:
        date_str: date string
        
    Returns:
        datetime: converted date object, if conversion fails then return None
    """
    try:
        return pd.to_datetime(date_str)
    except:
        return None

def clean_coordinates(row: Dict[Any, Any]) -> Tuple[float, float]:
    """
    extract and clean latitude and longitude data
    
    Args:
        row: dictionary containing latitude and longitude data
        
    Returns:
        tuple: (latitude, longitude) tuple, if data is invalid then return (0.0, 0.0)
    """
    try:
        lat = float(row.get('latitude', 0))
        lon = float(row.get('longitude', 0))
        # check if latitude and longitude are in a reasonable range
        if -90 <= lat <= 90 and -180 <= lon <= 180:
            return lat, lon
        return 0.0, 0.0
    except:
        return 0.0, 0.0

def clean_crime_data(df: pd.DataFrame) -> pd.DataFrame:
    """
    clean and standardize crime data
    
    Args:
        df: original crime data DataFrame
        
    Returns:
        DataFrame: cleaned data
    """
    # create a copy to avoid modifying the original data
    df = df.copy()
    
    # clean date
    df['date_reported'] = df['date_reported'].apply(clean_date)
    df['occurred_date'] = df['occurred_date'].apply(clean_date)
    
    # clean coordinates
    df['latitude'], df['longitude'] = zip(*df.apply(clean_coordinates, axis=1))
    
    # delete duplicate data
    df = df.drop_duplicates(subset=['report_number'])
    
    # fill missing values
    df = df.fillna({
        'offense_type': 'Unknown',
        'district': 'Unknown',
        'precinct': 'Unknown',
        'sector': 'Unknown',
        'beat': 'Unknown'
    })
    
    return df
