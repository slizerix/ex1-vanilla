# Builds the model and logic modules into artifacts/ and optionally runs the tester.
# Usage: .\build.ps1          (build only)
#        .\build.ps1 -Test    (build, then run workshop-tester.jar)
param([switch]$Test)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$apiModel = 'lib/model-1.0.jar'
$apiLogic = 'lib/logic-1.0.jar'
$tester   = 'lib/workshop-tester.jar'

Remove-Item -Recurse -Force out, artifacts -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force out/model, out/logic, artifacts | Out-Null

$modelSources = Get-ChildItem -Recurse model/src -Filter *.java | ForEach-Object FullName
javac --release 25 -d out/model -cp $apiModel $modelSources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
jar --create --file artifacts/model-impl.jar -C out/model .
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$logicSources = Get-ChildItem -Recurse logic/src -Filter *.java | ForEach-Object FullName
javac --release 25 -d out/logic -cp "$apiModel;$apiLogic;artifacts/model-impl.jar" $logicSources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
jar --create --file artifacts/logic-impl.jar -C out/logic .
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host 'Built artifacts/model-impl.jar and artifacts/logic-impl.jar'

if ($Test) {
    New-Item -ItemType Directory -Force out/test | Out-Null
    $testSources = Get-ChildItem -Recurse logic/test -Filter *.java | ForEach-Object FullName
    $implCp = "$apiModel;$apiLogic;artifacts/model-impl.jar;artifacts/logic-impl.jar"
    javac --release 25 -d out/test -cp $implCp $testSources
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    java -cp "out/test;$implCp" patmal.workshops.logic.impl.WorkshopSystemSelfTest
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    java -cp "$tester;$apiLogic;$apiModel;artifacts/model-impl.jar;artifacts/logic-impl.jar" patmal.workshops.main.Main
    exit $LASTEXITCODE
}
