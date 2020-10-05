insert
    into
    meta_audit.job_execution(
    job_id
    ,job_execution_status
    ,job_order_timestamp
    )
    values ( :jobId, :jobExecutionStatus ,:jobOrderTimestamp ) returning  job_execution_id
