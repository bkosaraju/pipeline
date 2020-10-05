insert into
    meta_audit.job_execution(
    job_execution_id
    ,job_id
    ,job_execution_status
    ,job_order_timestamp
    )
    values ( :jobExecutionId, :jobId, :jobExecutionStatus, :jobOrderTimestamp)
