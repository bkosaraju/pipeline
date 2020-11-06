insert
    into
    task_execution
    (
    task_id
    ,job_execution_id
    ,task_execution_status
    ,job_order_timestamp
    ,task_execution_start_timestamp
    ,task_execution_end_timestamp
    )
    values ( :taskId, :jobExecutionId, :taskExecutionStatus, :jobOrderTimestamp, :taskExecutionStartTimestamp, :taskExecutionEndTimestamp ) returning id