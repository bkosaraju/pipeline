insert into
    meta_audit.task_execution
    (task_id
    ,job_execution_id
    ,task_execution_status
     ,job_order_timestamp)
    values ( :taskId, :jobExecutionId, :taskExecutionStatus, :jobOrderTimestamp )  returning  task_execution_id