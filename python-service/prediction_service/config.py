import os

class Config:
    # API Configuration
    API_HOST = os.getenv("API_HOST", "0.0.0.0")
    API_PORT = int(os.getenv("API_PORT", "8000"))

    # Model Configuration
    MODEL_VERSION = os.getenv("MODEL_VERSION", "v1.0.0")
    DEFAULT_PREDICTION_HORIZON = 7

    # Data Configuration
    HISTORICAL_DATA_DAYS = 90
    EXTERNAL_FACTOR_DAYS = 30

    # Prediction Configuration
    CONFIDENCE_INTERVALS = [0.80, 0.95]
    PROBABILITY_DISTRIBUTION_BINS = 10

    # Java Service Integration
    JAVA_SERVICE_URL = os.getenv("JAVA_SERVICE_URL", "http://localhost:8080")
