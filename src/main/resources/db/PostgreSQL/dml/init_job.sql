insert
    into
    job_execution
    (
    job_id
    ,job_execution_status
    ,job_order_timestamp
    ,job_execution_start_timestamp
    ,job_execution_end_timestamp
    )
    values ( :jobId, :jobExecutionStatus, :jobOrderTimestamp, :jobExecutionStartTimestamp, :jobExecutionEndTimestamp ) returning id
