# 스프링 배치 개발 가이드
스프링 배치에는 데이터 유효성 검증, 출력 포맷, 복잡한 비즈니스 규칙을 재사용 할 수 있는 방식으로 구현하는 기능, 대규모 데이터셋 처리 기능을 포함하고 있다. 본 가이드에서는 배치 표준 코드 템플릿을 기술하고 DevPilot에서 제공하는 일반 스프링 배치와 온라인 배치 코드의 표준 개발 방법에 대해 설명한다.

**Table of Contents**
- [배치 애플리케이션 구조](#배치-애플리케이션-구조)
- [Job과 Step](#Job과-Step)
- [표준 템플릿](#표준-템플릿)
- [템플릿 소스 이름 규칙](#템플릿-소스-이름-규칙)
- [템플릿 소스 구조](#템플릿-소스-구조)
- [템플릿 소스 상세](#템플릿-소스-상세)
- [추가 사항](#추가-사항)

## 배치 애플리케이션 구조
스프링 배치 애플리케이션은 3개의 tier로 구성된 계층 구조로 이루어져 있다. Application layer는 배치 처리 구축에 사용되는 모든 사용자 코드를 포함한다. 즉, 업무와 연관된 business logic이 구현되는 부분이다. Application layer는 core layer와 infrastructure layer를 감싸고 있다. core layer는 job과 step inteface와 jobLauncher 등을 포함한다. Infrastructure layer는 파일 또는 데이터를 읽고 쓸 수 있는 reader와 writer등을 가지고 있다.

## Job과 Step
Job은 1개 이상의 step으로 구성될 수 있다. Step은 job 상태를 나타내는 state machine에 불과하다. 즉, 각각의 step은 job을 구성하는 독립된 작업의 단위이다. Step은 tasklet기반의 step과 Chunk기반의 step으로 크게 두 가지 유형으로 구분된다. Tasklet 기반의 step은 execute 메소드를 overriding하면 되며 chunk 기반의 step은 itemReader, itemWriter 및 itemProcessor를 각각 구현해야 된다. 여기서 서비스 특성 상 itemProcessor가 필요 없는 경우가 있다. 하지만, itemReader와 itemWriter는 필수적으로 구현되어야 한다.

잡이 실행될 때 스프링 배치의 많은 컴포넌트는 서로 상호 작용을 한다. JobRepository 컴포넌트는 다양한 배치 수행과 관련된 시작 시간, 종료 시간, 종료 상태, 읽기/쓰기 횟수 등의 수치 데이터를 jobExecution 또는 stepExecution에 업데이트 한다. JobLauncher는 내부적으로 job.execute 메서드를 호출하여 잡을 실행하는 역할을 담당한다.

잡을 실행하면 해당 잡에 속해 있는 각 스텝을 실행한다. 스텝이 실행될 때 실행된 스텝, 읽은 아이템 및 처리된 아이템 수 등의 스텝 정보가 jobRepository에 저장된다. 또한, 스텝이 각 chunk를 처리할 때 마다 현재 까지의 커밋 수, 시작 및 종료 시간 등의 스텝 상태가 jobRepository내의 stepExecution에 업데이트 된다. 따라서 잡과 스텝 실행이 완료되면 jobRepository내의 jobExecution과 stepExecution이 최종 상태로 업데이트 된다.

## 표준 템플릿
DevPilot에서 생성하는 배치 템플릿 기본구조는 하나의 프로젝트에 1개 이상의 잡과 1개 이상의 스텝을 포함할 수 있다. DevPilot에서 다중 스텝은 직렬 스텝 구조만 지원한다. 즉 , 병렬 스텝 구조를 구성하기 위해서는 DevPilot을 통해 기본 소스 코드를 생성한 후에 개발자가 직접 코드 수정 작업을 해야 한다. DevPilot에서 프로젝트 생성 시점에 프로젝트 유형으로 ```Batch``` 혹은 ```Online/batch```를 선택할 수 있으며 선택된 프로젝트 유형에 따라 dependency가 ```pom.xml``` 파일에 자동 설정되며 다양한 프로젝트 속성 값이 ```application.yaml``` 파일에 만들어 진다. DevPilot에서 배치 애플리케이션 프로젝트 생성 절차는 다음과 같다.

1. 프로젝트 생성: ```Batch``` 또는 ```Online/batch``` 선택, 프로젝트 이름 입력 및 DB 또는 Kafka 등의 외부 연계 솔루션 선택
2. 코드 생성기를 통해 Job 생성: Job component를 이용하여 단일의 Job과 단일의 step을 생성하며, 속성창을 통해 job name, step name 및 chunk size 등의 다양한 사용자 속성값 입력
3. DAG 생성기를 통해 Airflow DAG 파일 생성: 배치 실행 시작 시간, 설정 주기 등을 설정

## 템플릿 소스 코드 이름 규칙
DevPilot에 의해 생성 되는 job과 step 관련한 코드는 기본적으로 다음과 같은 명명 규칙을 가진다.
- `JOB`: PREFIX+${JOB_NAME}+POSTFIX
- `STEP`: PREFIX+${STEP_NAME}+POSTFIX

여기서 job name과 step name은 2개 이상의 잡과 스텝을 생성할 경우 각각 구분지어주는 고유한 값이어야 한다. 또한 POSTFIX는 클래스의 기능에 따라 정해진 이름이 사용되며 PREFIX와 POSTFIX는 다음과 같다.
- `PREFIX`: Batch
- `POSTFIX`: JobConfig, JobListener, JobValidator, StepConfig, StepListener

## 템플릿 소스 구조
DevPilot에서 생성한 일반 배치 표준 템플릿의 주요 소스 구조는 다음과 같다.

```bash
|_ job
|_ repository
    |_ dto
|_ step
BackendApplication.java
```

아울러, 온라인 배치 프로젝트를 생성할 경우에는 일반 어플리케이션과 동일하게 controller 및 payload component를 이용하여 endpoint를 추가할 경우 아래의 구조와 같이 endpoint와 payload가 추가로 생성된다.
```bash
|_ endpoint
|_ payload
|_ job
|_ repository
    |_ dto
|_ step
BackendApplication.java
```
- `repository`: DevPilot의 DB builder에 의해 만들어진 DTO class와 repository가 생성되는 위치
- `job`: DevPilot의 코드 생성기에 의해서 만들어지는 job configure가 생성되는 위치
- `step`: DevPilot의 코드 생성기에 의해서 만들어지는 step configure와 reader, writer 및 processor 오퍼레이터가 생성되는 위치
- `endpoint`: DevPilot에서 프로젝트 유형을 온라인 배치로 선택 할 경우 controller가 생성되는 위치
- `payload`: DevPilot에서 프로젝트 유형을 온라인 배치로 선택 할 경우 payload가 생성되는 위치

 또한, DevPilot에 의해 생성되는 코드는 생성 시점을 기준으로 두 가지로 분류된다. 즉, 프로젝트 생성 시점과 코드 생성기에서 제작 되는 시점으로 나누어 볼 수 있다.

- 프로젝트 생성 시점: 여러가지 dependency와 프로젝트 기본 속성이 설정되는 pom파일과 외부 연계 솔루션 속성을 정의한 application.yaml 파일
- 코드 생성기에 의해 제작되는 시점: Job configure, step configure와 다양한 reader, writer 및 processor를 제공하는 오퍼레이터 소스 파일

따라서, 단일의 잡과 tasklet 기반의 스텝을 포함하는 일반 배치 어플리케이션을 생성할 경우에는 다음과 같이 템플릿 소스 코드가 생성된다.
```bash
|_ job
    |_ Batch${job_name}JobConfig.java
    |_ Batch${job_name}JobListener.java
    |_ Batch${job_name}JobValidator.java
|_ repository
    |_ dto
|_ step
    |_ Batch${step_name}StepConfig.java
BackendApplication.java
```
단일의 잡과 단일의 chunk 기반 스텝으로 포함된 일반 배치 어플리케이션을 생성할 경우에는 다음과 같이 템플릿 소스 코드가 생성된다. 여기서 file itemReader와 DB itemWriter를 사용한다고 가정한다.
```bash
|_ job
    |_ Batch${job_name}JobConfig.java
    |_ Batch${job_name}JobListener.java
    |_ Batch${job_name}JobValidator.java
|_ repository
    |_ dto
|_ step
    |_ Batch${step_name}StepConfig.java
    |_ Batch${step_name}StepListener.java
    |_ Batch${step_name}FileItemReader.java
    |_ Batch${step_name}DbWItemWriter.java
    |_ Batch${step_name}ItemProcessor.java
BackendApplication.java
```
만약, 단일의 잡과 단일의 chunk 기반 스텝으로 구성된 온라인 배치 어플리케이션을 생성할 경우에는 다음과 같이 템플릿 소스 코드가 생성된다. Controller와 payload component 사용법은 DevPilot 표준 개발 가이드를 참조한다.

```bash
|_ job
    |_ Batch${job_name}JobConfig.java
    |_ Batch${job_name}JobListener.java
    |_ Batch${job_name}JobValidator.java
|_ endpoint
    |_ Batch${user_input_name}Endpoint.java
|_ payload
    |_ Batch${user_input_name}Payload.java
|_ repository
    |_ dto
|_ step
    |_ Batch${step_name}StepConfig.java
    |_ Batch${step_name}StepListener.java
    |_ Batch${step_name}FileItemReader.java
    |_ Batch${step_name}DbWItemWriter.java
    |_ Batch${step_name}ItemProcessor.java
BackendApplication.java
```

## 템플릿 소스 상세
DevPilot에서는 프로젝트 생성 시점에 일반 배치 또는 온라인 배치를 선택하여 스프링 배치 애플리케이션 프로젝트를 생성할 수 있다. 또한, 외부 솔루션 연계를 위해 생성된 프로젝트에서 사용하기 위한 DB, Kafka 및 Redis를 선택할 수 있다. 그리고 코드 생성기를 통해 잡과 스텝을 구성할 수 있다. 여기서는 DevPloit에 의해 기본적으로 생성해 주는 소스 코드에 대해 기술한다. 

DevPilot에서 생성해 주는 템플릿 소스 코드는 다음과 같다. 잡 관련된 클래스는 job name을 기준으로 생성되며, 스텝 관련된 클래스는 step name을 기준으로 생성된다. 온라인 배치 어플리케이션을 생성하는 경우에는 controller와 payload component를 이용하여 사용자 입력 이름 기반으로 endpoint 소스 코드를 추가로 만들 수 있다.
- Batch${job_name}JobConfig.java
- Batch${job_name}JobListener.java
- Batch${job_name}JobValidator.java
- Batch${step_name}StepConfig.java
- Batch${step_name}StepListener.java
- Batch${step_name}FileItemReader.java
- Batch${step_name}FileItemWriter.java
- Batch${step_name}DbItemWriter.java
- Batch${step_name}DbItemWriter.java
- Batch${step_name}KafkaItemWriter.java
- Batch${step_name}ItemProcessor.java
- Batch${user_input_name}Endpoint.java
- Batch${user_input_name}Payload.java
- BackendApplication.java

**1. Job**

DevPilot은 Job 관련해서 다음과 같이 3개의 파일을 생성해 준다.
```bash
Batch${job_name}JobConfig.java
Batch${job_name}JobListener.java
Batch${job_name}JobValidator.java
```
**Batch${job_name}JobConfig**

```jobBuilderFactory```가 jobBuilder 인스턴스를 생성하며 실제로 스프링 배치 잡을 생성하는데 사용된다. 이 팩토리 메서드는 ```jobbuilderFactory.get()```메서드를 통해 잡 이름을 전달받으며 잡에서 수행할 스텝을 지정한 후에 ```jobBuilder.build()```를 호출해 잡을 생성한다. 템플릿에서는 다수의 스텝을 추가하는 경우 ```jobBuilder.start()``` 뿐만 아니라 ```jobBuilder.next()``` 메서드를 순차적으로 추가하여 잡을 구성한다. 

또한, 하나의 식별 JobParameter를 사용해 동일한 잡을 두 번 이상 실행할 수 없으므로 DevPilot에서 생성하는 템플릿 코드에서는 jobParametersIncrementer를 사용하여 잡에서 사용할 파라미터를 고유하게 생성하도록 한다. 아래와 소스 코드와 같이 runIdIncrementer를 도입하여 고유한 jobParameter를 생성하는 것을 볼 수 있다.

추가적으로 jobExecutionListener를 사용하여 잡 생명주기에서 가장 먼저 실행되거나 가장 나중에 실행되어야 할 비즈니스 로직을 구현할 수 있도록 ```JobBuilderFactory.listener()```가 추가된다.

마지막으로 외부에서 입력을 받아들일 때마다 그 값이 예상대로 유효한지 확인하기 위해 스프링 배치에서 잡 파라미터를 쉽게 검증할 수 있게 jobParameterValidaotr 인터페이스 구현체를 받아 잡에 추가한다.

```bash
@Bean
public Job samp() {

    return jobBuilderFactory
            .get("samp")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .start(batchSampStep.samp())
            .next(batchSampTaskletStep.sampTasklet())
            .validator(validator)
            .build();
}
```
**Batch${job_name}JobListener**

JobExecutionListener 인터페이스는 beforeJob()과 afterJob()의 두 개의 메서드를 제공한다. 이 두 콜백 메서드는 잡 생명주기에서 가장 먼저 실행되거나 가장 나중에 실행된다. 따라서 비즈니스 로직에 맞게 잡의 시작이나 종료를 나타내는 알림, 잡 실행 전에 준비해둬야 할 초기화 및 잡이 실행 이후에 성공 혹은 실패 여부에 따라 정리해야 될 작업 등을 수행할 수 있다. DevPilot에서는 jobExecutionListener를 상속받은 BatchJobListener을 제공한다.

```bash
public abstract class BatchJobListener implements JobExecutionListener {

    public abstract void beforeExec(JobExecution ex);

    public abstract void afterExec(JobExecution ex);

    @Override
    public void beforeJob (JobExecution ex) {
        Assert.notNull(ex, "error sample Message");
        log.info("sampleMessage");
        beforeExec(ex);
    }

    @Override
    public void afterJob(JobExecution ex) {
        Assert.notNull(ex, "error sample Message");
        log.info("sampleMessage");
        afterExec(ex);
    }

}
```
템플릿 코드는 다음과 같이 생성되며 개발자는 각자의 비즈니스 로직에 맞게 ```beforeExec()```와 ```afterExec()```을 이용하여 실제 처리가 필요한 부분을 구현할 수 있다.

```bash
public class BatchSampJobListener extends BatchJobListener {

    @Override
    public void beforeExec(JobExecution jobExecution) {
    }

    @Override
    public void afterExec(JobExecution jobExecution) {
    }
}
``` 

**Batch${job_name}JobValidator**

잡 파라미터 유효성을 검증하기 위해 jobParameterValidator 구현체를 제공한다. 아래 코드와 같이 개발자는 validate 메서드를 오버라이딩 해서 검증기능을 구현할 수 있다.

```bash
public class BatchSampJobValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters jobParameters) {

    }
}
```

**2. Step**

잡이 전체적인 처리를 정의한다면 스텝은 잡의 구성 요소를 담당한다. 스텝은 독립적이고 순차적으로 배치 처리를 수행한다. 그렇기 때문에 스텝은 모든 단위 작업의 조각이다. 자체적으로 입력을 처리하고 자체적인 처리기를 가질 수 있으며 또한 자체적으로 출력을 처리한다. 트랜잭션은 스텝 내에서 이루어 지며 스텝은 서로 독립적이며 의도적으로 설계 될 수 있다. 결과적으로 스텝을 이용하여 잡을 구조화 할 수 있다. 

배치 처리는 일반적으로 데이터 처리와 관련이 있다. 스텝의 첫 번째 타입은 tasklet이다. Tasklet interface를 사용해 개발자는 Tasklet.execute 메서를 구현하게 된다. 두 번째 타입은 Chunk기반의 스텝이다. 이러한 chunk기반의 스텝은 최소한 2 내지 3개의 주요 컴포넌트인 itemReader, itemProcessor 및 itemWriter로 구성된다. 여기서 itemProcessor는 필수가 아닐 수 있다. 스프링 배치는 이러한 컴포넌트를 사용해서 chunk단위로 데이터를 처리한다.

위에서 언급한 세 가지 컴포넌트를 사용하여 잡을 구성하면 잡은 세 가지 행동을 처리한다. 첫 번째는 itemReader에서 청크 단위로 처리할 모든 레코드를 반복적으로 메모리로 읽어 낼 수 있다. 두 번째는 itemProcessor에서 메모리로 읽어 들인 데이터를 조작하는 행동이 처리된다. 마지막으로 itemWriter를 호출하면서 모든 데이터를 목적지에 기록한다. 즉, 스텝은 state machine과 같이 행동한다. 각 스텝이 상태와 다음 상태로 이어지는 transition이 모여 있는 state machine 처럼 행동한다. 아래에서 스텝의 유형에 대해 기술한다.

DevPilot의 코드 생성기에서 어떤 유형의 배치 스텝 선택하더라도 step name 기반으로 다음과 같이 소스코드가 생성된다. 

```bash
Batch${step_name}StepConfig.java
```

**Tasklet 스탭**

Tasklet 스텝은 tasklet 인터페이스의 execute 메서드를 구현하여 구성할 수 있다. Tasklet 구현체의 처리가 완료되면 RepeatStatus의 객체를 반환하도록 만든다. RepeatStatus.CONTINUABL 또는 RepeatStatus.FINISHED 중에서 하나를 선택해서 반환한다. 만약 CONTINUABLE을 반환할 경우 스프링 배치에게 어떤 조건이 충족될 때 까지 다시 실행하도록 한다. 반면 FINISHED를 반환하는 것은 처리의 성공 여부에 관계없이 이 tasklet의 처리를 완료하고 다음 처리를 이어서 하겠다는 의미이다. DevPilot에서 제공하는 tasklet 스텝의 모습은 다음과 같다. ```jobBuilderFactory```가 ```JobBuilder``` 인스턴스를 생성하듯이 ```stepBuilderFactory```는 ```StepBuilder``` 인스턴스를 생성한다. 여기서 ```stepBuilderFactory.get()```으로 step name을 전달하며 ```stepBuilderFactory.tasklet```으로 tasklet 구현체를 전달할 수 있다. 최종적으로 ```stepBuilderFactory.build()```를 호출하여 실제 스텝을 생성한다.

```bash
public class BatchSampTaskletStepConfig {

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step batchSampTaskletStep() {
        return stepBuilderFactory
                .get("batchSampTaskletStep")
                .tasklet(batchTasklet())
                .build();
    }

    private Tasklet batchTasklet() {
        return null;
    }
}
```

**Chunk 스텝**

DevPilot의 코드 생성기에서 사용자는 tasklet기반 뿐만 아니라 chunk 기반의 배치 스텝 유형을 선택할 수 있다. 또한, step name과 summary 등의 기본 정보를 입력하게 된다. 만약 chunk 기반의 스텝을 구성할 경우에는 기본적으로 다음과 같은 소스 코드가 생성된다.

```bash
Batch${step_name}StepConfig.java
Batch${step_name}StepListener.java
```

또한, chunk 기반의 스텝인 경우에는 itemReader, itemWriter 와 itemProcessor 등의 오퍼레이터가 추가로 생성된다. DevPilot에서 제공하는 오퍼레이터는 다음과 같다. ItemProcessor는 비즈니스 로직에 따라 필수가 아닐 수 있으나 itemReader와 itemWriter는 필수적으로 구현된다.

|               | ItemReader                         | ItemWriter                      |           
|---------------|------------------------------------|---------------------------------| 
|File           | Batch${step_name}FileItemReader    | Batch${step_name}FileItemWriter |
|DB             | Batch${step_name}DbItemReader      | Batch${step_name}DbItemWriter   |
|Kafka          | N/A                                | Batch${step_name}KafkaItemWriter|

따라서, DevPilot으로 chunk 기반의 스텝을 구성할 경우에는 File, DB 또는 Kafka reader 혹은 writer를 선택해야 한다. 그래서 다음과 같은 소스 코드가 추가로 생성될 수 있다.

```bash
Batch${step_name}FileItemReader.java
Batch${step_name}FileItemWriter.java
Batch${step_name}DbItemReader.java
Batch${step_name}DbItemWriter.java
Batch${step_name}KafkaItemWriter.java
Batch${step_name}ItemProcessor.java
```
**Batch${step_name}StepConfig**

Tasklet 스텝에서 설명한 바와 같이 ```stepBuilderFactory```는 ```stepBuilder``` 인스턴스를 생성한다. 여기서 ```stepBuilderFactory.get()```으로 step name을 전달하며 최종적으로 ```stepBuilderFactory.build()```를 호출하여 실제 스텝을 생성한다. Tasklet 스텝과 차이점은 ```chunk()``` 메서드를 통해 chunk 기반 스텝을 사용하도록 지정한다. 청크 사이즈는 DevPilot의 코드 생성기에서 배치 스텝을 구성할 때 사용자 입력으로 받을 수 있는 속성값이다. 만약 청크 사이즈를 10으로 설정한 경우 10개 단위로 데이터를 처리한 후 작업이 커밋된다. 아래 소스 코드에서 보듯이 이러한 청크 기반의 스텝은 ```build()``` 메서드가 호출되기 전에 reader와 writer를 가져 온다. 여기서 청크 사이즈를 통해 커밋 간격을 지정하는 것은 중요한 의미를 가진다. 청크 사이즈가 10으로 설정된 경우 10개의 데이터를 읽고 처리할 때까지 어떤 데이터도 쓰기 작업을 하지 않는다. 그리고 ```reader()```와 ```writer()``` 메서드를 이용하여 itemReader와 itemWriter의 구현체를 등록하여 ```build()``` 메서드를 호출하여 실제 스텝을 생성한다. 또한, 잡 리스너와 마찬가지로 ```listener()``` 메서드로 스텝 리스너를 등록할 수 있다. 

```bash
@Bean
public Step samp()  {
    return stepBuilderFactory
            .get("samp")
            .<SampReaderDto, SampWriterDto>chunk(CHUNK_SIZE)
            .reader(reader.reader())
            .processor(processor)
            .writer(writer.writer())
            .listener(listener)
            .build();
}
```

**Batch${step_name}StepListener**

스텝 리스너도 동일한 유형의 이벤트를 처리하지만 잡 전체가 아닌 개별 스텝 단위에서 이루어진다. 여기서 동일한 유형의 이벤트는 시작과 종료 처리를 의미하며 ```stepExecutionListener``` 인터페이스의 ```beforeStep```과 ```afterStep```을 구현하여 이루어 진다. ```afterStep``` 메서드는 ```ExitStatus```를 반환한다. 이것은, ```ExitStatus```를 잡에 전달하기 전에 수정할 수 있기 때문이다. 이 기능은 잡 처리 성공 여부를 판변하는 데 사용할 수 도 있으며 그 이상으로 잡에 유용하게 사용할 수 있다. 예를 들어, 파일을 가져 온 후 데이터베이스에 올바른 갯수로 레코드가 기록됬는지 여부를 확인 하는 등의 기본적인 무결성 검사를 수행 할 수 있다. DevPilot은 JobListener와 동일하게 BatchStepListener를 제공한다.

```bash
public abstract class BatchStepListener implements StepExecutionListener {

    public abstract void beforeExec(StepExecution ex);

    public abstract ExitStatus afterExec(StepExecution ex);

    @Override
    public void beforeStep(StepExecution ex) {
        Assert.notNull(ex, "error sample Message");
        log.info("sampleMessage");
        beforeExec(ex);
    }

    @Override
    public ExitStatus afterStep(StepExecution ex) {
        Assert.notNull(ex, "error sample Message");
        log.info("sampleMessage");
        return afterExec(ex);
    }

}
```
BatchJobListener를 상속하여 아래와 같은 템플릿 소스가 생성되며 실제 처리가 필요한 부분을 beforeExec()와 afterExec()함수에 구현한다.

```bash
public class BatchSampStepListener extends BatchStepListener {
    @Override
    public void beforeExec(StepExecution ex) {
        return;
    }

    @Override
    public ExitStatus afterExec(StepExecution ex) {
        return null;
    }
}
```

**Batch${step_name}FileItemReader**

DevPilot에서 아래 소스 코드와 같이 ```MultiResourceReader```와 함께 ```FlatFileItemReader```를 제공한다. 

```bash
public class BatchFileItemReader<T> {

    public FlatFileItemReader<T> reader(Class<T> type, String[] names, String delimiter) {
        return new FlatFileItemReaderBuilder<T>()
                .delimited()
                .delimiter(delimiter)
                .names(names)
                .targetType(type)
                .saveState(false)
                .name("BatchFileItemReader")
                .build();
    }

    public FlatFileItemReader<T> reader(Class<T> type, String[] names, String delimiter, Resource resource) {
        FlatFileItemReader<T> reader = reader(type, names, delimiter);
        reader.setResource(resource);
        return reader;
    }

    public MultiResourceItemReader<T> reader(Class<T> type, String[] names, String delimiter, Resource... resources) {
        return new MultiResourceItemReaderBuilder<T>()
                .resources(resources)
                .delegate(reader(type,names,delimiter))
                .build();
    }
}
```
BatchFileItemReader를 사용하기 위해서는 변환하려는 클래스 타입 ```type```, 변환 후 바인딩 할 속성 배열 ```names```, 구분자인 ```delimiter```와 변환할 리소스 ```resource```가 필요하다.
BatchFileItemReader는 default 값으로 ```saveState``` 값을 false로 지정한다. 동시에 여러 파일을 넣는 것도 가능한데, 매개변수에 reader대신 ```reader 배열```을 넣으면 MultiResourceItemReader으로 동작한다.

**Batch${step_name}FileItemWriter**

DevPilot에서 Reader와 마찬가지로 ```FlatFileItemWriter```를 제공하며 아래의 소스 코드와 같다.

```bash
public class BatchFileItemWriter<T> {

    public FlatFileItemWriter<T> writer(String delimiter, String[] names, Resource resource){
        return new FlatFileItemWriterBuilder<T>()
                .resource(resource)
                .delimited()
                .delimiter(delimiter)
                .names(names)
                .saveState(false)
                .name("BatchFileItemWriter")
                .build();
    }
    
    public FlatFileItemWriter<T> writer(String delimiter,
                                        String[] names,
                                        Resource resource,
                                        String encoding,
                                        FlatFileHeaderCallback header,
                                        FlatFileFooterCallback footer) {
        FlatFileItemWriter<T> writer = writer(delimiter, names, resource);
        writer.setFooterCallback(footer);
        writer.setHeaderCallback(header);
        writer.setEncoding(encoding);

        return writer;
    }
}
```
Reader와 유사하게 바인딩 할 속성 배열 ```names```, 구분자인 ```delimiter```와 변환할 리소스 ```resource```가 필요하다. 추가로 Reader에는 파일에 적용할 수 있는 ```footer```와 ```header```, ```encoding```을 추가할 수 있다.


**Batch${step_name}DbItemReader**

DevPilot에서 아래의 소스 코드와 같이 ```RepositoryItemReader```를 제공한다.

```bash
public class BatchDbItemReader<T> {

    public RepositoryItemReader<T> reader (PagingAndSortingRepository<T,?> repository,
                                           String methodName,
                                           int chunkSize) {
        return new RepositoryItemReaderBuilder<T>()
                .repository(repository)
                .methodName(methodName)
                .pageSize(chunkSize)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .name("BatchDbItemReader")
                .build();
    }

    public RepositoryItemReader<T> reader (PagingAndSortingRepository<T,?> repository,
                                           String methodName,
                                           int chunkSize,
                                           Map<String, Sort.Direction> sorts) {
        RepositoryItemReader<T> reader = reader(repository, methodName, chunkSize);
        reader.setSort(sorts);

        return reader;
    }

    public RepositoryItemReader<T> reader (PagingAndSortingRepository<T,?> repository,
                                           String methodName,
                                           int chunkSize,
                                           Map<String, Sort.Direction> sorts,
                                           List<?> arguments) {
        RepositoryItemReader<T> reader = reader(repository, methodName, chunkSize);
        reader.setSort(sorts);
        reader.setArguments(arguments);
        return reader;
    }
}
```
BatchDbItemReader는 RepositoryItemReader와 동일하게 ```repository```와 ```methodName```을 필요로 한다. Repository는 PagingAndSortingRepository를 상속한 클래스이다. 또한, BatchDbItemReader는 default 값으로 sort를 정한다. sort값은 id, sort direction은 ASC이다. 해당 값은 직접 reader에서 입력할 수 있도록 함수를 제공하고 있다. 메서드에서 인수를 받아야 한다면 ```argument```를 작성하면 된다.


**Batch${step_name}DbItemWriter**

DevPilot에서 데이터베이스 오퍼레이터로 Reader와 함께 ```RepositoryItemWriter```를 제공한다.

```bash
public class BatchDbItemWriter<T> {

    public RepositoryItemWriter<T> writer(CrudRepository<T,String> repository, String methodName){
        return new RepositoryItemWriterBuilder<T>()
                .repository(repository)
                .methodName(methodName)
                .build();
    }
}
```
BatchDbItemWriter의 사용 방법은 RepositoryItemWriter와 동일하다.

**Batch${step_name}KafkaItemWriter**

BatchKafkaItemWriter의 경우 Kafkatemplate와 함께 itemKeyMapper 인자를 받는다. kafkaTemplate 값만 입력하는 경우 키매핑 없이 KafkaTemplate의 키 값이 null으로 들어가게 된다. Key Value 값을 설정하고 싶은 경우 Converter 인터페이스를 구현하여 매개변수에 포함하면 된다.
```bash
public KafkaItemWriter<K,T> writer (KafkaTemplate<K,T> kafkaTemplate) {
        return new KafkaItemWriterBuilder<K,T>()
                .kafkaTemplate(kafkaTemplate)
                .itemKeyMapper(item -> null)
                .build();
    }
```
**Batch${step_name}ItemProcessor**

스프링 배치에서 읽은 데이터를 사용해 특정 작업을 수행해야 하는 경우가 있다. 이때, 사용 가능한 인터페이스는 ```org.springfrmaework.batch.item.itemProcessor``` 이다. 이 인터페이스는 ```process```라는 단일 메서드를 갖고 있다. 이 인처페이스는 itemReader가 읽어 들인 아이템을 전달 받아 특정 처리를 수행한 후 결과 아이템을 반환한다. 아래는 itemProcessor의 인터페이스 구현체이다.

```bash
package org.springfrmaework.batch.item;

public interface ItemProcessor<I, O> {
    O process (I item) throws Exception;
}
```

여기서 itemProcessor가 받아들이는 입력 아이템의 타입과 반환하는 아이템의 타입이 같을 필요가 없다. ItemProcessor는 자신이 전달 받은 입력 아이템 객체의 타입을 쓰기 작업을 수행하기 위한 다른 타입으로 변환해 반환할 수 있다. 이 기능을 사용할 때 최종적으로 itemProcessor가 반환하는 타입은 itemWriter가 입력으로 사용하는 타입이 돼야 한다. 만약 itemProcessor가 null을 반환하면 해당 아이템의 이후 모든 처리가 중지된다. 즉, itemWriter가 호출되지 않는다.

## 추가 사항

**1. Repository**

Repository와 dto 클래스 생성은 DevPilot에서 일반 애플리케이션 프로젝트에서 repository와 DTO를 생성하는 방식과 동일하다. 그러나 배치 애플리케이션에는 aggregate입력이 불필요하며, 속성창의 type을 ```none```을 선택하여 생성한다. 이때 repository와 dto 디렉토리에 각각 해당 소스 코드가 생성된다. Repository와 DTO를 생성하는 방법은 DevPilot 개발자 가이드를 참고하기 바란다.

**2. DefaultBatchConfigurer**

DevPilot 코드 생성기는 잡과 스텝의 기본 기능을 위한 클래스 외에 부수적으로 jobParameter와 메타 데이터 등을 커스터마이징 하기 위해 추가적인 클래스를 제공한다. 

@EnableBatchProcessing 어노테이션을 이용하면 추가적인 구성 없이 스프링 배치가 제공하는 jobRepository를 사용할 수 있다. 그러나 jobRepository에 대한 커스터마이징이 필요 하다. DevPilot에서 제공하는 템플릿 소스 코드는 BatchConfigurer 인터페이스를 사용해 JobRepository를 비롯한 모든 스프링 배치 infrastructure를 커스터마이징 할 수 있는 방안을 제공한다.

```bash
public class BatchConfigurer extends DefaultBatchConfigurer {

    private DataSource dataSource;

    private DatabaseType databaseType;

    @Override
    public void initialize() {
        super.setDataSource(dataSource);
        super.initialize();
    }

    @Override
    protected JobRepository createJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setDatabaseType(databaseType.getProductName());
        factory.setTransactionManager(super.getTransactionManager());
        factory.afterPropertiesSet();

        return factory.getObject();
    }

    @Override
    public JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.setJobRepository(super.getJobRepository());
        jobLauncher.afterPropertiesSet();

        return jobLauncher;
    }

}
```
Batch Auto configuration 은 미리 정의된 configurer가 없을때 DefaultBatchConfigurer을 사용한다. 해당 클래스의 메서드를 오버라이딩 하면 필요한 속성을 변경하여 사용할 수 있다. 예를 들어, Batch에 쓰이는 메타 데이터를 다른 데이터베이스에 저장하는 경우를 들 수 있다. BatchConfigurer는 별개 또는 다중 DataSource를 사용하는 경우를 위해 속성으로 DataSource와 그 타입인 DatabaseType을 가지고 있다. 해당 속성 값들을 setter 메서드로 부여하면 배치 메타데이터만 다른 데이터베이스를 사용하게 할 수 있다.

Devpilot에서 제공하는 BatchConfigurer 클래스 내의 configurer에 사용되는 속성을 작성하는 config 클래스를 사용자는 추가 구현해야 한다. 예시는 BatchConfigurer을 bean으로 등록하는 BatchSampConfig 클래스이다. 

```bash
public class BatchSampConfig {

    private final DataSource dataSource;//정의된 Datasource.

    private final JobRegistry jobRegistry;

    private final JobLauncher jobLauncher;

    private final JobExplorer jobExplorer;

    private final JobRepository jobRepository;

    @Bean
    public BeanPostProcessor jobRegistryBean() throws Exception {
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(jobRegistry);
        postProcessor.afterPropertiesSet();

        return postProcessor;
    }

    @Bean
    public BatchConfigurer batchConfigurer() {
        BatchConfigurer configurer = new BatchConfigurer();
        configurer.setDataSource(dataSource);
        configurer.setDatabaseType(DatabaseType.ORACLE);

        return configurer;
    }
    
    @Bean
    public BatchJobOperator batchJobOperator(){
        return new BatchJobOperator(jobLauncher,jobExplorer,jobRepository,jobRegistry);
    }

}
```
위의 샘플 소스 코드를 보면, Batch 메타데이터 저장에 Tibero DB를 사용하기로 하였다. Tibeo가 정의된 Datasource를 주입받아 사용한다. Tibero의 DatabaseType값인 "Tibero" 는 현재 Configuration 과정에서 지원되지 않으므로, DatabaseType Enum 클래스값 Oracle을 사용하도록 한다.

DefaultBatchConfigurer는 SimpleJobLauncher를 사용한다. SimpleJobLauncher의 default taskExecutor는 SyncTaskExecutor이다. BatchConfigurer에서는 잡의 비동기적 처리를 위해 SimpleJobLauncher에서 SimpleAsyncTaskExecutor를 사용하도록 하였다.

**3. BatchJobOperator**

Spring batch는 일반적으로 동일 JobParameter로 같은 Job을 실행하지 않는다. JobParameter가 중복되는 것을 막고 고유한 JobParameter를 제공하기 위해 Devpilot에서는 BatchJobOperator를 사용한다.
애플리케이션에서 Job 실행 요청을 보내면, BatchJobOperator에서는 내부적으로 Job에 할당된 runIdcremeter를 사용하여 JobParameter를 생성하여 준다. 생성된 JobParameter와 JobName을 사용하여 인스턴스가 사용중인지 체크 한 뒤, 잡을 실행한다. 아래 소스 코드와 같이 BatchJobOperator는 ```start``` 메서드를 제공하며, jobName을 받아 배치를 구동시키는 기능을 제공한다.

```bash
public class BatchJobOperator {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;

    public void start (String jobNm)  {
        try {
            Job job = jobRegistry.getJob(jobNm);
            JobParameters param = new JobParametersBuilder(jobExplorer)
                    .getNextJobParameters(job)
                    .toJobParameters();

            if (jobRepository.isJobInstanceExists(jobNm, param)) {
                throw new JobInstanceAlreadyExistsException(
                        String.format("job instance already exists.name=%s and parameters=%s", jobNm, param));
            }

            jobLauncher.run(job, param);
        }
        catch (NoSuchJobException e) {
            log.info("error message for no such Job" , e);
            e.printStackTrace();

        } catch (JobExecutionAlreadyRunningException |
                JobRestartException |
                JobInstanceAlreadyCompleteException |
                JobParametersInvalidException |
                JobInstanceAlreadyExistsException e ) {
            log.info("error message for JobExecuting process" , e);
            e.printStackTrace();

        }
    }

    public void start(List<String> jobNms) {
        jobNms.forEach(this::start);
    }
}
```

따라서, controller와 main에서 ```BatchJobOperator.start()``` 메서드를 호출하여 배치 어플리케이션을 구동한다.

**4. Entry point**

여기서는 스프링 부트가 어떻게 잡을 실행하는 지 기술한다. 스프링 부트는 배치 애플리케이션 기동 시에 어떤 잡을 실행할 지 정의 하는 방법을 제공한다. 예를 들어, REST 호출이나 특정 이벤트 등으로 배치 잡을 실행할 계획이라면 애플리케이션이 기동되는 시점에는 해당 잡이 실행되지 않아야 한다. 이렇게 동작하도록 스프링 부트는 ```spring.batch.job.enabled``` 프로퍼티를 제공하며 애플리케이션의 ```application.yml``` 파일 내에 이 프로퍼티를 ```false```로 표시하면 된다. 이 프로퍼티의 초기값은 ```true```이므로 명시적으로 ```false```로 표시해야 된다. 그러면 배치 애플리케이션이 기동 시 어떤 잡도 실행 되지 않는다. 

```bash
  batch:
    job:
      enabled: false
```
DevPilot에서 생성되는 배치 애플리케이션은 Airflow를 통해 실행된다. 이때 Airflow의 DAG 파일에서 특정 잡 이름을 지명하여 배치 잡을 기동 시키며 아래 소스 코드와 같이 ```batchJobService.run()``` 메소드를 호출하여 비동기 방식으로 잡을 수행한다. 

```bash
public class BackendApplication implements ApplicationRunner {

    private final BatchJobOperator operator;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> jobNms = args.getOptionValues("job.names");

        if(jobNms != null) {
            operator.start(jobNms);
        }
    }
}
```

**5. Endpoint**

온라인 배치의 경우에는 DevPilot의 코드 생성기에서 제공하는 controller와 payload component를 이용하여 endpoint를 별도로 생성할 수 있다. 배치의 경우에는 각각의 component 속성창에서 ```none```을 선택하고 생성을 하며 아래와 같은 소스 코드가 추가로 생성된다. Controller와 payload component를 사용 방법에 대한 자세한 사항은 DevPilot 표준 개발 가이드를 참고한다.

배치 어플리케이션은 BatchJobOperator의 start() 메서드를 호출하여 구동한다.

```bash
|_ endpoint
    |_ Batch${user_input_name}Endpoint.java
|_ payload
    |_ Batch${user_input_name}Payload.java
```
 아래의 소스 코드는 controller component를 통해 생성하여 사용자가 추가 개발한 사례를 보여 준다.

```bash
@Slf4j
@RequiredArgsConstructor
@RestController
public class BatchSampApi {

    private final BatchJobOperator operator;

    @PostMapping("/job/sync")
    public Callable<Object> runcJob(@RequestBody BatchSampInPyId batchSampInPyId)  {
        return () -> {
            operator.start(batchSampInPyId.getJobNm());
            return null;
        };
    }

}
```
아래의 소스 코드는 payload component를 통해 생성하여 사용자가 추가 개발한 사례를 나타낸다.
```bash
@Getter
@Setter
public class BatchSampInPyId {
    private String jobNm;
}
```
## 개발표준
스프링 배치 어플리케이션 개발에 필요한 표준은 [온라인개발가이드](https://wiki.dspace.kt.co.kr/pages/viewpage.action?pageId=53489701#ICISTR1%EB%8B%A8%EA%B3%84SA07%EC%98%A8%EB%9D%BC%EC%9D%B8%EA%B0%9C%EB%B0%9C%EA%B0%80%EC%9D%B4%EB%93%9C-%EA%B0%9C%EB%B0%9C%ED%91%9C%EC%A4%80)를 준용하며 다음의 규칙을 표준화한다.
- 문서화
- 주석
- 패키지 구조
- 클래스 정의 방식
- 클래스 메서드
- 변수

## 로그유틸
스프링 배치 어플리케이션은 로그 메세지를 출력하기 위한 공통 정책을 제공한다. 디버깅 등을 위해 필요한 로그 메세지는 배치 공통 프레임워크에서 제공하는 ```BatchLogUtil```을 호출하여 생성한다. 또한, 로그 출력시 출력값에 ```{ }```를 사용하여 별도의 문자열 더하기 연산을 수행하지 않도록 하며 로그 레벨에 따라 warn, debug, info등의 그에 맞는 메서드를 호출한다. DevPilot에서 제공하는 로그 거버넌스는 다음과 같다.
- Log는 반드시 batch common framework에서 제공하는 BatchLogUtil만을 사용한다.
- Log 레벨은 debug, info, warn, error로 구별하여 사용한다.
- Log는 반드시 발생 시간과 위치 그리고 내용을 포함한다.
- Debug log는 개발시에만 사용하며 운영에는 사용하지 않는다.
- Info log는 운영자 입장에서 운영에 필요한 내용만 기록한다.
- Warn log는 잠재적으로 error를 유발할 수 있는 개연성이 가진 내용만 기록한다.
- Error log는 error code와 함께 error에 대한 내용을 기록한다.
- Unchecked exception이 발생하면 stack trace 내용을 그대로 로그에 기록한다.