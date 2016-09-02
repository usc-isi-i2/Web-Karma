@IF EXIST "%~dp0\node.exe" (
  "%~dp0\node.exe"  "%~dp0\compile" %*
) ELSE (
  node  "%~dp0\compile" %*
)
