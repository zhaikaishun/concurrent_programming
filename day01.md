关键字：线程安全，synchronized，多个线程多个锁，对象锁的同步和异步，脏读，synchronized锁重入，synchronized代码块，volatile关键字

github 地址： https://github.com/zhaikaishun/concurrent_programming  
本篇文章代码在Multi_001 中
## 线程安全
当多个线程访问某一个类（对象或者方法）时，这个累始终都能表现出正确的行为，那么这个类（对象或方法）就是线程安全的

## 1.1 synchronized
synchronized可以在任意的对象以及方法上加锁，加锁的这段代码称为"互斥区"。当多个线程访问某一个方法时，会以排队的方式进行处理（这里按照cpu分配的先后顺序而定），一个线程想要执行synchronized修饰的方法里面的代码。首先要尝试获得锁，如果拿到锁，执行synchronized代码体内容，拿不到锁，这个线程就会不断的尝试得到这把锁，知道拿到为止。而且是多个线程同时竞争这把锁

例子 sync001 ： 
run方法不加锁
```java
public class MyThread extends Thread{
	
	private int count = 5 ;
	
	//synchronized加锁
	public void run(){
		count--;
		System.out.println(this.currentThread().getName() + " count = "+ count);
	}
	
	public static void main(String[] args) {
		/**
		 * 分析：当多个线程访问myThread的run方法时，以排队的方式进行处理（这里排对是按照CPU分配的先后顺序而定的），
		 * 		一个线程想要执行synchronized修饰的方法里的代码：
		 * 		1 尝试获得锁
		 * 		2 如果拿到锁，执行synchronized代码体内容；拿不到锁，这个线程就会不断的尝试获得这把锁，直到拿到为止，
		 * 		   而且是多个线程同时去竞争这把锁。（也就是会有锁竞争的问题）
		 */
		MyThread myThread = new MyThread();
		Thread t1 = new Thread(myThread,"t1");
		Thread t2 = new Thread(myThread,"t2");
		Thread t3 = new Thread(myThread,"t3");
		Thread t4 = new Thread(myThread,"t4");
		Thread t5 = new Thread(myThread,"t5");
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
	}
}
--- 运行后结果一般不是 4 3 2 1 0, 我的某次运行结果如下-----
t1 count = 3
t4 count = 2
t2 count = 3
t3 count = 1
t5 count = 0
``` 
如果给run方法加一把锁，其他代码不变，上面的run方法修改如下
```java
	//synchronized加锁
	public synchronized void run(){
		count--;
		System.out.println(this.currentThread().getName() + " count = "+ count);
	}
--------无论运行多少次，结果都为----
t3 count = 4
t2 count = 3
t1 count = 2
t5 count = 1
t4 count = 0
```

## 1.2 多个线程多个锁
关键字synchronized取得的锁都是对象锁，而不是把一段代码（方法）当做锁，所以代码中哪个线程先执行synchronized关键字的方法，哪个线程就持有该方法所属对象的锁（Lock），两个对象，线程获得的就是两个不同的锁，他们互不影响
有一种情况则是相同的锁，即在静态方法上加synchronized 关键字，表示锁定.class类。类一级别的锁
例子 sync002： 
```java
public class MultiThread {
	private int num = 0;
	/** static */
	public synchronized void printNum(String tag){
		try {
			if(tag.equals("a")){
				num = 100;
				System.out.println("tag a, set num over!");
				Thread.sleep(1000);
			} else {
				num = 200;
				System.out.println("tag b, set num over!");
			}
			System.out.println("tag " + tag + ", num = " + num);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//注意观察run方法输出顺序
	public static void main(String[] args) {
		//俩个不同的对象
		final MultiThread m1 = new MultiThread();
		final MultiThread m2 = new MultiThread();
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				m1.printNum("a");
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			@Override 
			public void run() {
				m2.printNum("b");
			}
		});
		t1.start();
		t2.start();
	}
}
```  
按照我们的希望，由于printNum方法加锁了, 我们希望t1线程执行完后，再执行t2线程, 实际上我们看到的结果如下, t1并没有执行完，就执行了t2线程
```
tag a, set num over!
tag b, set num over!
tag b, num = 200
tag a, num = 100
```
原因是虽然在printNum方法中加锁了，但是由于这里是两个对象， m1和m2，这里就对应着两把锁，两把锁各自肯定互不影响.

类锁: 在上面的这个例子当中，如果给synchronized方法加上static进行修饰，那么就相当于给这个类加锁。这两把锁就会变成同一把锁了.
上面的例子修改代码如下，其他不变
```java
	private static int num = 0;
	/** static */
	public static synchronized void printNum(String tag){
		try {
			if(tag.equals("a")){
				num = 100;
				System.out.println("tag a, set num over!");
				Thread.sleep(1000);
			} else {
				num = 200;
				System.out.println("tag b, set num over!");
			}
			System.out.println("tag " + tag + ", num = " + num);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
```
输出为
```
tag a, set num over!
tag a, num = 100
tag b, set num over!
tag b, num = 200
```

## 1.3 对象锁的同步和异步
同步: synchronized  
同步的概念就是共享，如果不是共享的资源，就没有必要进行同步  
异步: asynchronized  
异步的概念就是独立，相互之间不受任何制约，类似于页面Ajax请求，我们还可以继续浏览或操作页面的内容，而这之间没有任何关系
同步的目的就是为了线程安全，对于线程安全来说，需要满足两个特性  
- 原子性
- 可见性

例子sync003
 t1线程调用mo.method1()方法，该方法加有锁，这个方法需要等待，也就是同步  
 t2线程调用mo.method2()方法，该方法没有加锁，这个方法是异步的
 ```java
public class MyObject {

	public synchronized void method1(){
		try {
			System.out.println(Thread.currentThread().getName());
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/** synchronized */
	public void method2(){
			System.out.println(Thread.currentThread().getName());
	}
	
	public static void main(String[] args) {
		
		final MyObject mo = new MyObject();
		/**
		 * 分析：
		 * t1线程先持有object对象的Lock锁，t2线程可以以异步的方式调用对象中的非synchronized修饰的方法
		 * t1线程先持有object对象的Lock锁，t2线程如果在这个时候调用对象中的同步（synchronized）方法则需等待，也就是同步
		 */
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				mo.method1();
			}
		},"t1");
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				mo.method2();
			}
		},"t2");
		
		t1.start();
		t2.start();
		
	}
	
}

 ```
 ## 1.4 脏读
 对于对象的同步和异步方法，我们在设计自己的程序的时候，一定要考虑问题的整体性，不然就会出现数据不一致的错误，很经典的错误就是脏读(dirtyread )  
例子： sync004  
   我们在对一个对象的方法加锁的时候，需要考虑业务的整体性，即为setValue/getValue 方法同时加锁synchronized同步关键字, 保证业务的原子性，不然会出现业务错误
   下面的例子中，我希望先set完之后，再get内容，但是我在set的时候让t1线程休眠2秒钟，而主方法只休息一秒，这样，会导致名字设置完了，但是密码还没有设置，但是主线程调用了getValue()方法，产生了密码的脏读

```java
public class DirtyRead {
	private String username = "kaishun";
	private String password = "123";
	
	public synchronized void setValue(String username, String password){
		this.username = username;
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.password = password;
		System.out.println("setValue最终结果：username = " + username + " , password = " + password);
	}
	
	public void getValue(){
		System.out.println("getValue方法得到：username = " + this.username + " , password = " + this.password);
	}
	
	public static void main(String[] args) throws Exception{
		
		final DirtyRead dr = new DirtyRead();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				dr.setValue("z3", "456");		
			}
		});
		t1.start();
		Thread.sleep(1000);
		dr.getValue();
	}
}
```  
输出
```
getValue方法得到：username = z3 , password = 123
setValue最终结果：username = z3 , password = 456
```  
若想要达到先set完才能get，只需要在get的时候也加上同步synchronized  

 ## 1.5 synchronized的其他概念

 ### synchronized锁重入:  
 关键字synchronized拥有锁重入的功能，也就是在使用synchronized事，当一个线程得到了一个对象的锁喉，再次请求此对象时是可以再次得到该对象的锁。
 示例 SyncException  
 这个案例是通过一个抛出一个异常，来释放锁，但是，需要注意的是：很多异常释放锁的情况，如果不及时处理，很可能导致程序业务出错。比如你在执行一个队列任务，很多对象都去在等待对一个对象正确执行完毕后再去释放锁，但是第一个对象由于异常的出现，导致业务逻辑没有正常执行完毕，就释放了锁，那么可想而知后续的对象执行的都是错位的逻辑。所以这一点需要引起注意，在编写代码的时候，一定要考虑周全.

```java
public class SyncException {

	private int i = 0;
	public synchronized void operation(){
		while(true){
			try {
				i++;
				Thread.sleep(100);
				System.out.println(Thread.currentThread().getName() + " , i = " + i);
				if(i == 20){
					//Integer.parseInt("a");
					throw new RuntimeException();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		
		final SyncException se = new SyncException();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				se.operation();
			}
		},"t1");
		t1.start();
	}
	
	
}
```

**Java内置锁synchronized的可重入性**  
当线程请求一个由其它线程持有的对象锁时，该线程会阻塞，而当线程请求由自己持有的对象锁时，如果该锁是重入锁,请求就会成功,否则阻塞. 
例子 com.kaishun.base.sync005 SyncDubbo1 类 :  
由于synchronized是重入锁，当调用method1时，在method1的内部，由于是一个线程，会在method1内再次请求一次sd这个对象锁，所以能在method1()方法中执行method2()方法，而不会造成死锁
```java
public class SyncDubbo1 {

	public synchronized void method1(){
		System.out.println("method1..");
		method2();
	}
	public synchronized void method2(){
		System.out.println("method2..");
		method3();
	}
	public synchronized void method3(){
		System.out.println("method3..");
	}
	
	public static void main(String[] args) {
		final SyncDubbo1 sd = new SyncDubbo1();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				sd.method1();
			}
		});
		t1.start();
	}
}

``` 
输出
```
method1..
method2..
method3..
```
再举一个例子 com.kaishun.base.sync005.SyncDubbo2 ：  
子类也可以重入父类  
```java
public class SyncDubbo2 {
	
	static class A {
		public int i = 10;
		public synchronized void methodA(){
			try {
				i--;
				System.out.println("A print i = " + i);
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	static class B extends A {
		public synchronized void methodB(){
			try {
				while(i > 0) {
					i--;
					System.out.println("B print i = " + i);
					Thread.sleep(100);		
					this.methodA();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				B sub = new B();
				sub.methodB();
			}
		});
		t1.start();
	}

}
------------------输出-----------------
B print i = 9
A print i = 8
B print i = 7
A print i = 6
B print i = 5
A print i = 4
B print i = 3
A print i = 2
B print i = 1
A print i = 0
```

## 1.6 synchronized代码块
synchronized可以使用任意的Object进行加锁，用法比较灵活  
举例 com.kaishun.base.sync006.ObjectLock： 
```java
/**
 * 使用synchronized代码块加锁,比较灵活
 * @author alienware
 *
 */
public class ObjectLock {

	public void method1(){
		synchronized (this) {	//对象锁
			try {
				System.out.println("do method1..");
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void method2(){		//类锁
		synchronized (ObjectLock.class) {
			try {
				System.out.println("do method2..");
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Object lock = new Object();
	public void method3(){		//任何对象锁
		synchronized (lock) {
			try {
				System.out.println("do method3..");
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) {
		
		final ObjectLock objLock = new ObjectLock();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				objLock.method1();
			}
		});
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				objLock.method2();
			}
		});
		Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				objLock.method3();
			}
		});
		
		t1.start();
		t2.start();
		t3.start();
		
		
	}
	
}

```
**注意1** 不要使用String的常量加锁，会出现死循环问题  
案例 com.kaishun.base.sync006.StringLock
```java
public class StringLock {

	public void method() {
		//new String("字符串常量")
		synchronized ("字符串常量") {
			try {
				while(true){
					System.out.println("当前线程 : "  + Thread.currentThread().getName() + "开始");
					Thread.sleep(1000);		
					System.out.println("当前线程 : "  + Thread.currentThread().getName() + "结束");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		final StringLock stringLock = new StringLock();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				stringLock.method();
			}
		},"t1");
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				stringLock.method();
			}
		},"t2");
		
		t1.start();
		t2.start();
	}
-----将会一直输出------
当前线程 : t1开始
当前线程 : t1结束
当前线程 : t1开始
当前线程 : t1结束
当前线程 : t1开始
当前线程 : t1结束
当前线程 : t1开始
当前线程 : t1结束
当前线程 : t1开始
当前线程 : t1结束
当前线程 : t1开始
当前线程 : t1结束
当前线程 : t1开始
当前线程 : t1结束
......

```

**注意2** 锁对象改变的问题，如果对象本身发生改变的时候，那么持有的锁就不同。如果对象本身不发生改变，那么依然是同步的，即使对象的属性发生了改变。
示例1 锁对象的改变问题 com.kaishun.base.sync006.ChangeLock
```java
package com.kaishun.base.sync006;
/**
 * 锁对象的改变问题
 * @author alienware
 *
 */
public class ChangeLock {

	private String lock = "lock";
	
	private void method(){
		synchronized (lock) {
			try {
				System.out.println("当前线程 : "  + Thread.currentThread().getName() + "开始");
				lock = "change lock";
				Thread.sleep(2000);
				System.out.println("当前线程 : "  + Thread.currentThread().getName() + "结束");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
	
		final ChangeLock changeLock = new ChangeLock();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				changeLock.method();
			}
		},"t1");
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				changeLock.method();
			}
		},"t2");
		t1.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t2.start();
	}
	
}
------输出------
当前线程 : t1开始
当前线程 : t2开始
当前线程 : t1结束
当前线程 : t2结束

```
示例2 锁对象的属性发生改变 com.kaishun.base.sync006.ModifyLock  
```
package com.kaishun.base.sync006;
/**
 * 同一对象属性的修改不会影响锁的情况
 * @author alienware
 *
 */
public class ModifyLock {
	
	private String name ;
	private int age ;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	
	public synchronized void changeAttributte(String name, int age) {
		try {
			System.out.println("当前线程 : "  + Thread.currentThread().getName() + " 开始");
			this.setName(name);
			this.setAge(age);
			
			System.out.println("当前线程 : "  + Thread.currentThread().getName() + " 修改对象内容为： " 
					+ this.getName() + ", " + this.getAge());
			
			Thread.sleep(2000);
			System.out.println("当前线程 : "  + Thread.currentThread().getName() + " 结束");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		final ModifyLock modifyLock = new ModifyLock();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				modifyLock.changeAttributte("张三", 20);
			}
		},"t1");
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				modifyLock.changeAttributte("李四", 21);
			}
		},"t2");
		
		t1.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t2.start();
	}
	
}
-----输出------
当前线程 : t1 开始
当前线程 : t1 修改对象内容为： 张三, 20
当前线程 : t1 结束
当前线程 : t2 开始
当前线程 : t2 修改对象内容为： 李四, 21
当前线程 : t2 结束

```

## 1.7 volatile关键字
volatile概念：volatile关键字的主要作用是使变量在多个线程间可见  
但是要注意: volatile关键字不具备synchronized关键字的原子性（同步）
  
![](http://or49tneld.bkt.clouddn.com/17-10-4/15410243.jpg)  

在java中，每一个线程都会有一块工作内存区，其中放着所有线程共享的主内存的变量值的拷贝。当线程执行时，他在自己的工作区中操作这些变量，为了存取一个工ixangde变量，一个线程通常先获取锁定并清除他的内存工作区，把这些共享变量从所有线程的共享内存区中正确的装入到他自己所在的工作内存区中，当线程解锁时保证该工作内存区中变量的值写回到共享内存中。  
volatile的作用就是强制线程到主内存（共享内存）里去读取变量，而不去线程工作内存区里面读取，从而实现了多个线程间的变量可见，也满足线程安全的可见性。   

**AtomicInteger 保证原子性**  
AtomicInteger是原子性的，但是，一个方法中的多个StomicInteger却不是原子性的
示例:  

```java
public class AtomicUse {

	private static AtomicInteger count = new AtomicInteger(0);
	
	//多个addAndGet在一个方法内是非原子性的，需要加synchronized进行修饰，保证4个addAndGet整体原子性
	/**synchronized*/
	public  int multiAdd(){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			count.addAndGet(1);
			count.addAndGet(2);
			count.addAndGet(3);
			count.addAndGet(4); //+10
			return count.get();
	}
	
	public static void main(String[] args) {
		
		final AtomicUse au = new AtomicUse();

		List<Thread> ts = new ArrayList<Thread>();
		for (int i = 0; i < 100; i++) {
			ts.add(new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println(au.multiAdd());
				}
			}));
		}

		for(Thread t : ts){
			t.start();
		}
	}
}
----输出并不是都是10的整数倍-----
```
上述运行并不都是10的整数倍，因为多个AtomicInteger增加后，就不一定是原子的了，想要是原子性的，需要在 multiAdd() 方法加上synchronized修饰。


























