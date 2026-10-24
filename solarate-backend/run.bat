@echo off
REM Jalankan dari Anaconda Prompt di folder solarate-backend
call conda activate solarate-backend
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
