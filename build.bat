@echo off
echo ?? Building Spring Boot AI/ML Voice Assistant...
echo.
mvn clean package -DskipTests
if %errorlevel% == 0 (
    echo ? Build successful!
    echo.
    echo ?? To train the model: train.bat
    echo ?? To run the application: run.bat
) else (
    echo ? Build failed!
)
