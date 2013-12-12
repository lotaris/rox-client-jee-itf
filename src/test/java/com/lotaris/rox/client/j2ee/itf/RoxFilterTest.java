package com.lotaris.rox.client.j2ee.itf;

import com.lotaris.j2ee.itf.model.Description;
import com.lotaris.rox.annotations.RoxableTest;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class RoxFilterTest {

	Description description;
	
	@Before
	public void createDescription() {
		try {
			Method m = RoxFilterTest.class.getMethod("dummyMethod", Description.class);
			
			com.lotaris.j2ee.itf.annotations.Test a = 
				m.getAnnotation(com.lotaris.j2ee.itf.annotations.Test.class);
			
			description = new Description("groupName", a, m);
		}
		catch (NoSuchMethodException nme) {}
		catch (SecurityException se) {}
	}
	
	/**
	 * This method is never run. It is used only to create
	 * the description object to test the rox Filter that 
	 * allows to run test by key, tag, ticket or name.
	 */
	@RoxableTest(key = "dummyKey", tags = "dummyTag", tickets = "dummyTicket")
	@com.lotaris.j2ee.itf.annotations.Test
	public Description dummyMethod(Description description) {
		return description;
	}
	
	@Test
	@RoxableTest(key = "e2454cc2ae17")
	public void descriptionShouldBeRunnableWhenNoFilterIsSpecified() {
		RoxFilter rf = new RoxFilter(null);
		assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
	}

	@Test
	@RoxableTest(key = "e7109fbc50c8")
	public void descriptionShouldBeRunnableWhenValidKeyIsSpecified() {
		RoxFilter rf = new RoxFilter(new String[]{"key:dummyKey"});
		assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
	}

	@Test
	@RoxableTest(key = "4d8120dd710f")
	public void descriptionShouldNotBeRunnableWhenInvalidKeyIsSpecified() {
		RoxFilter rf = new RoxFilter(new String[]{"key:noKey"});
		assertFalse("The test is runnable when it should not", rf.isRunnable(description));
	}
	
	@Test
	@RoxableTest(key = "b8d91204cf3a")
	public void descriptionShouldBeRunnableWhenValidNameIsSpecified() {
		RoxFilter rf = new RoxFilter(new String[]{"name:dummyMethod"});
		assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
	}

	@Test
	@RoxableTest(key = "aa1dd5da0982")
	public void descriptionShouldNotBeRunnableWhenInvalidNameIsSpecified() {
		RoxFilter rf = new RoxFilter(new String[]{"name:noMethod"});
		assertFalse("The test is runnable when it should not", rf.isRunnable(description));
	}
	
	@Test
	@RoxableTest(key = "93aa5a8efb13")
	public void descriptionShouldBeRunnableWhenValidTagIsSpecified() {
		RoxFilter rf = new RoxFilter(new String[]{"tag:dummyTag"});
		assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
	}
	
	@Test
	@RoxableTest(key = "22f05a5eb161")
	public void descriptionShouldNotBeRunnableWhenInvalidTagIsSpecified() {
		RoxFilter rf = new RoxFilter(new String[]{"tag:noTag"});
		assertFalse("The test is runnable when it should not", rf.isRunnable(description));
	}
	
	@Test
	@RoxableTest(key = "0c9e59a9df76")
	public void descriptionShouldBeRunnableWhenValidTicketIsSpecified() {
		RoxFilter rf = new RoxFilter(new String[]{"ticket:dummyTicket"});
		assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
	}

	@Test
	@RoxableTest(key = "fcd1200ff07d")
	public void descriptionShouldNotBeRunnableWhenInvalidTicketIsSpecified() {
		RoxFilter rf = new RoxFilter(new String[] {"name:noTicket"});
		assertFalse("The test is runnable when it should not", rf.isRunnable(description));
	}
	
	@Test
	@RoxableTest(key = "0f860a46a204")
	public void descriptionShouldBeRunnableWhenValidGenericFilterIsSpecified() {
		RoxFilter rf = new RoxFilter(new String[]{"dummyMethod"});
		assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
	}

	@Test
	@RoxableTest(key = "267c2c16d511")
	public void descriptionShouldNotBeRunnableWhenInvalidGenericFilterIsSpecified() {
		RoxFilter rf = new RoxFilter(new String[]{"noMethod"});
		assertFalse("The test is runnable when it should not", rf.isRunnable(description));
	}
}