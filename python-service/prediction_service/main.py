from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict
import logging
import uvicorn

from .services.prediction import PredictionService
from .config import Config

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 初始化应用
app = FastAPI(
    title="Carbon Price Prediction Service",
    description="Multi-source data fusion price prediction API",
    version="1.0.0"
)

# 初始化预测服务
prediction_service = PredictionService()

# 请求模型
class PredictionRequest(BaseModel):
    horizon: Optional[int] = 7
    train: Optional[bool] = False

class TrainRequest(BaseModel):
    historical_days: Optional[int] = 90
    factor_days: Optional[int] = 30

# 响应模型
class PredictionResponse(BaseModel):
    status: str
    data: Optional[Dict] = None
    message: Optional[str] = None

@app.get("/")
async def root():
    """健康检查"""
    return {
        "status": "healthy",
        "service": "Carbon Price Prediction",
        "version": "1.0.0"
    }

@app.get("/health")
async def health():
    """健康检查"""
    return {"status": "ok"}

@app.post("/predict", response_model=PredictionResponse)
async def predict(request: PredictionRequest):
    """
    执行价格预测
    """
    try:
        if request.train or not prediction_service.is_trained:
            # 先训练模型
            train_result = prediction_service.train()
            logger.info(f"Model training: {train_result}")

        # 执行预测
        result = prediction_service.predict(request.horizon)

        return PredictionResponse(
            status="success",
            data=result
        )

    except Exception as e:
        logger.error(f"Prediction error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/train", response_model=PredictionResponse)
async def train(request: TrainRequest):
    """
    训练模型
    """
    try:
        result = prediction_service.train(
            request.historical_days,
            request.factor_days
        )

        return PredictionResponse(
            status=result['status'],
            message=result.get('message')
        )

    except Exception as e:
        logger.error(f"Training error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/model/info")
async def model_info():
    """获取模型信息"""
    try:
        info = prediction_service.get_model_info()
        return {
            "status": "success",
            "data": info
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/factors/latest")
async def get_latest_factors():
    """获取最新外部因子"""
    try:
        factors = prediction_service.data_loader.load_external_factors(1)
        if not factors.empty:
            return {
                "status": "success",
                "data": factors.iloc[-1].to_dict()
            }
        return {
            "status": "success",
            "data": {}
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host=Config.API_HOST, port=Config.API_PORT)
