Write-Host "🔨 Building AI/ML Voice Assistant..." -ForegroundColor Cyan

cd E:\Project\ai-ml-voice-assistant

Write-Host "📦 Building backend..." -ForegroundColor Yellow
mvn clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Backend built successfully!" -ForegroundColor Green
    
    Write-Host "🚀 Starting backend..." -ForegroundColor Yellow
    $env:GROQ_API_KEY="your_groq_api_key_here"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd E:\Project\ai-ml-voice-assistant; `$env:GROQ_API_KEY='your_key'; java -jar target\voice-ai-assistant-1.0.0.jar"
    
    Write-Host "🎨 Starting frontend..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd E:\Project\voice-ai-frontend; npm start"
    
    Write-Host ""
    Write-Host "✅ All services started!" -ForegroundColor Green
    Write-Host "📱 Frontend: http://localhost:3000" -ForegroundColor Cyan
    Write-Host "📊 Backend: http://localhost:8080/api/voice/health" -ForegroundColor Cyan
} else {
    Write-Host "❌ Build failed!" -ForegroundColor Red
}
