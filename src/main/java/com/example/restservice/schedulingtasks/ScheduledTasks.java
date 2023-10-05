package com.example.restservice.schedulingtasks;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.example.restservice.config.LedgerService;
import com.example.restservice.models.DataBatchLedgerEntry;

@Component
public class ScheduledTasks {
	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	private final LedgerService service;
	private final StopWatch timer;

	ScheduledTasks(@Lazy LedgerService service) {
		this.service = service;
		this.timer = new StopWatch();
		timer.start("sqlite");
	}

	@Scheduled(fixedRate = 5000)
	public void reportCurrentStatus() {
		Long count = service.getCount();
		log.info("The count is now {}", count);
		if (count >= 20000000) {
			this.timer.stop();
			log.info("Total Recs: {} ", count);
			log.info("Last task time in Millis: {}", this.timer.getLastTaskTimeMillis());
			System.exit(0);
		}
	}

	@Scheduled(fixedRate = 20)
	public void writeRandom() {
		Random rand = new Random();
		for (int i = 0; i < 100; i++) {
			DataBatchLedgerEntry e = new DataBatchLedgerEntry();
			e.setDataType("random");
			e.setDeviceConfigUuid(UUID.randomUUID().toString());
			e.setStartTimestamp(rand.nextLong());
			e.setEndTimestamp(rand.nextLong());
			e.setLastSeenTimestamp(rand.nextLong());
			service.saveEntry(e);
		}
	}

	@Scheduled(fixedRate = 10)
	public void readRandom() {
		service.randomEntry();
	}

	@Scheduled(fixedDelayString = "${deriveddataprocessor.data-batch-pruning-rate-string:PT5M}")
	public void expireOldLedgerRows() {
		log.info("Starting task: Checking for rows to expire for clean up (limit: {}).",
				service.getDataBatchPruningPageSize());
		List<DataBatchLedgerEntry> oldRows;
		do {
			oldRows = service.getEntriesToExpire();
			log.info("Found {} rows to expire (limit per search: {}).",
					oldRows.size(), service.getDataBatchPruningPageSize());
			oldRows.forEach(r -> r.setCleanUp(true));
			service.saveEntries(oldRows);
		} while (oldRows.size() >= service.getDataBatchPruningPageSize());
		log.info("Task complete; no more rows to expire.");
	}

	@Scheduled(fixedDelayString = "${deriveddataprocessor.data-batch-pruning-rate-string:PT5M}", initialDelayString = "${deriveddataprocessor.data-batch-pruning-delete-delay-string:PT5S}")
	public void cleanUpLedger() {
		log.info("Starting task: Checking for rows to clean up (limit: {}).",
				service.getDataBatchPruningPageSize());
		List<DataBatchLedgerEntry> oldRows;
		do {
			oldRows = service.getEntriesToCleanUp();
			log.info("Found {} rows to remove during clean up (limit per search: {}).",
					oldRows.size(), service.getDataBatchPruningPageSize());
			service.deleteEntries(oldRows);
		} while (oldRows.size() >= service.getDataBatchPruningPageSize());
		log.info("Task complete; no more rows to clean up.");
	}

	@Scheduled(fixedDelayString = "${deriveddataprocessor.data-batch-polling-rate-string:PT5M}", initialDelayString = "${deriveddataprocessor.data-batch-polling-delay-string:PT1M}")
	public void pollForRows() {
		log.info("Starting task: Checking for new rows to process (limit: {}).",
				service.getDataBatchPollingPageSize());
		List<DataBatchLedgerEntry> rowsToProcess;
		final Pageable pageable = Pageable.ofSize(service.getDataBatchPollingPageSize());
		do {
			rowsToProcess = service.pollForEntriesToProcess(pageable);
			log.info("Found {} rows to process (limit per search: {}).",
					rowsToProcess.size(), service.getDataBatchPollingPageSize());
			// Note: Eventually could make this threaded with worker pool if we want
			// parallel batch processing,
			// but would need to move fetch and save into worker service
			if (!rowsToProcess.isEmpty()) {
				try {
					// ledgerBatchProcessor.processDataBatch(rowsToProcess);
					service.randomEntry();
				} catch (final Exception ex) {
					log.error("Exception processing rows.", ex);
				} finally {
					// Service is responsible for flagging individual rows for clean up / retry /
					// etc.
					log.info("Finished batch of {} entries; saving results.", rowsToProcess.size());
					service.saveEntries(rowsToProcess);
					pageable.next();
				}
			}
		} while (rowsToProcess.size() >= service.getDataBatchPollingPageSize());
		log.info("Task complete; no more rows to process.");
	}

}
