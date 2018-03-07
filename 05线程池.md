**关键字：**：Executor框架, newFixedThreadPool,newSingleThreadExecutor,newCacheThreadPool,newScheduledThreadPool, ThreadPoolExecutor详解
github 地址： https://github.com/zhaikaishun/concurrent_programming 
本篇文章代码在Multi_005 中

---

## Executor框架  
为了更好的控制多线程， JDK提供了一套线程框架Executor, 帮助开发人员有效地进行线程控制。他们都在java.util.concurrent包中， 是JDK并发包的核心，其中有一个比较重要的类： Executors，他扮演者线程工厂的角色，我们通过Executors可以创建特定功能的线程池。   
![java-executor](http://or49tneld.bkt.clouddn.com/17-10-5/42681996.jpg)  




### Executors创建线程的方法  

- **newFixedThreadPool()**方法，该方法返回一个固定数量的线程池，该方法的线程数始终不变，当有一个任务提交时，若线程池中空闲，则立即执行，若没有，则会被暂缓在一个任务队列中等待有空闲的线程去执行。  


- **newSingleThreadExecutor()**方法，创建**一个**线程的线程池，若空闲则执行，若没有空闲线程则暂缓在队列任务中。  


- **newCacheThreadPool()**方法，返回一个可以根据实际情况调整线程个数的线程池，不限制最大线程量，若有空闲线程则执行任务，若无任务则不创建线程，并且每一个空闲线程会在60秒后自动回收。  


- **newScheduledThreadPool()**方法，该方法返回一个SchededExecutorService对象，但该线程池可以执行线程的数量  


### ThreadPoolExecutor()  
其实上面的几种线程池都是通过ThreadPoolExecutor()来实例出来的，如果上面的还不满足我们的要求，我们可以自己创建线程池，ThreadPoolExecutor有几个重载方法：  
```java 
     
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue)

    public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory)


    public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          RejectedExecutionHandler handler)

    public ThreadPoolExecutor(int corePoolSize,
                      int maximumPoolSize,
                      long keepAliveTime,
                      TimeUnit unit,
                      BlockingQueue<Runnable> workQueue,
                      ThreadFactory threadFactory,
                      RejectedExecutionHandler handler)


```    
![线程池](http://img.blog.csdn.net/20160724192641221)  

- **corePoolSize** 核心线程数，如果不进行特别的设定，线程池中始终会保持corePoolSize数量的线程数（一创建出来就有这么多个线程）  
- **maximumPoolSize** 最大允许存在的线程数: 包括核心线程数和非核心线程数,一旦任务数量过多（由等待队列的特性决定），线程池将创建“非核心线程”临时帮助运行任务。你设置的大于corePoolSize参数小于maximumPoolSize参数的部分，就是线程池可以临时创建的“非核心线程”的最大数量。这种情况下如果某个线程没有运行任何任务，在等待keepAliveTime时间后，这个线程将会被销毁，直到线程池的线程数量重新达到corePoolSize。  
- **keepAliveTime参数和timeUnit参数**: 配合使用,代表空闲线程的回收时间，如果设置的corePoolSize参数和设置的maximumPoolSize参数一致时，线程池在任何情况下都不会回收空闲线程。keepAliveTime和timeUnit也就失去了意义。    
- **workQueue** 等待队列： 当线程数过多时，多余的线程放入到队列等待执行。    
- **RejectedExecutionHandler** 拒绝策略,当提交给线程池的某一个新任务无法直接被线程池中“核心线程”直接处理，又无法加入等待队列，也无法创建新的线程执行, 这时候ThreadPoolExecutor线程池会拒绝处理这个任务，触发创建ThreadPoolExecutor线程池时定义的RejectedExecutionHandler接口的实现.    

示例：  
这个案例简要介绍了如何使用自定义ThreadPoolExecutor  
**主方法**  
```java
public class UseThreadPoolExecutor1 {
	public static void main(String[] args) {
		/**
		 * 在使用有界队列时，若有新的任务需要执行，如果线程池实际线程数小于corePoolSize，则优先创建线程，
		 * 若大于corePoolSize，则会将任务加入队列，
		 * 若队列已满，则在总线程数不大于maximumPoolSize的前提下，创建新的线程，
		 * 若线程数大于maximumPoolSize，则执行拒绝策略。或其他自定义方式。
		 * 
		 */	
		ThreadPoolExecutor pool = new ThreadPoolExecutor(
				1, 				//coreSize
				2, 				//MaxSize
				60, 			//60
				TimeUnit.SECONDS, 
				new ArrayBlockingQueue<Runnable>(3)			//指定一种队列 （有界队列）
				//new LinkedBlockingQueue<Runnable>()
				, new MyRejected()
				//, new DiscardOldestPolicy()
				);
		
		MyTask mt1 = new MyTask(1, "任务1");
		MyTask mt2 = new MyTask(2, "任务2");
		MyTask mt3 = new MyTask(3, "任务3");
		MyTask mt4 = new MyTask(4, "任务4");
		MyTask mt5 = new MyTask(5, "任务5");
		MyTask mt6 = new MyTask(6, "任务6");
		pool.execute(mt1);
		pool.execute(mt2);
		pool.execute(mt3);
		pool.execute(mt4);
		pool.execute(mt5);
		pool.execute(mt6);
		pool.shutdown();
	}
}
```  
**MyTask类**  
```java
public class MyTask implements Runnable {

	private int taskId;
	private String taskName;
	
	public MyTask(int taskId, String taskName){
		this.taskId = taskId;
		this.taskName = taskName;
	}
	
	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	@Override
	public void run() {
		try {
			System.out.println("run taskId =" + this.taskId);
			Thread.sleep(5*1000);
			//System.out.println("end taskId =" + this.taskId);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	public String toString(){
		return Integer.toString(this.taskId);
	}

}

```

**拒绝策略的类MyRejected**  
```java
public class MyRejected implements RejectedExecutionHandler{

	
	public MyRejected(){
	}
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		System.out.println("自定义处理..");
		System.out.println("当前被拒绝任务为：" + r.toString());
	}
}
```
**输出**  
因为2+3 = 5 而总共有6个任务，所以最后一个被拒绝，走了MyRejecte类的rejectedExecution方法
```
自定义处理..
run taskId =1
当前被拒绝任务为：6
run taskId =5
run taskId =2
run taskId =3
run taskId =4
```

如果没有定义拒绝策略，出现拒绝时会catch RejectedExecutionException 异常  

再举个例子深入了解一下coresize和maxsize： 
```java
public class UseThreadPoolExecutor2 implements Runnable{

	private static AtomicInteger count = new AtomicInteger(0);
	
	@Override
	public void run() {
		try {
			int temp = count.incrementAndGet();
			System.out.println("任务" + temp);
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception{
		//System.out.println(Runtime.getRuntime().availableProcessors());
		BlockingQueue<Runnable> queue = 
				//new LinkedBlockingQueue<Runnable>();
				new ArrayBlockingQueue<Runnable>(10);
		ExecutorService executor  = new ThreadPoolExecutor(
					5, 		//core
					10, 	//max
					120L, 	//2fenzhong
					TimeUnit.SECONDS,
					queue);
		
		for(int i = 0 ; i < 21; i++){
			executor.execute(new UseThreadPoolExecutor2());
		}
		Thread.sleep(1000);
		System.out.println("queue size:" + queue.size());		//10
		Thread.sleep(2000);
	}


}

```  

**输出**  
说明是一次执行10个，然后再从队列取出来执行，每次线程池只能维护10个线程
```java
任务1
任务3
任务2
任务4
任务5
任务6
任务7
任务8
任务9
任务10
queue size:10
任务11
任务12
任务13
任务14
任务15
任务16
任务17
任务20
任务19
任务18
```

特别感谢互联网架构师白鹤翔老师，本文大多出自他的讲解。  
笔者主要是记录笔记，以便之后翻阅，正所谓好记性不如烂笔头，烂笔头不如云笔记