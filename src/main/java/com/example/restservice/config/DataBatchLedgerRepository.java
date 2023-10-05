package com.example.restservice.config;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.restservice.models.DataBatchLedgerEntry;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface DataBatchLedgerRepository
    extends JpaRepository<DataBatchLedgerEntry, BigInteger>, JpaSpecificationExecutor<DataBatchLedgerEntry> {

  DataBatchLedgerEntry findFirstByDeviceConfigUuidAndComponentSerialNumberAndDataTypeAndLastSeenTimestampGreaterThanOrderByLastSeenTimestampDesc(
      final String deviceConfigUuid,
      final String componentSerialNumber,
      final String dataType,
      final long lastSeenTimestamp);

  List<DataBatchLedgerEntry> findByCleanUpAndErrorCountLessThanAndLastSeenTimestampLessThanAndLastSeenTimestampGreaterThanOrderByLastSeenTimestampAsc(
      final boolean cleanUp,
      final int errorCount,
      final long lastSeenTimestampStart,
      final long lastSeenTimestampEnd,
      final Pageable pageable);

  List<DataBatchLedgerEntry> findByCleanUpAndLastSeenTimestampLessThanOrderByLastSeenTimestampAsc(
      final boolean cleanUp,
      final long lastSeenTimestamp,
      final Pageable pageable);

  List<DataBatchLedgerEntry> findByCleanUpOrderByLastSeenTimestampAsc(
      final boolean cleanUp,
      final Pageable pageable);

  @Modifying
  @Transactional
  @Query(value = "DELETE FROM data_batch_ledger_entry", nativeQuery = true)
  void truncateTable();

  @Query(value = "SELECT * FROM data_batch_ledger_entry WHERE id > (ABS(RANDOM()) % (SELECT max(id) FROM data_batch_ledger_entry)) LIMIT 1;", nativeQuery = true)
  DataBatchLedgerEntry randomEntry();


}
