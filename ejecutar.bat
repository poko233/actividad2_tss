@echo off
REM Pipeline UMSS: recoleccion + analisis de mercado de titulados
REM Uso analisis : ejecutar.bat [titulados.csv] [empleadores.csv] [dir_salida]
REM Uso registro : ejecutar.bat registrar [titulados.csv] [empleadores.csv]
if "%1"=="registrar" goto REGISTRO
set TIT=%1
if "%TIT%"=="" set TIT=titulados.csv
set EMP=%2
if "%EMP%"=="" set EMP=empleadores.csv
set OUT=%3
if "%OUT%"=="" set OUT=salida
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin src\CsvUtil.java src\StatsUtil.java src\Analizador.java src\Registro.java
if errorlevel 1 (echo ERROR de compilacion & exit /b 1)
java -cp bin Analizador %TIT% %EMP% %OUT%
goto FIN
:REGISTRO
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin src\CsvUtil.java src\StatsUtil.java src\Registro.java
if errorlevel 1 (echo ERROR de compilacion & exit /b 1)
set RTIT=%2
if "%RTIT%"=="" set RTIT=titulados.csv
set REMP=%3
if "%REMP%"=="" set REMP=empleadores.csv
java -cp bin Registro %RTIT% %REMP%
:FIN
