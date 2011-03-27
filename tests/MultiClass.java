package multiclass;

import java.lang.System;
import java.util.Map;

class MultiClass {
	int a, b;
	String c;

	MultiClass()
	{
		System.out.println("new MultiClass");
	}

	void doSomething() {
		System.out.println("doSomething");
	}

	Map getMap() { return null; }
}

class AnotherClass {
	int x,y;
	String k;

	AnotherClass()
	{
		System.out.println("new AnotherClass");
	}

	void setMap(Map map) { }
}
