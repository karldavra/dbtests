package com.davra.ledgertests.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;

@Entity
@Table(indexes = @Index(name = "ledger_input_stream_index", columnList = "deviceConfigUuid, componentSerialNumber, dataType"))
@Data
public class DataBatchLedgerEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "integer not null primary key autoincrement")
  private BigInteger id;
  private String deviceUuid;
  private String deviceConfigUuid;
  private String componentSerialNumber;
  private String dataType;
  private long startTimestamp;
  private long endTimestamp;
  private long lastSeenTimestamp;
  private boolean cleanUp;
  private int errorCount;
  @Transient
  private boolean dataReadyForQuerying;
  @Transient
  private boolean errorProcessing;
}