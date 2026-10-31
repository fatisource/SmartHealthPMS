@echo off
setlocal

set "PHP_EXE="
if exist "C:\xampp\php\php.exe" set "PHP_EXE=C:\xampp\php\php.exe"
if not defined PHP_EXE if exist "C:\XAMPP\php\php.exe" set "PHP_EXE=C:\XAMPP\php\php.exe"
if not defined PHP_EXE (
  echo Could not find XAMPP PHP. Update the PHP path in run-xampp.bat.
  pause
  exit /b 1
)

echo Ensure MySQL is running in the XAMPP Control Panel.
echo CarePlan will open at http://127.0.0.1:8000
echo Open that address after the PHP server starts.
"%PHP_EXE%" -S 127.0.0.1:8000 -t "%~dp0"
