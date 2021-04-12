package swen30006.automail;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;

import swen30006.exceptions.ItemTooHeavyException;
import swen30006.simulation.ChargeCalculator;

/**
 * addToPool is called when there are mail items newly arrived at the building to add to the MailPool or
 * if a robot returns with some undelivered items - these are added back to the MailPool.
 * The data structure and algorithms used in the MailPool is your choice.
 * 
 */
public class MailPool {

	private class Item {
		int destination;
		MailItem mailItem;
		// Use stable sort to keep arrival time relative positions
		
		public Item(MailItem mailItem) {
			destination = mailItem.getDestFloor();
			this.mailItem = mailItem;
		}
	}
	
	public class ItemComparator implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			int order = 0;
			if (i1.destination < i2.destination) {
				order = 1;
			} else if (i1.destination > i2.destination) {
				order = -1;
			}
			return order;
		}
	}
	


	private LinkedList<Item> pool;
	private LinkedList<Robot> robots;

	public MailPool(int nrobots){
		// Start empty
		pool = new LinkedList<Item>();
		robots = new LinkedList<Robot>();
	}

	/**
     * Adds an item to the mail pool, is sorted by destinations
     * @param mailItem the mail item being added.
     */
	public void addToPool(MailItem mailItem) {
		Item item = new Item(mailItem);
		pool.add(item);
		pool.sort(new ItemComparator());
	}
	
	
	
	/**
     * load up any waiting robots with mailItems, if any.
     */
	public void loadItemsToRobot() throws ItemTooHeavyException {
		//List available robots
		ListIterator<Robot> i = robots.listIterator();
		while (i.hasNext()) {
			loadItem(i);
		}
	}
	
	//load items to the robot
	private void loadItem(ListIterator<Robot> i) throws ItemTooHeavyException {
		Robot robot = i.next();
		assert(robot.isEmpty());
		// System.out.printf("P: %3d%n", pool.size());
		ListIterator<Item> j = pool.listIterator();

		if (pool.size() > 0) {
			try {
			robot.addToHand(j.next().mailItem); // hand first as we want higher priority delivered first
			j.remove();
			if (pool.size() > 0) {
				robot.addToTube(j.next().mailItem);
				j.remove();
			}
			robot.dispatch(); // send the robot off if it has any items to deliver
			i.remove();       // remove from mailPool queue
			} catch (Exception e) { 
	            throw e; 
	        } 
		}
	}

	/* every time robot arrived, the mail pool will calc charge of each
	* mail item and move items whose charge exceeds threshold to the head of the linkedList */
	private void handlePriority(LinkedList<Item> pool) {

		// if charge threshold is zero, which means we don't need to handle priority
		// we put Threshold in ChargeCalculator to avoid MailPool coupling with Simulation class, while
		// ChargeCalculator already has coupling with Simulation class
		if(ChargeCalculator.getChargeThreshold() == 0) {
			return;
		}

		// travel all items in the pool, and calc the charge
		// if a item's charge exceeds threshold, move it to the head of the linked list
		// if not exceed, continue traveling
		int index = 0;
		for(int i =0;i<pool.size();i++) {
			Item item = pool.get(i);
			MailItem mailItem = item.mailItem;
			double mailCharge = ChargeCalculator.CalcCharge(mailItem);
			if (index ==2){
				break;
			}
			if (mailCharge > ChargeCalculator.getChargeThreshold()) {
				//do something
				pool.remove(item);
				pool.add(index,item);
				index++;
			}
		}
	}

	/**
     * @param robot refers to a robot which has arrived back ready for more mailItems to deliver
     */	
	public void registerWaiting(Robot robot) { // assumes won't be there already
		robots.add(robot);
	}

}
