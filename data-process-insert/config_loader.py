"""
unified config loader
read the database configuration from the db.properties file
if the db.properties file is not available, 
read the database configuration from the .env file
"""
import os
import pathlib
from typing import Optional


def load_db_config_from_properties() -> Optional[dict]:
    """read the database configuration from the db.properties file"""
    # find the project root directory (upward lookup until finding the db.properties file)
    current_dir = pathlib.Path(__file__).parent.resolve()
    project_root = current_dir.parent
    db_properties_file = project_root / "db.properties"

    if not db_properties_file.exists():
        return None

    config = {}
    try:
        with open(db_properties_file, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                # skip comments and empty lines
                if not line or line.startswith('#'):
                    continue
                if '=' in line:
                    key, value = line.split('=', 1)
                    key = key.strip()
                    value = value.strip()
                    config[key] = value
        return config
    except Exception as e:
        print(f"Warning: Could not read db.properties: {e}")
        return None


def get_db_config() -> dict:
    """
    get the database configuration
    priority: environment variables > db.properties > .env file (fallback to using environment variables if the db.properties file is not available)
    
    all Python scripts should use this function to get the database configuration,
    so that only the db.properties file needs to be modified to manage the password uniformly.
    """
    config = {}
    
    # 1. read the database configuration from the db.properties file (primary configuration source)
    props = load_db_config_from_properties()
    if props:
        # map the keys in the db.properties file to the standard environment variable names
        config['DB_HOST'] = props.get('db.host') or props.get('DB_HOST') or 'localhost'
        config['DB_PORT'] = props.get('db.port') or props.get('DB_PORT') or '3306'
        config['DB_USER'] = (props.get('db.user') or 
                           props.get('jdbc.username') or 
                           props.get('spring.datasource.username') or 
                           'root')
        config['DB_PASSWORD'] = (props.get('db.password') or 
                                props.get('jdbc.password') or 
                                props.get('spring.datasource.password') or 
                                '')
        config['DB_NAME'] = props.get('db.name') or props.get('DB_NAME') or 'safepath'
    
    # 2. environment variables override (highest priority, can override the configuration file)
    for key in ['DB_HOST', 'DB_PORT', 'DB_USER', 'DB_PASSWORD', 'DB_NAME']:
        env_value = os.getenv(key)
        if env_value:
            config[key] = env_value
    
    # 3. if the db.properties file does not have a password, try to read from the .env file (fallback to using environment variables if the db.properties file is not available)
    if not config.get('DB_PASSWORD'):
        try:
            from dotenv import load_dotenv
            project_root = pathlib.Path(__file__).parent.parent
            env_file = project_root / ".env"
            if env_file.exists():
                load_dotenv(env_file)
                for key in ['DB_HOST', 'DB_PORT', 'DB_USER', 'DB_PASSWORD', 'DB_NAME']:
                    if key not in config or not config[key]:
                        env_value = os.getenv(key)
                        if env_value:
                            config[key] = env_value
        except ImportError:
            pass
    return config