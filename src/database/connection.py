"""
database connection management module
provides database connection and session management functionality
"""
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from src.config.settings import DB_CONFIG

def get_database_url():
    """build database connection URL"""
    return f"postgresql://{DB_CONFIG['user']}:{DB_CONFIG['password']}@{DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}"

def create_db_engine():
    """create database engine"""
    return create_engine(get_database_url())

def get_db_session():
    """get database session"""
    engine = create_db_engine()
    Session = sessionmaker(bind=engine)
    return Session()
