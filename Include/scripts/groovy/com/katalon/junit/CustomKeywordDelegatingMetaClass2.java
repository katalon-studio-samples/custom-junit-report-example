package com.katalon.junit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import com.kms.katalon.core.exception.StepErrorException;
import com.kms.katalon.core.exception.StepFailedException;
import com.kms.katalon.core.logging.ErrorCollector;
import com.kms.katalon.core.logging.KeywordLogger;
import com.kms.katalon.core.logging.LogLevel;
import com.kms.katalon.core.util.internal.ExceptionsUtil;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;

public class CustomKeywordDelegatingMetaClass2 extends DelegatingMetaClass {

	private static final KeywordLogger logger = KeywordLogger.getInstance(CustomKeywordDelegatingMetaClass2.class);

	private GroovyClassLoader groovyClassLoader;

	private ErrorCollector errorCollector = ErrorCollector.getCollector();

	public CustomKeywordDelegatingMetaClass2(final Class<?> clazz, GroovyClassLoader groovyClassLoader) {
		super(clazz);
		delegate = (GroovySystem.getMetaClassRegistry().getMetaClass(clazz));
		delegate.initialize();
		this.groovyClassLoader = groovyClassLoader;
	}

	@Override
	public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
		boolean isJUnitTestCase = isJUnitTestCase(object, methodName, arguments);
		if (isJUnitTestCase) {
			CustomJUnitTestCaseUtils.markJUnitTestCase();
		}
		return defaultInvokeStaticMethod(object, methodName, arguments);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean isJUnitTestCase(Object object, String methodName, Object[] arguments) {
		try {
			int classAndMethodSeparatorIndex = methodName.lastIndexOf(".");
			String customKeywordClassName = methodName.substring(0, classAndMethodSeparatorIndex);
			Class<?> customKeywordClass = getCustomKeywordClassAndSetMetaClass(customKeywordClassName);
			String customKeywordMethodName = methodName.substring(classAndMethodSeparatorIndex + 1,
					methodName.length());

			List<Class> argumentClasses = new ArrayList<>();
			List<Class> primitiveArgumentClasses = new ArrayList<>();
			for (Object argI : arguments) {
				Class argClass = argI.getClass();
				argumentClasses.add(argClass);
				primitiveArgumentClasses.add(TypeUtils.toUnboxed(argClass));
			}

			Method customKeywordMethod = null;
			try {
				customKeywordMethod = customKeywordClass.getMethod(customKeywordMethodName,
						argumentClasses.toArray(new Class[] {}));
			} catch (Throwable exception) {
				customKeywordMethod = customKeywordClass.getMethod(customKeywordMethodName,
						primitiveArgumentClasses.toArray(new Class[] {}));
			}

			return customKeywordMethod.isAnnotationPresent(JUnitTestCase.class);
		} catch (Throwable throwable) {
			// Just skip
		}
		return false;
	}

	private Object defaultInvokeStaticMethod(Object object, String methodName, Object[] arguments) {
		List<Throwable> oldErrors = errorCollector.getCoppiedErrors();
		boolean oldIsKeywordPassed = errorCollector.isKeywordPassed();
		try {
			errorCollector.clearErrors();
			errorCollector.setKeywordPassed(false);

			int classAndMethodSeparatorIndex = methodName.lastIndexOf(".");
			String customKeywordClassName = methodName.substring(0, classAndMethodSeparatorIndex);
			Class<?> customKeywordClass = getCustomKeywordClassAndSetMetaClass(customKeywordClassName);
			GroovyObject obj = (GroovyObject) customKeywordClass.newInstance();

			String customKeywordMethodName = methodName.substring(classAndMethodSeparatorIndex + 1,
					methodName.length());
			Object result = obj.invokeMethod(customKeywordMethodName, arguments);

			if (errorCollector.containsErrors()) {
				Throwable throwable = errorCollector.getFirstError();

				logger.logMessage(ErrorCollector.fromError(throwable), ExceptionsUtil.getMessageForThrowable(throwable),
						throwable);
			} else if (!errorCollector.isKeywordPassed()) {
				logger.logMessage(LogLevel.PASSED, methodName + " is PASSED");
			}

			return result;
		} catch (Throwable throwable) {
			errorCollector.addError(throwable);

			Throwable errorToThrow = errorCollector.getFirstError();
			if (errorToThrow != null) {
				throwError(errorToThrow);
			}
			return null;
		} finally {
			// return previous errors to error collector
			errorCollector.getErrors().addAll(0, oldErrors);
			errorCollector.setKeywordPassed(oldIsKeywordPassed);
		}
	}

	private static void throwError(Throwable error) {
		if (ErrorCollector.isErrorFailed(error)) {
			logger.logFailed(error.getMessage(), null, error);
			if (error instanceof InvokerInvocationException) {
				throw (InvokerInvocationException) error;
			}
			if (error instanceof AssertionError) {
				throw (AssertionError) error;
			}
			if (error instanceof StepFailedException) {
				throw (StepFailedException) error;
			}
			throw new StepFailedException(error);
		}
		logger.logError(error.getMessage(), null, error);
		if (error instanceof StepErrorException) {
			throw (StepErrorException) error;
		}
		throw new StepErrorException(error);
	}

	private Class<?> getCustomKeywordClassAndSetMetaClass(String customKeywordClassName) throws ClassNotFoundException {
		Class<?> customKeywordClass = groovyClassLoader.loadClass(customKeywordClassName);

		MetaClass keywordMetaClass = InvokerHelper.metaRegistry.getMetaClass(customKeywordClass);
		if (!(keywordMetaClass instanceof KeywordClassDelegatingMetaClass2)) {
			InvokerHelper.metaRegistry.setMetaClass(customKeywordClass,
					new KeywordClassDelegatingMetaClass2(customKeywordClass, groovyClassLoader));
		}
		return customKeywordClass;
	}
}