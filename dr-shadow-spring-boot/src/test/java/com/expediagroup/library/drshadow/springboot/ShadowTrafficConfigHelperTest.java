package com.expediagroup.library.drshadow.springboot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static junit.framework.TestCase.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class ShadowTrafficConfigHelperTest {

	private ShadowTrafficConfigHelper shadowTrafficConfigHelper;

	@Mock
	private ShadowTrafficConfig shadowTrafficConfig;

	@Before
	public void setup() {

		shadowTrafficConfigHelper = new ShadowTrafficConfigHelper(shadowTrafficConfig);
	}

	@Test
	public void testGetConfigReturnsValue_HappyPath() {
		// arrange

		// act
		ShadowTrafficConfig config = shadowTrafficConfigHelper.getConfig();
		
		// assert
		assertNotNull(config);
	}
	
}
