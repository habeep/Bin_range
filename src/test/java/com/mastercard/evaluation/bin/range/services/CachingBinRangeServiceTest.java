package com.mastercard.evaluation.bin.range.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.mastercard.evaluation.bin.range.events.EventManager;
import com.mastercard.evaluation.bin.range.models.BinRangeInfo;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class CachingBinRangeServiceTest {

	private final ObjectMapper objectMapper = spy(new ObjectMapper());
	private final EventManager eventManager = mock(EventManager.class);

	private CachingBinRangeService cachingBinRangeService;

	@Before
	public void setup() {
		cachingBinRangeService = new CachingBinRangeService(objectMapper, eventManager);

		cachingBinRangeService.refreshCache();
	}

	@Test
	public void shouldFindTheCorrectRangeForAGivenPan() {
		String panWithinSomeTestBankRange = "4263123412341234";

		Optional<BinRangeInfo> binRangeInfoOptional = cachingBinRangeService
				.findBinRangeInfoByPan(panWithinSomeTestBankRange);

		assertTrue(binRangeInfoOptional.isPresent());
		assertNotNull(binRangeInfoOptional.get());
		assertThat(binRangeInfoOptional.get().getRef(), is(UUID.fromString("2A480C8A-83CA-4BB7-95B7-F19CEC97B3FD")));
		assertThat(binRangeInfoOptional.get().getStart(), is(new BigDecimal("4263000000000000")));
		assertThat(binRangeInfoOptional.get().getEnd(), is(new BigDecimal("4263999999999999")));
		assertThat(binRangeInfoOptional.get().getBankName(), is("AIB"));
		assertThat(binRangeInfoOptional.get().getCurrencyCode(), is("EUR"));
	}

	@Test
	public void shouldFailToFindTheCorrectRangeForANonExistentPan() {
		String panWithinSomeTestBankRange = "6263123412341234";

		Optional<BinRangeInfo> binRangeInfoOptional = cachingBinRangeService
				.findBinRangeInfoByPan(panWithinSomeTestBankRange);

		assertFalse(binRangeInfoOptional.isPresent());
	}

	@Test
	@Ignore("Problem 1 - The CachingBinRangeService should be updated to maintain a valid cache, where a valid cache reflects the latest source of truth")
	public void shouldMaintainAValidCache() {
		String testBankOnePan = "4263000000000001";
		String testBankTwoPan = "4319000000000001";
		String testBankThreePan = "5432000000000001";

		assertTrue(cachingBinRangeService.findBinRangeInfoByPan(testBankOnePan).isPresent());
		assertTrue(cachingBinRangeService.findBinRangeInfoByPan(testBankTwoPan).isPresent());
		assertTrue(cachingBinRangeService.findBinRangeInfoByPan(testBankThreePan).isPresent());

		String testBankFourPan = "5263000000000001";

		cachingBinRangeService.populateCache(getLatestBinRangeInfo());

		assertTrue(cachingBinRangeService.findBinRangeInfoByPan(testBankOnePan).isPresent());
		assertTrue(cachingBinRangeService.findBinRangeInfoByPan(testBankFourPan).isPresent());
		assertFalse(cachingBinRangeService.findBinRangeInfoByPan(testBankTwoPan).isPresent());
		assertFalse(cachingBinRangeService.findBinRangeInfoByPan(testBankThreePan).isPresent());
	}

	private List<BinRangeInfo> getLatestBinRangeInfo() {
		Faker faker = new Faker();

		BinRangeInfo updatedBinRangeInfo = new BinRangeInfo();
		updatedBinRangeInfo.setRef(UUID.fromString("2A480C8A-83CA-4BB7-95B7-F19CEC97B3FD"));
		updatedBinRangeInfo.setStart(new BigDecimal("4263000000000000"));
		updatedBinRangeInfo.setEnd(new BigDecimal("4263999999999999"));
		updatedBinRangeInfo.setBankName(faker.company().name());
		updatedBinRangeInfo.setCurrencyCode(faker.currency().code());

		BinRangeInfo newBinRangeInfo = new BinRangeInfo();
		newBinRangeInfo.setRef(UUID.fromString("9651794E-8166-423F-8B8D-EE235A04DDB7"));
		newBinRangeInfo.setStart(new BigDecimal("5263000000000000"));
		newBinRangeInfo.setEnd(new BigDecimal("5263999999999999"));
		newBinRangeInfo.setBankName(faker.company().name());
		newBinRangeInfo.setCurrencyCode(faker.currency().code());

		return Lists.newArrayList(updatedBinRangeInfo, newBinRangeInfo);
	}
}