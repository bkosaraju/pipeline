# Pipeline

Pipeline is a application framework to orchestrate and stitch multiple apps to run sequentially.

Framework provides sequential dependency,configuration sharing, restartability, sequence checking(order of execution) for applications   

pipeline rail multiple components which is tasks.

Task 
----
Task is basic running block of code 

1. running spark Apps   
    * Kubernetes (spark_on_kube)                                                                      
    * Databricks (spark_on_databricks)
    * EMR (spark_on_emr)
    * locally (spark)
    
2. SFTP (read/write data from SFTP servers)
3. S3Copy (copy data from S3)
4. S3Push (push local data to S3)
5. Arbitrary shell script execution

Task have configuration(task_config) which will be fed to application as Map[String,String]

more of these component documentation and sample configuration properties can be found at Utils library.

Any of the sensitive values can be fed to application with config_type to SSM which will be retrieved from AWS-Systems manager parameter store. 

Job
-----------------------
Job is collection of Tasks which will be stitched together to run the apps  

There is a relation between job and task (job_task_order) which describe how the tasks should be sequenced, this also capability to enable and disable some of the tasks. 

job also have configuration(job_config) which will be applied to all tasks for the job.

this validates whether the previous instance is success or not in case if failure it holds of current run(of course you can overwrite) to avid history data issue.
 
Global Configuration
--------------------
Applied to all the jobs in a framework (common configurations)

precedence for configuration wold be task(highest) --> job --> glabal which gives flexibility to override a global config at task level.

Application logging
-------------------
All the log metadata captured at meta_audit store where each execution job(job_execution),task(task_execution) audits get loaded(start time, end time, end status).

App Logs 
--------
when application push to target system (kubernetes/databricks,EMR) it hangs on to it and get back logs.

Application Properties
----------------------
Basic starting application properties to start.

any of the sensitive values can be upload into SSM and refer with `secret.` where application try to retrieve from amazon systems manager parameter store.

```properties
jdbcHostname=<>
jdbcPort=5432
jdbcDatabase=<>
jdbcUsername=<>
secret.jdbcPassword=<SSM location>
#jdbcPassword=<not a good way to store locally>

```
any of these parameters specified here have equivalent precedence same as global config.

## Usage

```bash
Usage: pipeline [-ehrV] -d=<orderDateTime> -j=<jobId> -p=<properties>
Pipeline to run user applications in sequence example:
  -d, --ordertimestamp=<orderDateTime>
                        Job Order date must be specified in YYYY-mm-dd or
                          YYYY-mm-DDTHH:MM:SS[Z]
  -e, --endjobstatusandrun and run
                        Set the previous job execution status to END and run the job
  -h, --help            Show this help message and exit.
  -j, --jobid=<jobId>   jobId to be executed
  -p, --properties=<properties>
                        properties for input application
  -r, --restartjobstatusandrun and rerun
                        Set the previous job execution status to RERUN and run the
                          job
  -V, --version         Print version information and exit.

#ex:
bin/pipeline  -j <job_id> -d <job_order_timestamp> -p <app_properties> -r

Alternatively fat jar can be used to Run the same 

java -jar piplien_2.12_<version>-all.jar  -j <job_id> -d <job_order_timestamp> -p <app_properties> -r

```

Where can I get the latest release?
-----------------------------------
You can get source from [SCM](https://github.com/bkosaraju/pipeline).

Alternatively you can pull binaries from the central Maven repositories:
For mvn: 
```xml
<dependency>
  <groupId>io.github.bkosaraju</groupId>
  <artifactId>pipeline_#ScalaVariant#</artifactId>
  <version>#Version#</version>
</dependency>
 
<!--Fat/ Assembly Jar-->
<dependency>
  <groupId>io.github.bkosaraju</groupId>
  <artifactId>pipeliene_#ScalaVariant#</artifactId>
  <version>#verion#</version>
  <classifier>all</classifier>
</dependency>

```
for Gradle: 

```groovy
    api group: "io.github.bkosaraju", name: "pipeline_$scalaVariant", version: "$Version"
```

## Build Instructions 

```bash
./gradlew clean build

#Artifacts can be found in build/lib directory 

#Linux/Windows binaries can be found at build/distribution directory 
```

## Scala [Docs](https://bkosaraju.github.io/pipeline)

## Contributing
Please feel free to raise a pull request in case if you feel like something can be updated or contributed

## License
[Apache](http://www.apache.org/licenses/LICENSE-2.0.txt)