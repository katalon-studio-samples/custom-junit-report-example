<p align="center">
  <a href="" rel="noopener">
 <img width=200px height=200px src="https://avatars.githubusercontent.com/u/28861843?s=200&v=4" alt="Project logo"></a>
</p>

<h1 align="center">Custom JUnit Report Example</h1>

<div align="center">

[![Status](https://img.shields.io/badge/status-active-success.svg)]()
[![GitHub Issues](https://img.shields.io/github/issues/kylelobo/The-Documentation-Compendium.svg)](https://github.com/kylelobo/The-Documentation-Compendium/issues)
[![GitHub Pull Requests](https://img.shields.io/github/issues-pr/kylelobo/The-Documentation-Compendium.svg)](https://github.com/kylelobo/The-Documentation-Compendium/pulls)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](/LICENSE)

</div>

---

<p align="center"> Few lines describing your project.
    <br> 
</p>

## 📝 Table of Contents

- [About](#about)
- [Usage](#usage)
- [References](#references)
- [Acknowledgments](#acknowledgement)

## 🧐 About <a name = "about"></a>

This is an example of customizing the Katalon JUnit report. Which includes some keywords and annotations for marking a keyword call as a separated test case in the JUnit report file.

## 🎈 Usage <a name="usage"></a>

1. Setup hooks
> First, you will need to set up a hook into keyword invoker, so that we can mark our keyword calls as need to be separated in the JUnit report file and then extract those calls to separated test cases when generating the JUnit report file.

```groovy
/**
 * TestListener.groovy
 */

@BeforeTestSuite
def sampleBeforeTestSuite(TestSuiteContext testSuiteContext) {
  println testSuiteContext.getTestSuiteId()
  CustomKeywords.'com.katalon.customjunit.CustomJUnitKeywords.overrideCustomKeywordInvoker'(CustomKeywords.class)
}
```

2. Mark our keywords those need to be separated as a test case in the JUnit report file

* The first way to do that is to use `@JUnitTestCase` annotation
```groovy
/**
 * Keywords/WebUICustomKeywords.groovy
 */

import com.katalon.junit.JUnitTestCase

public class WebUICustomKeywords {
  // ...

  @Keyword
  @JUnitTestCase
  def isElementPresent(TestObject to, int timeout) {
    List<WebElement> elements = WebUiBuiltInKeywords.findWebElements(to, timeout)
    return elements.size() > 0
  }

  // ...
}
```

* The second way is to use `CustomJUnitTestCaseUtils.markJUnitTestCase()`. In this way, you will be able to add some condition to decide this keyword call should be separated in the JUnit file or not
```groovy
/**
 * Keywords/WebUICustomKeywords.groovy
 */

import com.katalon.junit.CustomJUnitTestCaseUtils

public class WebUICustomKeywords {
  // ...

  @Keyword
  def isElementPresent(TestObject to, int timeout) {
    CustomJUnitTestCaseUtils.markJUnitTestCase();
    List<WebElement> elements = WebUiBuiltInKeywords.findWebElements(to, timeout)
    return elements.size() > 0
  }

  // ...
}
```

3. Generate our custom JUnit report after the execution

```groovy
/**
 * TestListener.groovy
 */

@AfterTestSuite
def sampleAfterTestSuite(TestSuiteContext testSuiteContext) {
  println testSuiteContext.getTestSuiteId()
  CustomKeywords.'com.katalon.customjunit.CustomJUnitKeywords.export'()
}
```

## 📄 References <a name = "references"></a>

1. `CustomJUnitKeywords`
```groovy
// Set up a hook into keywork invoker
CustomJUnitKeywords.overrideCustomKeywordInvoker()

// Export our custom JUnit report to the test suite's report folder
CustomJUnitKeywords.export()

// Export our custom JUnit report to a specified folder
CustomJUnitKeywords.export(String reportFolder)

// Export our custom JUnit report to a specified folder and a specified name
CustomJUnitKeywords.export(String reportFolder, String reportName)

// Mark a keyword call as a test case in our custom JUnit report
CustomJUnitKeywords.markJUnitTestCase()
```

4. `@JUnitTestCase`
```groovy
// An annotation for marking a keyword as a test case in the JUnit file
@Keyword
@JUnitTestCase
def isElementPresent(TestObject to, int timeout){
  List<WebElement> elements = WebUiBuiltInKeywords.findWebElements(to, timeout)
  return elements.size() > 0
}
```

2. `CustomJUnitTestCaseUtils`
```groovy
// Mark a keyword call as a test case in our custom JUnit report
CustomJUnitTestCaseUtils.markJUnitTestCase()
```

3. `JUnitExporter`
```groovy
// Export our custom JUnit report to the test suite's report folder
JUnitExporter.export()

// Export our custom JUnit report to a specified folder
JUnitExporter.export(String reportFolder)

// Export our custom JUnit report to a specified folder and a specified name
JUnitExporter.export(String reportFolder, String reportName)
```

## 🎉 Acknowledgements <a name = "acknowledgement"></a>

Note: You may notice that there is a little bit of difference between the default `JUnit_Report.xml` and `Custom_JUnit_Report.xml`. This is due to an encoding function does not work properly when running in the groovy engine.
