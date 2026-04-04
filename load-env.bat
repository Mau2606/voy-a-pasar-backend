@echo off
REM ── Carga las variables del archivo .env como variables de entorno ──
REM    Se ejecuta ANTES de spring-boot:run para que el proceso hijo las herede.

if not exist ".env" (
    echo [WARN] No se encontro .env en %CD% — usando valores por defecto.
    goto :eof
)

echo Cargando variables de entorno desde .env...
for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do (
    if not "%%A"=="" set "%%A=%%B"
)
echo Variables cargadas OK.
echo DB_HOST=%DB_HOST%
echo DB_USERNAME=%DB_USERNAME%
echo DB_PASSWORD=***SET***
