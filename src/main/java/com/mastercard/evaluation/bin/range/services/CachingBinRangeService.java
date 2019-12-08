package com.mastercard.evaluation.bin.range.services;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import com.mastercard.evaluation.bin.range.events.EventManager;
import com.mastercard.evaluation.bin.range.models.BinRange;
import com.mastercard.evaluation.bin.range.models.BinRangeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CachingBinRangeService implements BinRangeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CachingBinRangeService.class);

	private static final int ONE_HOUR_IN_MILLIS = 3600000;
	private static final String BIN_TABLE_RESOURCE_FILE_NAME = "bin-range-info-data.json";

	private final Lock lock = new ReentrantLock();
	private final ObjectMapper objectMapper;
	private final EventManager eventManager;

	private HashMap<UUID, BinRangeInfo> binRangeInfoCache = new HashMap<>();
	private NavigableMap<BinRange, UUID> binRangeInfoByBinRangeIndex = new TreeMap<>();

	@Autowired
	public CachingBinRangeService(ObjectMapper objectMapper, EventManager eventManager) {
		Preconditions.checkNotNull(objectMapper, "ObjectMapper cannot be null ");
		Preconditions.checkNotNull(eventManager, "EventManager cannot be null");

		this.objectMapper = objectMapper;
		this.eventManager = eventManager;
	}

	@PostConstruct
	@Scheduled(fixedRate = ONE_HOUR_IN_MILLIS)
	@SuppressWarnings("unchecked")
	public void refreshCache() {
		LOGGER.info("Refreshing cache");

		try {
			URL url = Resources.getResource(BIN_TABLE_RESOURCE_FILE_NAME);
			JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, BinRangeInfo.class);
			List<BinRangeInfo> binTableEntries = objectMapper.readValue(Resources.toString(url, Charsets.UTF_8), type);
			populateCache(binTableEntries);
		} catch (IOException e) {
			LOGGER.error("Failed to read bin range entries from file={}", BIN_TABLE_RESOURCE_FILE_NAME, e);
		}
	}

	@Override
	public Optional<BinRangeInfo> findBinRangeInfoByPan(String pan) {
		lock.lock();
		try {
			Optional<UUID> binRangeInfo = Optional.ofNullable(binRangeInfoByBinRangeIndex.get(new BinRange(pan)));
			return binRangeInfo.isPresent() ? Optional.ofNullable(binRangeInfoCache.get(binRangeInfo.get()))
					: Optional.empty();

		} finally {
			lock.unlock();
		}

	}

	@Override
	public Optional<BinRangeInfo> findBinRangeInfoCacheByRef(UUID ref) {
		lock.lock();
		try {
			Optional<BinRangeInfo> binRangeInfo = Optional.ofNullable(binRangeInfoCache.get(ref));
			return binRangeInfo.isPresent() ? Optional.ofNullable((binRangeInfo.get())) : Optional.empty();
		} finally {
			lock.unlock();
		}
	}

	@VisibleForTesting
	void populateCache(List<BinRangeInfo> binTableEntries) {
		lock.lock();

		try {
			binTableEntries.forEach(this::populateCacheAndIndices);
		} finally {
			lock.unlock();
		}
	}

	public HashMap<UUID, BinRangeInfo> getBinRangeInfosFromCache() {
		return binRangeInfoCache;
	}

	public void removeCacheAndIndices(BinRangeInfo entry) {
		binRangeInfoCache.remove(entry.getRef());
		binRangeInfoByBinRangeIndex.remove(getBinRangeFromEntry(entry), entry.getRef());
	}

	public void populateCacheAndIndices(BinRangeInfo entry) {
		Optional<BinRangeInfo> binRangeInfo = Optional.ofNullable(binRangeInfoCache.get(entry.getRef()));
		if (binRangeInfo.isPresent()) {
			boolean isHashcodeEquals = binRangeInfo.get().hashCode() == entry.hashCode();
			if (isHashcodeEquals) {
				binRangeInfoCache.put(entry.getRef(), entry);
				binRangeInfoByBinRangeIndex.put(getBinRangeFromEntry(entry), entry.getRef());

			} /*else {
				BinRange binRange = getBinRangeFromEntry(binRangeInfo.get());
				BinRangeInfo binRangeInfoObj = binRangeInfo.get();
				binRangeInfoCache.remove(entry.getRef());
				binRangeInfoByBinRangeIndex.remove(getBinRangeFromEntry(binRangeInfo.get()), entry.getRef());

			}*/
		} else {
			binRangeInfoCache.put(entry.getRef(), entry);
			binRangeInfoByBinRangeIndex.put(getBinRangeFromEntry(entry), entry.getRef());
		}

	}

	private BinRange getBinRangeFromEntry(BinRangeInfo binRangeInfo) {
		return new BinRange(binRangeInfo.getStart(), binRangeInfo.getEnd());
	}

}
