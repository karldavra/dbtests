package com.davra.ledgertests.schedulingtasks;

import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.davra.ledgertests.config.DataSourceConfig;
import com.davra.ledgertests.config.LedgerService;
import com.davra.ledgertests.models.DataBatchLedgerEntry;

@EnableAsync
@Component
public class ScheduledTasks {
	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	private final int delayStartMS = 10;
	private final int targetRows = 1000;
	private boolean done;

	private final LedgerService service;
	private final long startTime = System.currentTimeMillis();

	@Autowired
	private ApplicationContext ctx;

		@Autowired
		private DataSourceConfig ds;

	ScheduledTasks(@Lazy LedgerService service) {
		this.done = false;
		this.service = service;
	}

	// simple reads can be async...
	@Async
	@Scheduled(fixedRate = 2000, initialDelay = delayStartMS + 1)
	public void reportCurrentStatus() {
		Long count = this.service.getCount();
		int elapsedSeconds = Math.round((System.currentTimeMillis() - startTime) / 1000);
		this.done = count >= this.targetRows;
		log.info("Current Status:\nRow Count: " + count + "\nSeconds elapsed: " + elapsedSeconds + "\nDone? " + this.done);
		if (this.done) {
			log.info("...and now we're done!");
			SpringApplication.exit(ctx, () -> 0);
		}
	}

	@Async
	@Scheduled(fixedRate = 25, initialDelay = delayStartMS + 25)
	public void readRandom() {
		if (this.done) {
			return;
		}
		log.debug("Read Random");
		this.service.randomEntry();
	}

	// but writes/edits/deletes not so much...
	// @Async
	@Scheduled(fixedRate = 50, initialDelay = delayStartMS + 50)
	public void writeRandom() {
		if (this.done) {
			return;
		}
		log.debug("Write Random");
		Random rand = new Random();
		DataBatchLedgerEntry e = new DataBatchLedgerEntry();
		e.setDataType("random");
		e.setDeviceConfigUuid(UUID.randomUUID().toString());
		e.setStartTimestamp(rand.nextLong());
		e.setEndTimestamp(rand.nextLong());
		e.setLastSeenTimestamp(rand.nextLong());
		this.service.saveEntry(e);
	}

	// @Async
	@Scheduled(fixedRate = 50, initialDelay = delayStartMS + 75)
	public void updateRandom() {
		if (this.done) {
			return;
		}
		log.debug("Update Random");
		Random rand = new Random();
		DataBatchLedgerEntry e = this.service.randomEntry();
		e.setStartTimestamp(rand.nextLong());
		e.setEndTimestamp(rand.nextLong());
		e.setLastSeenTimestamp(rand.nextLong());
		this.service.saveEntry(e);
	}

	// @Async
	@Scheduled(fixedRate = 100, initialDelay = delayStartMS + 100)
	public void deletedRandom() {
		if (this.done) {
			return;
		}
		log.debug("Delete Random");
		DataBatchLedgerEntry e = this.service.randomEntry();
		this.service.deleteEntry(e);
	}

	// @Scheduled(fixedRate = 20000, initialDelay = delayStartMS + 1000)
	@Scheduled(fixedRate = 2000, initialDelay = 1)
	public void doBackUp() {
		Random rand = new Random();
		Long offset = rand.nextLong();
		ds.doBackUp(offset);
	// save backup state
	}

}
