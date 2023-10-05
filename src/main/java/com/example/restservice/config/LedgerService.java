package com.example.restservice.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.restservice.models.DataBatchLedgerEntry;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class LedgerService {
  private final DataBatchLedgerRepository repository;
  @Value("${deriveddataprocessor.data-batch-latency-millis:60000}")
  @Setter
  private Integer dataBatchLatencyMillis;
  @Value("${deriveddataprocessor.data-batch-lifespan-millis:86400000}")
  @Setter
  private Integer dataBatchLifespanMillis;
  @Value("${deriveddataprocessor.data-batch-pruning-page-size:1000}")
  @Setter
  @Getter
  private Integer dataBatchPruningPageSize;
  @Value("${deriveddataprocessor.data-batch-polling-page-size:1000}")
  @Setter
  @Getter
  private Integer dataBatchPollingPageSize;
  @Value("${deriveddataprocessor.data-batch-polling-allowance-millis:5000}")
  @Setter
  @Getter
  private Integer dataBatchPollingAllowanceMillis;
  @Value("${deriveddataprocessor.data-batch-max-error-count:100}")
  @Setter
  @Getter
  private Integer dataBatchMaxErrorCount;

  LedgerService(final DataBatchLedgerRepository repository) {
    this.repository = repository;
  }

  public DataBatchLedgerEntry getCurrentLedgerEntry(final String deviceConfigUuid, final String componentSerialNumber) {
    final long startTimestamp = System.currentTimeMillis() - dataBatchLatencyMillis;
    log.debug(
        "Finding most recent ledger entry last seen after timestamp {} ({}) for deviceConfigUuid {}, componentSerialNumber {}, dataType {}.",
        startTimestamp, Instant.ofEpochMilli(startTimestamp), deviceConfigUuid, componentSerialNumber);
    return repository
        .findFirstByDeviceConfigUuidAndComponentSerialNumberAndDataTypeAndLastSeenTimestampGreaterThanOrderByLastSeenTimestampDesc(
            deviceConfigUuid, componentSerialNumber, "random", startTimestamp);
  }

  public DataBatchLedgerEntry saveEntry(final DataBatchLedgerEntry entry) {
    return repository.save(entry);
  }

  public List<DataBatchLedgerEntry> saveEntries(final List<DataBatchLedgerEntry> entries) {
    return repository.saveAll(entries);
  }

  public void deleteEntries(final List<DataBatchLedgerEntry> entries) {
    repository.deleteAll(entries);
  }

  public List<DataBatchLedgerEntry> pollForEntriesToProcess(final Pageable pageable) {
    final long startTimestamp = System.currentTimeMillis() - dataBatchLifespanMillis + dataBatchPollingAllowanceMillis;
    final long endTimestamp = System.currentTimeMillis() - dataBatchLatencyMillis - dataBatchPollingAllowanceMillis;
    log.debug(
        "Finding ledger entries last seen after timestamp {} ({}) and last seen before timestamp {} ({}) and ready to process.",
        startTimestamp, Instant.ofEpochMilli(startTimestamp), endTimestamp, Instant.ofEpochMilli(endTimestamp));
    return repository
        .findByCleanUpAndErrorCountLessThanAndLastSeenTimestampLessThanAndLastSeenTimestampGreaterThanOrderByLastSeenTimestampAsc(
            false,
            dataBatchMaxErrorCount,
            endTimestamp,
            startTimestamp,
            pageable);
  }

  public List<DataBatchLedgerEntry> getEntriesToExpire() {
    final long startTimestamp = System.currentTimeMillis() - dataBatchLifespanMillis;
    log.debug("Finding ledger entries last seen before timestamp {} ({}) and ready to expire.",
        startTimestamp, Instant.ofEpochMilli(startTimestamp));
    return repository.findByCleanUpAndLastSeenTimestampLessThanOrderByLastSeenTimestampAsc(
        false,
        startTimestamp,
        Pageable.ofSize(dataBatchPruningPageSize));
  }

  public List<DataBatchLedgerEntry> getEntriesToCleanUp() {
    log.debug("Finding ledger entries flagged ready for cleanup.");
    return repository.findByCleanUpOrderByLastSeenTimestampAsc(
        true,
        Pageable.ofSize(dataBatchPruningPageSize));
  }

  public void truncateTable() {
    log.warn("Clearing table.");
    repository.truncateTable();
  }

  public Long getCount() {
    return repository.count();
  }

  public DataBatchLedgerEntry randomEntry() {
    return repository.randomEntry();
  }

}
