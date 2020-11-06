update
    task_execution
    set task_execution_end_timestamp = :taskExecutionEndTimestamp,
    task_execution_status = :taskExecutionStatus
 where id =  :taskExecutionId