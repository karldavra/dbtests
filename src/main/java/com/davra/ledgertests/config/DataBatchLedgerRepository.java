package com.davra.ledgertests.config;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.davra.ledgertests.models.DataBatchLedgerEntry;

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
    @Query(value = "DELETE FROM DataBatchLedgerEntry", nativeQuery = true)
    void truncateTable();

    @Transactional
    @Query(value = "SELECT * FROM DataBatchLedgerEntry WHERE id > (ABS(RANDOM()) % (SELECT max(id) FROM DataBatchLedgerEntry)) LIMIT 1;", nativeQuery = true)
    DataBatchLedgerEntry randomEntry();

//     @Modifying
//     @Transactional
//     @Query(value = "CREATE TABLE IF NOT EXISTS DataBatchLedgerEntry (cleanUp boolean not null, errorCount integer not null, id integer, endTimestamp bigint not null, lastSeenTimestamp bigint not null, startTimestamp bigint not null, componentSerialNumber varchar(255), dataType varchar(255), deviceConfigUuid varchar(255), deviceUuid varchar(255), primary key (id))", nativeQuery = true)
//     void createTableIfNotExists();

//     // @Transactional
//     @Modifying
//     @Query(value = "VACUUM", nativeQuery = true)
//     void vacuum();

}
