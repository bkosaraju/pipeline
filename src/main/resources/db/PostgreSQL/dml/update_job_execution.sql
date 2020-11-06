update
    job_execution
    set job_execution_end_timestamp = :jobExecutionEndTimestamp,
    job_execution_status = :jobExecutionStatus
 where id =  :jobExecutionId