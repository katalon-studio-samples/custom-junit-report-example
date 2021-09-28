package com.katalon.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.kms.katalon.core.logging.KeywordLogger;
import com.kms.katalon.core.logging.model.ILogRecord;

public class CustomJUnitTestCaseUtils {
	private static final KeywordLogger logger = KeywordLogger.getInstance(CustomJUnitTestCaseUtils.class);

	private CustomJUnitTestCaseUtils() {
		// Hide the default constructor
	}
	
	public static void markJUnitTestCase() {
//        logger.logInfo(CustomJUnitConstants.JUNIT_TEST_CASE_MARKER); // does not work
        logger.logWarning(CustomJUnitConstants.JUNIT_TEST_CASE_MARKER);
	}

	public static List<ILogRecord> collectJUnitTestCases(ILogRecord[] testCases) {
		return collectJUnitTestCases(testCases, null, true);
	}

	public static List<ILogRecord> collectJUnitTestCases(ILogRecord[] records, List<ILogRecord> outputList,
			boolean removeTestCaseMarker) {
		if (outputList == null) {
			outputList = new ArrayList<ILogRecord>();
		}
		for (ILogRecord recordI : records) {
			if (isCustomJUnitTestCase(recordI)) {
				outputList.add(recordI);
				if (removeTestCaseMarker) {
					removeJUnitTestCaseMarkers(recordI);
				}
				continue;
			}
			ILogRecord[] subRecords = recordI.getChildRecords();
			if (!ArrayUtils.isEmpty(subRecords)) {
				collectJUnitTestCases(subRecords, outputList, removeTestCaseMarker);
			}
		}
		return outputList;
	}

	private static void removeJUnitTestCaseMarkers(ILogRecord testStep) {
		ILogRecord[] subSteps = testStep.getChildRecords();
		ILogRecord[] normalSteps = Arrays.stream(subSteps).filter(subStepI -> !isJUnitTestCaseMarker(subStepI))
				.collect(Collectors.toList()).toArray(new ILogRecord[] {});
//		testStep.setChildRecords(normalSteps); // Only support from version 8.1.0
	}

	private static boolean isCustomJUnitTestCase(ILogRecord testStep) {
		ILogRecord[] subSteps = testStep.getChildRecords();
		return Arrays.stream(subSteps).anyMatch((subStepI) -> isJUnitTestCaseMarker(subStepI));
	}

	private static boolean isJUnitTestCaseMarker(ILogRecord logRecord) {
		return StringUtils.equals(logRecord.getMessage(), CustomJUnitConstants.JUNIT_TEST_CASE_MARKER);
	}
}