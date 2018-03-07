**关键字**：Concurrent.util常用类，CountDownLacth，CyclicBarrier，Callable和Future， 重入锁ReentrantLock， 锁的等待、通知，lock锁， 单Condition，多Condition,ReentrantReadWriteLock 读写锁,


github 地址： https://github.com/zhaikaishun/concurrent_programming 
本篇文章代码在Multi_006 中

---

# Concurrent.util常用类  

## CountDownLacth使用:  
它经常用于监听某些初始化操作，等初始化执行完毕后通知主线程继续工作。  
举例com.kaishun.height.concurrent019下  
```
public class UseCountDownLatch {

	public static void main(String[] args) {
		
		final CountDownLatch countDown = new CountDownLatch(2);
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("进入线程t1" + "等待其他线程处理完成...");
					countDown.await();
					System.out.println("t1线程继续执行...");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		},"t1");
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("t2线程进行初始化操作...");
					Thread.sleep(3000);
					System.out.println("t2线程初始化完毕，通知t1线程继续...");
					countDown.countDown();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("t3线程进行初始化操作...");
					Thread.sleep(4000);
					System.out.println("t3线程初始化完毕，通知t1线程继续...");
					countDown.countDown();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		t1.start();
		t2.start();
		t3.start();
	}
}
```


## CyclicBarrier使用:
假设有只有的一个场景：每个线程代表一个跑步运动员，当运动员都准备好后，才一起出发，只要有一个人没准备好，大家都等待  
举例：UseCyclicBarrier   
```java
public class UseCyclicBarrier {

	static class Runner implements Runnable {  
	    private CyclicBarrier barrier;  
	    private String name;  
	    
	    public Runner(CyclicBarrier barrier, String name) {  
	        this.barrier = barrier;  
	        this.name = name;  
	    }  
	    @Override  
	    public void run() {  
	        try {  
	            Thread.sleep(1000 * (new Random()).nextInt(5));  
	            System.out.println(name + " 准备OK.");  
	            barrier.await();  
	        } catch (InterruptedException e) {  
	            e.printStackTrace();  
	        } catch (BrokenBarrierException e) {  
	            e.printStackTrace();  
	        }  
	        System.out.println(name + " Go!!");  
	    }  
	} 
	
    public static void main(String[] args) throws IOException, InterruptedException {  
        CyclicBarrier barrier = new CyclicBarrier(3);  // 3 
        ExecutorService executor = Executors.newFixedThreadPool(3);  
        
        executor.submit(new Thread(new Runner(barrier, "zhangsan")));  
        executor.submit(new Thread(new Runner(barrier, "lisi")));  
        executor.submit(new Thread(new Runner(barrier, "wangwu")));  
  
        executor.shutdown();  
    }  
  
}  
-------输出-------------
lisi 准备OK.
zhangsan 准备OK.
wangwu 准备OK.
wangwu Go!!
lisi Go!!
zhangsan Go!!
```


## Callable和Future使用  
这个例子其实就是我们之前实现的Future模式，jdk给与我们衣蛾实现的封装，使用非常简单， Future模式非常适合在处理耗时很长的业务逻辑时使用，可以有效地减少系统的响应时间，提高系统的吞吐量。  
示例：  
```java
public class UseFuture implements Callable<String>{
	private String para;
	
	public UseFuture(String para){
		this.para = para;
	}
	
	/**
	 * 这里是真实的业务逻辑，其执行可能很慢
	 */
	@Override
	public String call() throws Exception {
		//模拟执行耗时
		Thread.sleep(5000);
		String result = this.para + "处理完成";
		return result;
	}
	
	//主控制函数
	public static void main(String[] args) throws Exception {
		String queryStr = "query";
		//构造FutureTask，并且传入需要真正进行业务逻辑处理的类,该类一定是实现了Callable接口的类
		FutureTask<String> future = new FutureTask<String>(new UseFuture(queryStr));
		
		FutureTask<String> future2 = new FutureTask<String>(new UseFuture(queryStr));
		//创建一个固定线程的线程池且线程数为1,
		ExecutorService executor = Executors.newFixedThreadPool(2);
		//这里提交任务future,则开启线程执行RealData的call()方法执行
		//submit和execute的区别： 第一点是submit可以传入实现Callable接口的实例对象， 第二点是submit方法有返回值
		
		Future f1 = executor.submit(future);		//单独启动一个线程去执行的
		Future f2 = executor.submit(future2);
		System.out.println("请求完毕");
		
		try {
			//这里可以做额外的数据操作，也就是主程序执行其他业务逻辑
			System.out.println("处理实际的业务逻辑...");
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//调用获取数据方法,如果call()方法没有执行完成,则依然会进行等待
		System.out.println("数据：" + future.get());
		System.out.println("数据：" + future2.get());
		
		executor.shutdown();
	}

}
-----------输出-------------------
请求完毕
处理实际的业务逻辑...
数据：query处理完成
数据：query处理完成
```

## 重入锁ReentrantLock  
重入锁，在需要进行同步的代码部分加上锁定，但不要忘记最后一定要释放锁定，不然会造成锁永远无法释放，其他线程永远进不来的结果。  【com.kaishun.height.lock020.UseReentrantLock】  

使用方法： 
1. 实例化一个锁： Lock lock = new ReentrantLock();  
2. 在需要加锁的地方使用lock.lock();  
3. 记住加锁的代码需要加上try catch finally  , finally的时候，一定要释放锁 lock.unlock

举例：  
```java
public class UseReentrantLock {
	
	private Lock lock = new ReentrantLock();
	
	public void method1(){
		try {
			lock.lock();
			System.out.println("当前线程:" + Thread.currentThread().getName() + "进入method1..");
			Thread.sleep(1000);
			System.out.println("当前线程:" + Thread.currentThread().getName() + "退出method1..");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			
			lock.unlock();
		}
	}
	
	public void method2(){
		try {
			lock.lock();
			System.out.println("当前线程:" + Thread.currentThread().getName() + "进入method2..");
			Thread.sleep(2000);
			System.out.println("当前线程:" + Thread.currentThread().getName() + "退出method2..");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			
			lock.unlock();
		}
	}
	
	public static void main(String[] args) {

		final UseReentrantLock ur = new UseReentrantLock();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				ur.method1();
				ur.method2();
			}
		}, "t1");

		t1.start();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//System.out.println(ur.lock.getQueueLength());
	}
	
	
}

-----------输出----------------
当前线程:t1进入method1..
当前线程:t1退出method1..
当前线程:t1进入method2..
当前线程:t1退出method2..
```
## 锁的等待、通知  
还记得我们在使用synchronized的时候，如果需要多线程间进行协作工作则需要Object的wait()和notify方法进行配合工作。  
那么同样，我们在使用Lock的时候，可以使用一个新的等待、通知的类，他就是Condition, 这个份Cibdutuib一定是针对具体某一吧锁的。也就是只有在有锁的情况下才会产生Condition.    
使用方法：  
1. Condition condition = lock.newCondition();  
2. 等待调用condition.await();  
3. 唤醒调用condition.signal();	  

### 单Condition
举例说明：  
```java
public class UseCondition {

	private Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	
	public void method1(){
		try {
			lock.lock();
			System.out.println("当前线程：" + Thread.currentThread().getName() + "进入等待状态..");
			Thread.sleep(3000);
			System.out.println("当前线程：" + Thread.currentThread().getName() + "释放锁..");
			condition.await();	// Object wait
			System.out.println("当前线程：" + Thread.currentThread().getName() +"继续执行...");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	public void method2(){
		try {
			lock.lock();
			System.out.println("当前线程：" + Thread.currentThread().getName() + "进入..");
			Thread.sleep(3000);
			System.out.println("当前线程：" + Thread.currentThread().getName() + "发出唤醒..");
			condition.signal();		//Object notify
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	public static void main(String[] args) {
		
		final UseCondition uc = new UseCondition();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				uc.method1();
			}
		}, "t1");
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				uc.method2();
			}
		}, "t2");
		t1.start();

		t2.start();
	}
}
```  
t1线程进入method1，然后wait释放锁， t2线程得到锁唤醒了t1  
输出结果：  
```
当前线程：t1进入等待状态..
当前线程：t1释放锁..
当前线程：t2进入..
当前线程：t2发出唤醒..
当前线程：t1继续执行...
```  

### 多Condition
我们可以通过一个Lock对象产生多个Condition进行多线程间的交互，非常的灵活。可以使得部分需要唤醒的线程被唤醒，其他线程则继续等待通知。  
例如下面这个例子，我们队一个lock，new出了2个Condition 一个是c1一个是c2 . 
m1和m2方法使用c1.wait。 m3方法使用c2.wait。 m4方法唤醒了c1.signalAll， m5方法唤醒的是c2.signal

代码：  
```java
public class UseManyCondition {

	private ReentrantLock lock = new ReentrantLock();
	private Condition c1 = lock.newCondition();
	private Condition c2 = lock.newCondition();
	
	public void m1(){
		try {
			lock.lock();
			System.out.println("当前线程：" +Thread.currentThread().getName() + "进入方法m1等待..");
			c1.await();
			System.out.println("当前线程：" +Thread.currentThread().getName() + "方法m1继续..");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	public void m2(){
		try {
			lock.lock();
			System.out.println("当前线程：" +Thread.currentThread().getName() + "进入方法m2等待..");
			c1.await();
			System.out.println("当前线程：" +Thread.currentThread().getName() + "方法m2继续..");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	public void m3(){
		try {
			lock.lock();
			System.out.println("当前线程：" +Thread.currentThread().getName() + "进入方法m3等待..");
			c2.await();
			System.out.println("当前线程：" +Thread.currentThread().getName() + "方法m3继续..");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	public void m4(){
		try {
			lock.lock();
			System.out.println("当前线程：" +Thread.currentThread().getName() + "唤醒..");
			c1.signalAll();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	public void m5(){
		try {
			lock.lock();
			System.out.println("当前线程：" +Thread.currentThread().getName() + "唤醒..");
			c2.signal();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	public static void main(String[] args) {
		
		
		final UseManyCondition umc = new UseManyCondition();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				umc.m1();
			}
		},"t1");
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				umc.m2();
			}
		},"t2");
		Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				umc.m3();
			}
		},"t3");
		Thread t4 = new Thread(new Runnable() {
			@Override
			public void run() {
				umc.m4();
			}
		},"t4");
		Thread t5 = new Thread(new Runnable() {
			@Override
			public void run() {
				umc.m5();
			}
		},"t5");
		
		t1.start();	// c1
		t2.start();	// c1
		t3.start();	// c2
		

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		t4.start();	// c1
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t5.start();	// c2
	}
}

```
**输出**  
先输出  
```
当前线程：t1进入方法m1等待..
当前线程：t3进入方法m3等待..
当前线程：t2进入方法m2等待..

```  
2秒后输出  
```
当前线程：t4唤醒..
当前线程：t1方法m1继续..
当前线程：t2方法m2继续..
```  
10秒后输出  
```
当前线程：t5唤醒..
当前线程：t3方法m3继续..
```

## ReentrantReadWriteLock 读写锁  
读写锁ReentrantReadWriteLock, 其核心就是实现读写分离的锁，在高并发访问下，尤其是读多写少的情况下，性能要远高于重入锁。  
之前学synchronized, ReentrantLock时，我们知道，同一时间内，只能有一个线程进行访问被锁定的代码，而读写锁不同，在读锁，多个线程可以并发的访问，而在写锁的时候，只能一个一个顺序的访问  
口诀：  读读共享， 写写互斥， 读写互斥。  
举例：  
```java

public class UseReentrantReadWriteLock {

	private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private ReadLock readLock = rwLock.readLock();
	private WriteLock writeLock = rwLock.writeLock();
	
	public void read(){
		try {
			readLock.lock();
			System.out.println("当前线程:" + Thread.currentThread().getName() + "进入...");
			Thread.sleep(3000);
			System.out.println("当前线程:" + Thread.currentThread().getName() + "退出...");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			readLock.unlock();
		}
	}
	
	public void write(){
		try {
			writeLock.lock();
			System.out.println("当前线程:" + Thread.currentThread().getName() + "进入...");
			Thread.sleep(3000);
			System.out.println("当前线程:" + Thread.currentThread().getName() + "退出...");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
	}
	
	public static void main(String[] args) {
		
		final UseReentrantReadWriteLock urrw = new UseReentrantReadWriteLock();
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				urrw.read();
			}
		}, "t1");
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				urrw.read();
			}
		}, "t2");
		Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				urrw.write();
			}
		}, "t3");
		Thread t4 = new Thread(new Runnable() {
			@Override
			public void run() {
				urrw.write();
			}
		}, "t4");		
		
		
	}
}


```
当运行下面语句时输出,读和读可以并发运行  
```
		t1.start();
		t2.start();
---------输出----------
当前线程:t2进入...
当前线程:t1进入...
当前线程:t1退出...
当前线程:t2退出...

```  
当运行下面读和写两个线程时, 读写互斥  
```
		t1.start(); // R
		t3.start(); // W  
----------输出---------
当前线程:t1进入...
当前线程:t1退出...
当前线程:t3进入...
当前线程:t3退出...
```
当运行两个写的时候，写写互斥  
```java
		t3.start();
		t4.start();
--------输出---------
当前线程:t3进入...
当前线程:t3退出...
当前线程:t4进入...
当前线程:t4退出...

```
特别感谢互联网架构师白鹤翔老师，本文大多出自他的视频讲解。  
笔者主要是记录笔记，以便之后翻阅，正所谓好记性不如烂笔头，烂笔头不如云笔记





















