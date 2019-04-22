cd /Users/you/.jenkins/workspace/JenkinsZephyrAutomationProject/

mvn -Dexec.mainClass="com.thed.zephyr.PostTestApi" -Dexec.classpathScope=test test-compile exec:java