package com.kaishun.base.sync005;
/**
 * synchronized的重入
 * @author alienware
 *
 */
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
