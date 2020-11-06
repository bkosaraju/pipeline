select
    jbs.id jobId
    ,jbs.job_name jobName
        ,tsks.id taskId
    ,tsks.task_name taskName
    ,tsks.task_type taskType
    ,jp.task_seq_id taskSeqId
from job_task_order jp
inner join job jbs
on
    jbs.id = jp.job_id
    and jbs.job_status_flag = true
inner join
task tsks
on
    tsks.id = jp.task_id
LEFT OUTER JOIN
	( SELECT pe.task_id
	  ,MAX(pe.id) over (partition BY pe.task_id ORDER BY pe.task_execution_start_timestamp desc)
	 from task_execution pe
	 WHERE pe.id = cast(:jobExecutionId as bigint)
	 AND pe.task_execution_status='END' ) jpe
	 on
	 jpe.task_id = jp.task_id
WHERE
	 jpe.task_id IS NULL  AND
    jp.job_task_status_flag = 1 AND
	 jbs.job_id = cast(:jobId as int)
order by taskSeqId