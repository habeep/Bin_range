package com.mastercard.evaluation.bin.range.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mastercard.evaluation.bin.range.models.BinRangeInfo;
import com.mastercard.evaluation.bin.range.services.BinRangeService;
import com.mastercard.evaluation.bin.range.services.CachingBinRangeService;

@RestController
@RequestMapping(value = "/binRangeInfoCache")
public class BinRangeInfoCacheController {

	
	private final BinRangeService binRangeService;

	@Autowired
	private CachingBinRangeService cachingBinRangeService;

	@Autowired
	public BinRangeInfoCacheController(BinRangeService binRangeService) {
		this.binRangeService = binRangeService;
	}

	@RequestMapping(value = "/get", method = GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<UUID, BinRangeInfo>> getAllBinRangeInfosFromCache() {
		HashMap<UUID, BinRangeInfo> binRangeCacheInfos = cachingBinRangeService.getBinRangeInfosFromCache();
		return new ResponseEntity<HashMap<UUID, BinRangeInfo>>(binRangeCacheInfos, HttpStatus.OK);
	}

	@RequestMapping(value = { "/create" }, method = RequestMethod.POST)
	public ResponseEntity<BinRangeInfo> addBinRangeInfo(@RequestBody BinRangeInfo binRangeInfo) {
		
		if (binRangeService.findBinRangeInfoCacheByRef(binRangeInfo.getRef()).isPresent()) {
			return new ResponseEntity<>(binRangeInfo, HttpStatus.CONFLICT);
		} else {
			List<BinRangeInfo> binRangeInfoList = new ArrayList<BinRangeInfo>();
			binRangeInfoList.add(binRangeInfo);
			cachingBinRangeService.populateCacheAndIndices(binRangeInfo);
			return new ResponseEntity<>(binRangeInfo, HttpStatus.CREATED);
		}
	}

	@RequestMapping(value = { "/update" }, method = RequestMethod.PUT)
	public ResponseEntity<BinRangeInfo> updateBinRangeInfo(@RequestBody BinRangeInfo binRangeInfo) {
		if (binRangeService.findBinRangeInfoCacheByRef(binRangeInfo.getRef()).isPresent()) {
			List<BinRangeInfo> binRangeInfoList = new ArrayList<BinRangeInfo>();
			binRangeInfoList.add(binRangeInfo);
			cachingBinRangeService.removeCacheAndIndices(binRangeInfo);
			cachingBinRangeService.populateCacheAndIndices(binRangeInfo);
			return new ResponseEntity<>(binRangeInfo, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(binRangeInfo, HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = { "/delete/{ref}" }, method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteBinRangeInfo(@PathVariable("ref") UUID ref) {
		Optional<BinRangeInfo> binRangeInfo = binRangeService.findBinRangeInfoCacheByRef(ref);
		if (binRangeInfo.isPresent()) {
			cachingBinRangeService.removeCacheAndIndices(binRangeInfo.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

}
