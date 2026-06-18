FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY main.py .
# 容器启动时跑 uvicorn，监听 8000（容器内端口）
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
