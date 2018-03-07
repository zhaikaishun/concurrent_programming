github地址：https://github.com/zhaikaishun/concurrent_programming  
示例都在Multi_002项目下 
**关键字**：线程之间通信，volatile进行线程之间的通信，wait/notify的方法，CountDownLatch实现线程间通信，wait和notify模拟Queue，ThreadLocal

## 2.1 线程之间通信
 
线程间通信概念：不介绍了，就是线程之间的通信
方法： 
使用wait/notify方法实现线程间的通信。（注意这两个方法都是object的类的方法）
1. wait和notify必须配合synchronized关键字使用
2. wait方法释放锁, notify方法不释放锁

**使用volatile进行线程之间的通信**
示例 com.kaishun.base.conn008.ListAdd1: 
t1线程向list中添加元素，当list的元素到达5个的时候，t2线程打印一句话，并且通过异常释放锁进行退出。  
```java
public class ListAdd1 {

	
	private volatile static List list = new ArrayList();	
	
	public void add(){
		list.add("kaishun");
	}
	public int size(){
		return list.size();
	}
	
	public static void main(String[] args) {
		
		final ListAdd1 list1 = new ListAdd1();
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					for(int i = 0; i <10; i++){
						list1.add();
						System.out.println("当前线程：" + Thread.currentThread().getName() + "添加了一个元素..");
						Thread.sleep(500);
					}	
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "t1");
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(list1.size() == 5){
						System.out.println("当前线程收到通知：" + Thread.currentThread().getName() + " list size = 5 线程停止..");
						throw new RuntimeException();
					}
				}
			}
		}, "t2");		
		
		t1.start();
		t2.start();
	}
}

```  
但是这样并不是很好，因为t2这个线程一直在轮询的调用list1.size()来进行判断。于是我们也可以使用wait和notify的方法来实现这个功能

**wait/notify的方法**  
```java
/**
 * wait notfiy 方法，wait释放锁，notfiy不释放锁
 * @author alienware
 *
 */
public class ListAdd2 {
	private volatile static List list = new ArrayList();	
	
	public void add(){
		list.add("kaishun");
	}
	public int size(){
		return list.size();
	}
	
	public static void main(String[] args) {
		
		final ListAdd2 list2 = new ListAdd2();
		
		// 1 实例化出来一个 lock
		// 当使用wait 和 notify 的时候 ， 一定要配合着synchronized关键字去使用
		final Object lock = new Object();
//		final CountDownLatch countDownLatch = new CountDownLatch(1);
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					synchronized (lock) {
						for(int i = 0; i <10; i++){
							list2.add();
							System.out.println("当前线程：" + Thread.currentThread().getName() + "添加了一个元素..");
							Thread.sleep(500);
							if(list2.size() == 5){
								System.out.println("已经发出通知..");
//								countDownLatch.countDown();
								lock.notify();
							}
						}						
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}, "t1");
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					if(list2.size() != 5){
						try {
							System.out.println("t2进入...");
							lock.wait();
//							countDownLatch.await();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					System.out.println("当前线程：" + Thread.currentThread().getName() + "收到通知线程停止..");
//					throw new RuntimeException();
				}
			}
		}, "t2");	
		
		t2.start();
		t1.start();
	}
}
------运行结果
t2进入...
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
已经发出通知..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t2收到通知线程停止..
```
这个方法可以实现线程之间的通信，但是还不是我们想要的，我们希望只要list的size()到了5, 就立马唤醒t2线程，打印t2线程收到通知。 这时候我们可以使用 CountDownLatch  



**CountDownLatch实现线程间通信**  
```java
public class ListAdd2 {
	private volatile static List list = new ArrayList();	
	
	public void add(){
		list.add("kaishun");
	}
	public int size(){
		return list.size();
	}
	
	public static void main(String[] args) {
		
		final ListAdd2 list2 = new ListAdd2();
		
		// 1 实例化出来一个 lock
		// 当使用wait 和 notify 的时候 ， 一定要配合着synchronized关键字去使用
		//final Object lock = new Object();

		final CountDownLatch countDownLatch = new CountDownLatch(1);

		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//synchronized (lock) {
						for(int i = 0; i <10; i++){
							list2.add();
							System.out.println("当前线程：" + Thread.currentThread().getName() + "添加了一个元素..");
							Thread.sleep(500);
							if(list2.size() == 5){
								System.out.println("已经发出通知..");
								countDownLatch.countDown();
								//lock.notify();
							}
						}						
					//}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}, "t1");
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				//synchronized (lock) {
					if(list2.size() != 5){
						try {
							//System.out.println("t2进入...");
							//lock.wait();
							countDownLatch.await();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					System.out.println("当前线程：" + Thread.currentThread().getName() + "收到通知线程停止..");
//					throw new RuntimeException();
				//}
			}
		}, "t2");	
		
		t2.start();
		t1.start();

	}

}
-------输出-------------
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
已经发出通知..
当前线程：t1添加了一个元素..
当前线程：t2收到通知线程停止..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
当前线程：t1添加了一个元素..
```
上面例子可以看出，只要t1到了5，就告诉t2，你可以执行下面的了，但是t2线程还是抢不过t1线程，所以还是稍微比t1慢了一点

## 2.2 wait和notify模拟Queue  
BlockingQueue: 是一个支持阻塞的队列，阻塞的放入和得到数据  
实现这个功能，主要是
1. 在queue队列满了的时候，put方法进入wait，等待take操作之后的notify。
2. 在queue队列为空的时候，take方法进入wait，等待put操作之后的notify，具体查看示例代码如下   
com.kaishun.base.conn009.MyQueue
```java
public class MyQueue {
	
	//1 需要一个承装元素的集合 
	private LinkedList<Object> list = new LinkedList<Object>();
	
	//2 需要一个计数器
	private AtomicInteger count = new AtomicInteger(0);
	
	//3 需要制定上限和下限
	private final int minSize = 0;
	
	private final int maxSize ;
	
	//4 构造方法
	public MyQueue(int size){
		this.maxSize = size;
	}
	
	//5 初始化一个对象 用于加锁
	private final Object lock = new Object();
	
	
	//put(anObject): 把anObject加到BlockingQueue里,如果BlockQueue没有空间,则调用此方法的线程被阻断，直到BlockingQueue里面有空间再继续.
	public void put(Object obj){
		synchronized (lock) {
			while(count.get() == this.maxSize){
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//1 加入元素
			list.add(obj);
			//2 计数器累加
			count.incrementAndGet();
			//3 通知另外一个线程（唤醒）
			lock.notify();
			System.out.println("新加入的元素为:" + obj);
		}
	}
	
	
	//take: 取走BlockingQueue里排在首位的对象,若BlockingQueue为空,阻断进入等待状态直到BlockingQueue有新的数据被加入.
	public Object take(){
		Object ret = null;
		synchronized (lock) {
			while(count.get() == this.minSize){
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//1 做移除元素操作
			ret = list.removeFirst();
			//2 计数器递减
			count.decrementAndGet();
			//3 唤醒另外一个线程
			lock.notify();
		}
		return ret;
	}
	
	public int getSize(){
		return this.count.get();
	}
	
	
	public static void main(String[] args) {
		
		final MyQueue mq = new MyQueue(5);
		mq.put("a");
		mq.put("b");
		mq.put("c");
		mq.put("d");
		mq.put("e");
		
		System.out.println("当前容器的长度:" + mq.getSize());
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				mq.put("f");
				mq.put("g");
			}
		},"t1");
		
		t1.start();

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				Object o1 = mq.take();
				System.out.println("移除的元素为:" + o1);
				Object o2 = mq.take();
				System.out.println("移除的元素为:" + o2);
			}
		},"t2");

		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t2.start();
	}
}

```

## 2.3 ThreadLocal  
ThreadLocal概念: 线程局部变量，是一种多线程间并发访问变量的解决方案。与其synchronized等加锁方式不同，THreadLocal完全不提供锁，而使用空间换时间的手段，为每个线程提供变量的独立副本，以保障线程安全。  
在高并发量或者竞争激烈的场景，使用ThreadLocal可以在一定程度上减少锁竞争。  
当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。  
案例： 
每个线程一个独立的变量副本，所以t2线程执行为null
```java
public class ConnThreadLocal {

	public static ThreadLocal<String> th = new ThreadLocal<String>();
	
	public void setTh(String value){
		th.set(value);
	}
	public void getTh(){
		System.out.println(Thread.currentThread().getName() + ":" + this.th.get());
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		final ConnThreadLocal ct = new ConnThreadLocal();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				ct.setTh("张三");
				ct.getTh();
			}
		}, "t1");
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					ct.getTh();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "t2");
		
		t1.start();
		t2.start();
	}
	
}
----------输出------------
t1:张三
t2:null
```  

## 2.4 高并发下的单例模式  

**懒汉模式**  需要两层的if判断  
```java
public class DubbleSingleton {

	private static DubbleSingleton ds;
	
	public  static DubbleSingleton getDs(){
		if(ds == null){
			try {
				//模拟初始化对象的准备时间...
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (DubbleSingleton.class) {
				if(ds == null){
					ds = new DubbleSingleton();
				}
			}
		}
		return ds;
	}
	
	public static void main(String[] args) {
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println(DubbleSingleton.getDs().hashCode());
			}
		},"t1");
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println(DubbleSingleton.getDs().hashCode());
			}
		},"t2");
		Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println(DubbleSingleton.getDs().hashCode());
			}
		},"t3");
		
		t1.start();
		t2.start();
		t3.start();
	}
}
------输出的hashCode码是一致的----------
21936835
21936835
21936835
```  

**饿汉模式**  
```
public class Singletion {
	
	private static class InnerSingletion {
		private static Singletion single = new Singletion();
	}
	
	public static Singletion getInstance(){
		return InnerSingletion.single;
	}
	
}
```