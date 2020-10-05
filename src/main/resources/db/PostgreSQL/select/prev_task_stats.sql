 SELECT
     max(task_execution_id) over (partition BY job_order_timestamp,task_execution_timestamp ORDER BY job_order_timestamp desc,task_execution_timestamp desc ) AS previousTaskExecutionId
     ,task_execution_timestamp previousTaskExecutionTimestamp
     ,job_order_timestamp previousJobOrderTimestamp
     ,job_execution_id previousJobExecutionId
 FROM meta_audit.task_execution
 WHERE task_id=:taskId
 AND task_execution_status='END'
 LIMIT 1
