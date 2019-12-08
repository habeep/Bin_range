package com.mastercard.evaluation.bin.range.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercard.evaluation.bin.range.events.EventManager;
import com.mastercard.evaluation.bin.range.models.BinRangeInfo;
import com.mastercard.evaluation.bin.range.services.BinRangeService;
import com.mastercard.evaluation.bin.range.services.CachingBinRangeService;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.runners.MethodSorters;


@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BinRangeInfoCacheControllerTest extends BaseControllerTest {

	//private BinRangeService binRangeService = mock(BinRangeService.class);

	private final ObjectMapper objectMapper = spy(new ObjectMapper());
    private final EventManager eventManager = mock(EventManager.class);

    private CachingBinRangeService cachingBinRangeService;
	

	@Autowired
	private BinRangeInfoCacheController binRangeInfoCacheController;

	@Before
	public void setUp() {
        cachingBinRangeService = new CachingBinRangeService(objectMapper, eventManager);
        cachingBinRangeService.refreshCache();
		//ReflectionTestUtils.setField(binRangeInfoCacheController, "binRangeService", binRangeService);
	}

	@Test
	public void test00_create_shouldReturn201StatusCode_withValidBinRangeInfo() throws Exception {
		BinRangeInfo binRangeInfo = new BinRangeInfo();
		binRangeInfo.setBankName("UlsterBank");
		binRangeInfo.setCurrencyCode("EUR");
		binRangeInfo.setStart(new BigDecimal("4263000000000001"));
		binRangeInfo.setEnd(new BigDecimal("4263999999999999"));
		binRangeInfo.setRef(UUID.fromString("2a480c8a-83ca-4bb7-95b7-f19cec97b3f6"));
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/binRangeInfoCache/create")
				.contentType(MediaType.APPLICATION_JSON).content(asJsonString(binRangeInfo));

		MockHttpServletResponse response = getMockMvc().perform(builder).andReturn().getResponse();

		assertEquals(HttpStatus.CREATED.value(), response.getStatus());

		BinRangeInfo binRangeInfoResponse = objectMapper.readValue(response.getContentAsString(), BinRangeInfo.class);

		assertNotNull(binRangeInfoResponse);
		assertEquals(new BigDecimal("4263000000000001"), binRangeInfoResponse.getStart());
		assertEquals(new BigDecimal("4263999999999999"), binRangeInfoResponse.getEnd());
		assertEquals("UlsterBank", binRangeInfoResponse.getBankName());
		assertEquals("EUR", binRangeInfoResponse.getCurrencyCode());
	}
	

	@Test
	public void test01_update_shouldReturn200StatusCode_withValidBinRangeInfo() throws Exception {
		BinRangeInfo binRangeInfo = new BinRangeInfo();
		binRangeInfo.setBankName("AIB123");
		binRangeInfo.setCurrencyCode("EUR123");
		binRangeInfo.setStart(new BigDecimal("4263000000000000"));
		binRangeInfo.setEnd(new BigDecimal("4263999999999999"));
		binRangeInfo.setRef(UUID.fromString("2a480c8a-83ca-4bb7-95b7-f19cec97b3f6"));
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/binRangeInfoCache/update")
				.contentType(MediaType.APPLICATION_JSON).content(asJsonString(binRangeInfo));

		MockHttpServletResponse response = getMockMvc().perform(builder).andReturn().getResponse();

		assertEquals(HttpStatus.OK.value(), response.getStatus());

		BinRangeInfo binRangeInfoResponse = objectMapper.readValue(response.getContentAsString(), BinRangeInfo.class);

		assertNotNull(binRangeInfoResponse);
	}

	@Test
	public void test02_delete_shouldReturn404StatusCode_withValidBinRangeInfo() throws Exception {
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete("/binRangeInfoCache/delete/{ref}","2a480c8a-83ca-4bb7-95b7-f19cec97b3fd");

		MockHttpServletResponse response = getMockMvc().perform(builder).andReturn().getResponse();
		assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());

	}

	@Test
	public void test03_delete_shouldReturn204StatusCode_whenNotValidBinRangeInfo() throws Exception {
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete("/binRangeInfoCache/delete/{ref}","D893D80C-E7CF-4BA7-9F26-AE289D29E136");

		MockHttpServletResponse response = getMockMvc().perform(builder).andReturn().getResponse();
		assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());

	}

	@Test
	public void test04_get_shouldReturn200StatusCode() throws Exception {
		BinRangeInfo binRangeInfo = new BinRangeInfo();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/binRangeInfoCache/get");
		MockHttpServletResponse response = getMockMvc().perform(builder).andReturn().getResponse();
		String content = response.getContentAsString();
		assertEquals(HttpStatus.OK.value(), response.getStatus());

	}
	
	@Test
	public void test05_update_shouldReturn404StatusCode_whenNoValidRef() throws Exception {
		BinRangeInfo binRangeInfo = new BinRangeInfo();
		binRangeInfo.setBankName("AIB123");
		binRangeInfo.setCurrencyCode("EUR123");
		binRangeInfo.setStart(new BigDecimal("4263000000000000"));
		binRangeInfo.setEnd(new BigDecimal("4263999999999999"));
		binRangeInfo.setRef(UUID.fromString("2a480c8a-83ca-4bb7-95b7-f19cec97b3f7"));
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/binRangeInfoCache/update")
				.contentType(MediaType.APPLICATION_JSON).content(asJsonString(binRangeInfo));

		MockHttpServletResponse response = getMockMvc().perform(builder).andReturn().getResponse();

		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

	}


	public static String asJsonString(final Object obj) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final String jsonContent = mapper.writeValueAsString(obj);
			return jsonContent;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}