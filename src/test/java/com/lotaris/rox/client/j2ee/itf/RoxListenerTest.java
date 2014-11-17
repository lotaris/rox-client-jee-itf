package com.lotaris.rox.client.j2ee.itf;

import com.lotaris.j2ee.itf.model.Description;
import com.lotaris.rox.annotations.RoxableTest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class RoxListenerTest {
	@Mock
  private Logger mockLogger = LoggerFactory.getLogger(RoxListener.class);

	private RoxListener roxListener;
	private List<com.lotaris.rox.common.model.v1.Test> results;
	
	
	static void setFinalStatic(Field field, Object newValue) throws Exception {
		field.setAccessible(true);

		// remove final modifier from field
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(null, newValue);
	}	
		
	@Before
	public void createDescription() {
		MockitoAnnotations.initMocks(this);
		
		roxListener = new RoxListener();
		results = new ArrayList<>();
		Whitebox.setInternalState(roxListener, "results", results);
		
		try {
			setFinalStatic(RoxListener.class.getDeclaredField("LOGGER"), mockLogger);
		}
		catch (Exception e) {}
	}

	/**
	 * This method is never run. It is used only to create
	 * the description object to test the rox Filter that 
	 * allows to run test by key, tag, ticket or name.
	 */
	@RoxableTest(key = "dummyKey", tags = "dummyTag", tickets = "dummyTicket")
	@com.lotaris.j2ee.itf.annotations.Test
	public Description dummyMethod(Description description) {
		return description.pass();
	}
	
	/**
	 * This method is never run. It is used only to create
	 * the description object to test the rox Filter that 
	 * allows to run test by key, tag, ticket or name.
	 */
	@com.lotaris.j2ee.itf.annotations.Test
	public Description dummyMethodWithoutAnnotation(Description description) {
		return description.pass();
	}
	
	@Test
	@RoxableTest(key = "6eb18c16a7ce")
	public void theRoxTestListenerShouldContainOneResultAfterOneTestNotification() {
		Description description = null;
		
		try {
			Method m = RoxListenerTest.class.getMethod("dummyMethod", Description.class);
			
			com.lotaris.j2ee.itf.annotations.Test a = 
				m.getAnnotation(com.lotaris.j2ee.itf.annotations.Test.class);
			
			description = new Description("groupName", a, m);
		}
		catch (NoSuchMethodException | SecurityException nme) {}

		roxListener.testEnd(description.pass());
		
		assertEquals("The listener does not contain any result where it should", results.size(), 1);
	}

	@Test
	@RoxableTest(key = "e6f2bcd5a728")
	public void theRoxTestListenerShouldNotContainAnyResultAndShouldLogWarning() {
		Description description = null;
		
		try {
			Method m = RoxListenerTest.class.getMethod("dummyMethodWithoutAnnotation", Description.class);
			
			com.lotaris.j2ee.itf.annotations.Test a = 
				m.getAnnotation(com.lotaris.j2ee.itf.annotations.Test.class);
			
			description = new Description("groupName", a, m);
		}
		catch (NoSuchMethodException | SecurityException nme) {}

		final String descriptionName = description.getName();
		
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				assertEquals(
					"The message [@" + RoxableTest.class.getSimpleName() + " annotation is missing on method name : " + descriptionName + "] is expected", 
					(String) invocation.getArguments()[0], 
					"@" + RoxableTest.class.getSimpleName() + " annotation is missing on method name : " + descriptionName);
				return null;
			}
		}).when(mockLogger).warn(any(String.class));
		
		roxListener.testEnd(description.pass());
		
		assertEquals("The listener should not contain any result", results.size(), 0);
	}
	
	@Test
	@RoxableTest(key = "3f51abb8ece5")
	public void whenDataIsAddedToDescriptionItShouldAppearInTheTestResult() {
		Description description = null;
		results.clear();
		
		try {
			Method m = RoxListenerTest.class.getMethod("dummyMethod", Description.class);
			
			com.lotaris.j2ee.itf.annotations.Test a = 
				m.getAnnotation(com.lotaris.j2ee.itf.annotations.Test.class);
			
			description = new Description("groupName", a, m);
			description.addData("test", "Test");
		}
		catch (NoSuchMethodException | SecurityException nme) {}

		roxListener.testEnd(description.pass());
		
		assertEquals("The listener does not contain any result where it should", 1, results.size());
		assertEquals("The data should contains five elements", 5, results.get(0).getData().size());
		assertNotNull("The test in the results should contain the custom data", results.get(0).getData().get("test"));
	}	
}
