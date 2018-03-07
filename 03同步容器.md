**关键字:**  同步容器，队列，ConcurrentMap, Copy-On-Write容器,并发Queue,   ConcurrentLinkedQueue, BlockQueue接口, ArrayBlockingQueue , LinkedBlockingQueue, PriorityBlockingQueue, DelayQueue, SynchronousQueue


## 3.1 同步容器
同步容器都是线程安全的，但是在某些场景下可能需要加锁来保护复合操作，复合类操作如 迭代，跳转，条件运算等，这些复合操作在多线程并发执行的时候。可能会出现意外行为，最经典的便是ConcurrentModificationException，原因是当容器迭代的过程中，被并发的修改了内容。  
同步类容器： 如古老的Vector，HashTable。这些容器的同步功能其实都是由JDK的Collections.synchronized等工厂方法去实现的，其底层的机制无非就是用传统的synchronized关键字对每个公用的方法都进行同步，使得每次只能有一个线程访问容器的状态。这很明显不符合今天互联网时代高并发的需求，在保证线程安全的同时，也必须要有足够好的性能。  

JDK5.0以后提供了多种并发容器来替代同步容器从而改善性能。并发容器是专门针对并发设计的，使用ConcurrentHashMap来代替给予散列的HashTable，而且在ConcurrentHashMap中，添加了一些常见复合操作的支持，以及使用CopyOnWriteArrayList代替Voctor, 并发的CopyOnWriteArraySet, 以及并发的Queue， ConcurrentLinkedQueue和LinkedBlockingQueue, 前者是高性能的队列，后者是以阻塞形式的队列，具体实现Queue还有很多，例如ArrayBlockingQueue, PriorityBlockingQueue, SynchronousQueue等。  

## 4.1 ConcurrentMap  
ConcurrentMap接口有两个重要的实现  
- ConcurrentHashMap  
- ConcurrentSkipListMap(支持并发排序功能，弥补ConcurrnetHashMap)  
ConcurrentHashMap主要是利用了Segment(段)的方式，来减小锁的粒度，从而实现提高并发性能的机制, 最大可以分成16段。并且代码中大多共享变量使用Volatile关键字声明，目的是第一时间获取修改的内容，性能非常好 
如图，传统的HashTable，只有一段，对整个map进行加锁，锁的粒度比较大。而CorrentHashMap, 对这个map的某一个小Segment来进行加锁，在哪一段操作，只锁定哪一个段，其他段不影响，锁的粒度比较小，从而提高并发的性能   
![correntHashMap](http://or49tneld.bkt.clouddn.com/17-10-4/46638058.jpg)      

具体如何使用，和之前的HashMap几乎是一模一样的，还是随便看个例子吧  
```java
		ConcurrentHashMap<String, Object> chm = new ConcurrentHashMap<String, Object>();
		chm.put("k1", "v1");
		chm.put("k2", "v2");
		chm.put("k3", "v3");
		chm.putIfAbsent("k4", "vvvv"); // 如果key不存在，就加进去
```

## 4.2 Copy-On-Write容器  
Copy-On-Write简称COW， 是一种用于程序设计中的优化策略。 
Copy容器即写时复制的容器，先将当前容器进行Copy,复制出一个新的容器，然后想信的容器里面添加元素，添加完后，在讲原容器的引用指向新的容器。这样的好处是我们可以对CopyOnWrite容器进行并发的读而不用加锁，因为当前容器不会添加任何元素，所以CopyOnWrite也是一种读写分离的思想，读和写不同的容器，适用于读多写少的场景。   
JDK里的COW容器有两种：CopyOnWriteArrayList和CopyOnWriteArraySet, COW容器非常有用，可以在非常多的并发容器场景中使用到。   
使用方法也和原始的ArrayList, set一样  
```java
		CopyOnWriteArrayList<String> cwal = new CopyOnWriteArrayList<String>();
		CopyOnWriteArraySet<String> cwas = new CopyOnWriteArraySet<String>();
```



## 5.1 并发Queue  
在并发队列上JDK提供了两套实现，一个是以ConcurrentLinkedQueue为代表的高性能队列，一个是以BlockingQueue接口为代表的阻塞队列，无论哪种都继承自Queue  
![Queue的实现类](http://or49tneld.bkt.clouddn.com/17-10-4/72036961.jpg)  
![Queue的实现类1](http://or49tneld.bkt.clouddn.com/17-10-4/51989061.jpg)  

## 5.2 ConcurrentLinkedQueue
ConcurrentLinkedQueue: 是一个适用于高并发场景下的队列，通过无锁的方式，实现了高并发状态下的高性能，通常ConcurrentLinkedQueue性能好于BlockingQueue,他是一个基于链接节点的无界线程安全队列，该队列的元素遵循先进先出的原则，头是最新加入，尾是最近加入。该队列不允许null元素。  

ConcurrentLinkedQueue重要方法：  
add()和offer都是加入元素的方法(在ConcurrentLinkedQueue中，这两个方法没有任何区别)  
poll()和peek()都是取头元素节点，区别在于前者会删除元素，后者不会。
示例：  
com.kaishun.base.coll013.UseQueue  
**高性能无阻塞无界队列：ConcurrentLinkedQueue**
```
//高性能无阻塞无界队列：ConcurrentLinkedQueue

ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<String>();
q.offer("a");
q.offer("b");
q.offer("c");
q.offer("d");
q.add("e");

System.out.println(q.poll());	//a 从头部取出元素，并从队列里删除
System.out.println(q.size());	//4
System.out.println(q.peek());	//b
System.out.println(q.size());	//4

----输出-----
a
4
b
4

```  

## 5.3 BlockQueue接口  
有5种queue的实现。  
### ArrayBlockingQueue  
基于数组的阻塞队列实现，在ArrayBlockingQueue内部，维护了一个定常数组，一边缓存队列中的数据对象，其内部没有实现读写分离，也就意味着生产和消费不能完全并行，长度是需要定义的，可以指定先进先出或者先进后出。也叫**有界队列**，在很多场合下非常适用。  
```java
		ArrayBlockingQueue<String> array = new ArrayBlockingQueue<String>(5);
		array.put("a");
		array.put("b");
		array.add("c");
		array.add("d");
		array.add("e");
//		array.add("f");
		System.out.println(array.offer("a", 3, TimeUnit.SECONDS));
```  
由于指定的是5个长度，前面已经加了5个了，后面再次添加的时候，3秒内都加不进去，3秒后返回一个false，输出  
```
false
```  
若超过了还是用add方法，就会抛异常 IllegalStateException: Queue full ，例如  
```java
		ArrayBlockingQueue<String> array = new ArrayBlockingQueue<String>(5);
		array.put("a");
		array.put("b");
		array.add("c");
		array.add("d");
		array.add("e");
		array.add("f");  //这里接回抛异常
		System.out.println(array.offer("a", 3, TimeUnit.SECONDS));
```  


### LinkedBlockingQueue: 基于链表的阻塞队列，同ArrayBlockingQueue类似，其内部也维护者一个数据缓冲队列（该队列是由一个链表构成），LinkBlockingQueue之所以能够搞笑的处理并发数据，是因为其内部实现了读写分离锁，从而实现了生产者和消费者的完全并行运行，他是一个**无界队列**。  
```java
		//阻塞队列
		LinkedBlockingQueue<String> q = new LinkedBlockingQueue<String>();
		q.offer("a");
		q.offer("b");
		q.offer("c");
		q.offer("d");
		q.offer("e");
		q.add("f");
		System.out.println(q.size());
		
		for (Iterator iterator = q.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
		}
-----------输出-----------
6
a
b
c
d
e
f

```

drainTo一次取多个元素
```java
		//阻塞队列
		LinkedBlockingQueue<String> q = new LinkedBlockingQueue<String>();
		q.offer("a");
		q.offer("b");
		q.offer("c");
		q.offer("d");
		q.offer("e");
		q.add("f");
		System.out.println(q.size());

		List<String> list = new ArrayList<String>();
		//取3个元素，放入到 list 集合中去
		System.out.println(q.drainTo(list, 3));
		System.out.println(list.size());
		for (String string : list) {
			System.out.println(string);
		}
-----------输出-----------
6
3
3
a
b
c
```

### PriorityBlockingQueue: 基于优先级的阻塞队列（优先级的判断通过构造函数传入的Compator对象来决定，也就是说传入队列的对象必须实现Comparable接口），在实现PriorityBlockingQueue时，内部控制线程同步的锁采用的是公平锁，他也是一个无界的队列。  
示例： 
Task类，实现了Comparable的方法，重写compareTo方法
```java
public class Task implements Comparable<Task>{
	
	private int id ;
	private String name;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int compareTo(Task task) {
		return this.id > task.id ? 1 : (this.id < task.id ? -1 : 0);  
	}
	
	public String toString(){
		return this.id + "," + this.name;
	}
	
}
```
**UsePriorityBlockingQueue**类  
测试是否是有序队列  
```
public class UsePriorityBlockingQueue {

	
	public static void main(String[] args) throws Exception{
		
		
		PriorityBlockingQueue<Task> q = new PriorityBlockingQueue<Task>();
		
		Task t1 = new Task();
		t1.setId(3);
		t1.setName("id为3");
		Task t2 = new Task();
		t2.setId(4);
		t2.setName("id为4");
		Task t3 = new Task();
		t3.setId(1);
		t3.setName("id为1");
		
		//return this.id > task.id ? 1 : 0;
		q.add(t1);	//3
		q.add(t2);	//4
		q.add(t3);  //1
		
		// 1 3 4
		System.out.println("容器：" + q);
		System.out.println(q.take().getId());
		System.out.println("容器：" + q);
		System.out.println(q.take().getId());
		System.out.println(q.take().getId());
	}
}
```  
输出  
```java
容器：[1,id为1, 4,id为4, 3,id为3]
1
容器：[3,id为3, 4,id为4]
3
4
```  
说明，这种队列在没有take的时候，还不是排序的，take()时，才利用了排序，比较的方法  



### DelayQueue: 带有延迟时间的Queue, 其中的元素只有当其指定的延迟时间到了，才能够从队列中获取到该元素， **DelayQueue的元素必须实现Delayed 接口**， DelayQueue是一个没有大小限制的队列，应用场景很多，比如对缓存超时的数据进行一处，任务超时处理，空闲连接的关闭等等。   
经典的网吧上机案例：  
Wangmin 实现了Delayed接口  
```
public class Wangmin implements Delayed {  
    
    private String name;  
    //身份证  
    private String id;  
    //截止时间  
    private long endTime;  
    //定义时间工具类
    private TimeUnit timeUnit = TimeUnit.SECONDS;
      
    public Wangmin(String name,String id,long endTime){  
        this.name=name;  
        this.id=id;  
        this.endTime = endTime;  
    }  
      
    public String getName(){  
        return this.name;  
    }  
      
    public String getId(){  
        return this.id;  
    }  
      
    /** 
     * 用来判断是否到了截止时间 
     */  
    @Override  
    public long getDelay(TimeUnit unit) { 
        //return unit.convert(endTime, TimeUnit.MILLISECONDS) - unit.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    	return endTime - System.currentTimeMillis();
    }  
  
    /** 
     * 相互批较排序用 
     */  
    @Override  
    public int compareTo(Delayed delayed) {  
    	Wangmin w = (Wangmin)delayed;  
        return this.getDelay(this.timeUnit) - w.getDelay(this.timeUnit) > 0 ? 1:0;  
    } 

```

WangBa类  
```java
public class WangBa implements Runnable {  
    
    private DelayQueue<Wangmin> queue = new DelayQueue<Wangmin>();  
    
    public boolean yinye =true;  
      
    public void shangji(String name,String id,int money){  
        Wangmin man = new Wangmin(name, id, 1000 * money + System.currentTimeMillis());  
        System.out.println("网名"+man.getName()+" 身份证"+man.getId()+"交钱"+money+"块,开始上机...");  
        this.queue.add(man);  
    }  
      
    public void xiaji(Wangmin man){  
        System.out.println("网名"+man.getName()+" 身份证"+man.getId()+"时间到下机...");  
    }  
  
    @Override  
    public void run() {  
        while(yinye){  
            try {  
                Wangmin man = queue.take();  
                xiaji(man);  
            } catch (InterruptedException e) {  
                e.printStackTrace();  
            }  
        }  
    }  
      
    public static void main(String args[]){  
        try{  
            System.out.println("网吧开始营业");  
            WangBa siyu = new WangBa();  
            Thread shangwang = new Thread(siyu);  
            shangwang.start();  
              
            siyu.shangji("路人甲", "123", 1);  
            siyu.shangji("路人乙", "234", 10);  
            siyu.shangji("路人丙", "345", 5);  
        }  
        catch(Exception e){  
            e.printStackTrace();
        }  
  
    }  
}  

```  
输出  
```
网吧开始营业
网名路人甲 身份证123交钱1块,开始上机...
网名路人乙 身份证234交钱10块,开始上机...
网名路人丙 身份证345交钱5块,开始上机...
网名路人甲 身份证123时间到下机...
网名路人丙 身份证345时间到下机...
网名路人乙 身份证234时间到下机...
```
### SynchronousQueue: 
一种没有缓冲的队列，生产者产生的数据直接会被消费者获取并消费   
个人理解为虚拟队列，这个队列不存元素，生产与消费相互扔而已
```java
		final SynchronousQueue<String> q = new SynchronousQueue<String>();
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println(q.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		t1.start();
		Thread t2 = new Thread(new Runnable() {

			@Override
			public void run() {
				q.add("asdasd");
			}
		});
		t2.start();
---------输出-------
asdasd
```






























