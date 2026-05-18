# Docker 图片管理 - 快速启动和测试脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Docker 图片管理系统 - 快速启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 切换到 backend 目录
Set-Location "C:\Users\Sunhongye\Desktop\学校实习\project\backend"

# ============================================
# 第一步：创建图片目录
# ============================================
Write-Host "[1/4] 创建图片目录..." -ForegroundColor Yellow

$picsPath = "C:\Users\Sunhongye\Desktop\学校实习\project\pics"
if (-not (Test-Path $picsPath)) {
    New-Item -ItemType Directory -Path $picsPath | Out-Null
    Write-Host "  ✅ 创建 pics 目录" -ForegroundColor Green
} else {
    Write-Host "  ℹ️  pics 目录已存在" -ForegroundColor Gray
}

$subdirs = @("products", "banners")
foreach ($dir in $subdirs) {
    $path = Join-Path $picsPath $dir
    if (-not (Test-Path $path)) {
        New-Item -ItemType Directory -Path $path | Out-Null
        Write-Host "  ✅ 创建 pics/$dir 目录" -ForegroundColor Green
    } else {
        Write-Host "  ℹ️  pics/$dir 目录已存在" -ForegroundColor Gray
    }
}

Write-Host ""

# ============================================
# 第二步：停止旧容器
# ============================================
Write-Host "[2/4] 停止旧容器..." -ForegroundColor Yellow
docker-compose down 2>&1 | Out-Null
Start-Sleep -Seconds 2
Write-Host "  ✅ 旧容器已停止" -ForegroundColor Green
Write-Host ""

# ============================================
# 第三步：启动 Docker
# ============================================
Write-Host "[3/4] 启动 Docker 容器..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ Docker 启动失败！请检查 Docker Desktop 是否运行" -ForegroundColor Red
    exit 1
}

Write-Host "  ✅ Docker 容器启动成功" -ForegroundColor Green
Write-Host ""

# ============================================
# 第四步：等待服务就绪
# ============================================
Write-Host "[4/4] 等待服务启动（约30秒）..." -ForegroundColor Yellow

$maxRetries = 15
$retryCount = 0
$healthCheckUrl = "http://localhost:8080/actuator/health"

while ($retryCount -lt $maxRetries) {
    Start-Sleep -Seconds 2
    $retryCount++
    
    try {
        $response = Invoke-RestMethod -Uri $healthCheckUrl -Method Get -TimeoutSec 2 -ErrorAction Stop
        if ($response.status -eq "UP") {
            Write-Host "  ✅ 后端服务已就绪！" -ForegroundColor Green
            break
        }
    } catch {
        Write-Host "     等待中... ($retryCount/$maxRetries)" -ForegroundColor Gray
    }
}

if ($retryCount -ge $maxRetries) {
    Write-Host "  ⚠️  服务启动超时，请查看日志" -ForegroundColor Yellow
    Write-Host "     docker-compose logs backend" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 显示使用说明
# ============================================
Write-Host "📁 图片目录位置：" -ForegroundColor Yellow
Write-Host "   C:\Users\Sunhongye\Desktop\学校实习\project\pics" -ForegroundColor White
Write-Host ""

Write-Host "📝 使用方法：" -ForegroundColor Yellow
Write-Host "  1. 将图片复制到对应文件夹：" -ForegroundColor White
Write-Host "     - 商品图片: pics/products/" -ForegroundColor Gray
Write-Host "     - 轮播图:   pics/banners/" -ForegroundColor Gray
Write-Host ""
Write-Host "  2. 数据库中记录路径：" -ForegroundColor White
Write-Host "     - 商品图片: /pics/products/图片名.jpg" -ForegroundColor Gray
Write-Host "     - 轮播图:   /pics/banners/图片名.jpg" -ForegroundColor Gray
Write-Host ""
Write-Host "  3. 前端访问URL：" -ForegroundColor White
Write-Host "     http://localhost:8080/pics/products/图片名.jpg" -ForegroundColor Gray
Write-Host ""

Write-Host "🧪 测试步骤：" -ForegroundColor Yellow
Write-Host "  1. 复制一张测试图片到 pics/products/test.jpg" -ForegroundColor White
Write-Host "  2. 浏览器访问: http://localhost:8080/pics/products/test.jpg" -ForegroundColor White
Write-Host "  3. 应该能看到图片" -ForegroundColor White
Write-Host ""

Write-Host "🔍 常用命令：" -ForegroundColor Yellow
Write-Host "  查看日志:   docker-compose logs -f backend" -ForegroundColor Gray
Write-Host "  停止服务:   docker-compose down" -ForegroundColor Gray
Write-Host "  重启服务:   docker-compose restart" -ForegroundColor Gray
Write-Host ""

Write-Host "📖 详细文档: backend/documents/IMAGE_MANAGEMENT.md" -ForegroundColor Cyan
Write-Host ""
