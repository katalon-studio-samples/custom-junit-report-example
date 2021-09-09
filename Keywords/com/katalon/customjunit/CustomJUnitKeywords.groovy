package com.katalon.customjunit

import org.codehaus.groovy.runtime.InvokerHelper

import com.katalon.junit.CustomJUnitTestCaseUtils
import com.katalon.junit.CustomKeywordDelegatingMetaClass2
import com.katalon.junit.JUnitExporter
import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.main.TestCaseMain

public class CustomJUnitKeywords {
	@Keyword
	def overrideCustomKeywordInvoker(Class<?> customKeywordsClass) {
		GroovyClassLoader classLoader = new GroovyClassLoader(TestCaseMain.class.getClassLoader());
		InvokerHelper.metaRegistry.removeMetaClass(customKeywordsClass);
		InvokerHelper.metaRegistry.setMetaClass(customKeywordsClass, new CustomKeywordDelegatingMetaClass2(customKeywordsClass, classLoader));
	}

	@Keyword
	def export() {
		JUnitExporter.export();
	}

	@Keyword
	def export(String reportFolder) {
		JUnitExporter.export(reportFolder);
	}

	@Keyword
	def export(String reportFolder, String reportName) {
		JUnitExporter.export(reportFolder, reportName);
	}

	@Keyword
	def markJUnitTestCase() {
		CustomJUnitTestCaseUtils.markJUnitTestCase();
	}
}
