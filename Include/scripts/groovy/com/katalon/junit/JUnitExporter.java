package com.katalon.junit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.kms.katalon.core.configuration.RunConfiguration;
import com.kms.katalon.core.logging.XMLParserException;
import com.kms.katalon.core.logging.model.ILogRecord;
import com.kms.katalon.core.logging.model.TestStatus;
import com.kms.katalon.core.logging.model.TestSuiteLogRecord;
import com.kms.katalon.core.logging.model.TestStatus.TestStatusValue;
import com.kms.katalon.core.reporting.JUnitError;
import com.kms.katalon.core.reporting.JUnitFailure;
import com.kms.katalon.core.reporting.JUnitProperties;
import com.kms.katalon.core.reporting.JUnitProperty;
import com.kms.katalon.core.reporting.JUnitReportObjectFactory;
import com.kms.katalon.core.reporting.JUnitTestCase;
import com.kms.katalon.core.reporting.JUnitTestSuite;
import com.kms.katalon.core.reporting.JUnitTestSuites;
import com.kms.katalon.core.reporting.ReportUtil;
import com.kms.katalon.core.util.KeywordUtil;
import com.kms.katalon.core.util.internal.DateUtil;

@SuppressWarnings("deprecation")
public class JUnitExporter {
	public static void export() throws JAXBException, IOException, XMLParserException, XMLStreamException {
		export(RunConfiguration.getReportFolder());
	}

	public static void export(String reportFolder)
			throws JAXBException, IOException, XMLParserException, XMLStreamException {
		export(reportFolder, CustomJUnitConstants.DEFAULT_REPORT_NAME);
	}

	public static void export(String reportFolder, String reportName)
			throws JAXBException, IOException, XMLParserException, XMLStreamException {
		File reportFolderFile = new File(reportFolder);
		TestSuiteLogRecord suiteLogEntity = ReportUtil.generate(reportFolderFile.getAbsolutePath());
		KeywordUtil.logInfo("Start generating JUnit report folder at: " + reportFolder + "...");
		writeJUnitReport(suiteLogEntity, reportFolderFile, reportName);
		KeywordUtil.logInfo("JUnit report generated");
	}

	public static void writeJUnitReport(TestSuiteLogRecord suiteLogEntity, File logFolder, String reportName)
			throws JAXBException, IOException {
		JUnitReportObjectFactory factory = new JUnitReportObjectFactory();
		JUnitTestSuite ts = generateJUnitTestSuite(suiteLogEntity);

		// This is a single test suite. Thus, the info for the test suites is the same
		// as test suite
		JUnitTestSuites tss = factory.createTestSuites();
		// errors: total number of tests with error result from all test suite
		tss.setErrors(ts.getErrors());
		// failures: total number of failed tests from all test suite
		tss.setFailures(ts.getFailures());
		// tests: total number of tests from all test suite
		tss.setTests(ts.getTests());
		// time: in seconds to execute all test suites
		tss.setTime(ts.getTime());
		// name
		tss.setName(ts.getName());

		tss.getTestsuite().add(ts);

		JAXBContext context = JAXBContext
				.newInstance(new Class[] { JUnitError.class, JUnitFailure.class, JUnitProperties.class,
						JUnitProperty.class, JUnitTestCase.class, JUnitTestSuites.class, JUnitTestSuite.class });
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(tss, new File(logFolder, reportName));
	}

	public static JUnitTestSuite generateJUnitTestSuite(TestSuiteLogRecord suiteLogEntity) {
		JUnitReportObjectFactory factory = new JUnitReportObjectFactory();

		String testSuiteName = suiteLogEntity.getName();
		String totalTests = suiteLogEntity.getTotalTestCases() + "";
		String totalError = suiteLogEntity.getTotalErrorTestCases() + "";
		String totalFailure = suiteLogEntity.getTotalFailedTestCases() + "";
		String totalSkipped = suiteLogEntity.getTotalSkippedTestCases() + "";
		String duration = ((float) (suiteLogEntity.getEndTime() - suiteLogEntity.getStartTime()) / 1000) + "";

		JUnitProperties properties = factory.createProperties();
		List<JUnitProperty> propertyList = properties.getProperty();
		propertyList.add(new JUnitProperty("deviceName", suiteLogEntity.getDeviceName()));
		propertyList.add(new JUnitProperty("devicePlatform", suiteLogEntity.getDevicePlatform()));
		propertyList.add(new JUnitProperty("logFolder", StringEscapeUtils.escapeJava(suiteLogEntity.getLogFolder())));
		propertyList.add(new JUnitProperty("logFiles", factory.sanitizeReportLogs(suiteLogEntity)));
		propertyList.add(new JUnitProperty("attachments", factory.sanitizeReportAttachments(suiteLogEntity)));
		suiteLogEntity.getRunData().forEach((name, value) -> propertyList.add(new JUnitProperty(name, value)));

		JUnitTestSuite ts = factory.createTestSuite();
		ts.setProperties(properties);
		ts.setId(suiteLogEntity.getId());
		ts.setName(testSuiteName);
		ts.setHostname(suiteLogEntity.getHostName());
		ts.setTime(duration);
		ts.setTimestamp(DateUtil.getDateTimeFormatted(suiteLogEntity.getStartTime()));
		ts.setSystemOut(suiteLogEntity.getSystemOutMsg().trim());
		ts.setSystemErr(suiteLogEntity.getSystemErrorMsg().trim());

		// tests: The total number of tests in the suite, required
		ts.setTests(totalTests);
		// errors: The total number of tests in the suite that error
		ts.setErrors(totalError);
		// failures: The total number of tests in the suite that failed
		ts.setFailures(totalFailure);
		// skipped: The total number of tests in the suite that is skipped by users
		ts.setSkipped(totalSkipped);

		List<ILogRecord> allTestCases = new ArrayList<>();

		ILogRecord[] testCases = suiteLogEntity.getChildRecords();
		allTestCases.addAll(Arrays.asList(testCases));
		allTestCases.addAll(CustomJUnitTestCaseUtils.collectJUnitTestCases(testCases));

		allTestCases.stream().forEach(item -> {
			JUnitTestCase tc = factory.createTestCase();
			String time = ((float) (item.getEndTime() - item.getStartTime()) / 1000) + "";

			tc.setClassname(item.getId());
			tc.setName(item.getName());
			tc.setTime(time);

			TestStatus status = item.getStatus();
			TestStatusValue statusValue = status.getStatusValue();
			String statusName = statusValue.name();
			String message = StringUtils.removeStart(item.getMessage(),
					item.getName() + " " + statusName + " because (of) ");
			tc.setStatus(statusName);
			if (TestStatusValue.ERROR == statusValue) {
				JUnitError error = factory.createError();
				error.setType(statusName);
				error.setMessage(message);
				tc.getError().add(error);
			}
			if (TestStatusValue.FAILED == statusValue) {
				JUnitFailure failure = factory.createFailure();
				failure.setType(statusName);
				failure.setMessage(message);
				tc.getFailure().add(failure);
			}

			tc.getSystemOut().add(item.getSystemOutMsg().trim());
			tc.getSystemErr().add(item.getSystemErrorMsg().trim());
			ts.getTestcase().add(tc);
		});

		return ts;
	}
}