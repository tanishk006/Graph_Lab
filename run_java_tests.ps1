$cp = "bin;C:\Users\Tanishk\.m2\repository\org\junit\jupiter\junit-jupiter-api\5.10.3\junit-jupiter-api-5.10.3.jar;C:\Users\Tanishk\.m2\repository\org\opentest4j\opentest4j\1.3.0\opentest4j-1.3.0.jar;C:\Users\Tanishk\.m2\repository\org\junit\platform\junit-platform-commons\1.10.3\junit-platform-commons-1.10.3.jar;C:\Users\Tanishk\.m2\repository\org\apiguardian\apiguardian-api\1.1.2\apiguardian-api-1.1.2.jar"

Write-Output "Compiling test files..."
& javac.exe -cp $cp -d bin test/algorithms/GraphAlgorithmsTest.java test/algorithms/TestRunner.java

Write-Output "Running TestRunner..."
& java.exe -cp $cp algorithms.TestRunner
