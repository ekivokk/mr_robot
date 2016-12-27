package fr;

import fr.lib.Movement;

public class Run {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
 
  try {
	new Thread (new Movement()).start();
} catch (Exception e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
	}

}
