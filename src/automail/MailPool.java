package automail;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;

import exceptions.ItemTooHeavyException;
import simulation.ChargeCalculator;

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

	// Descending order
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
	private final int mailroomLocation;

	public MailPool(int nrobots, int mailroomLocation){
		// Start empty
		pool = new LinkedList<Item>();
		robots = new LinkedList<Robot>();
		this.mailroomLocation = mailroomLocation;
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

			// new feature
			// put 2 high priority mailItems at the head of the pool
			handlePriority(pool);

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
	* mail item and move 2 items whose charge exceeds threshold to the head of the linkedList */
	private void handlePriority(LinkedList<Item> pool) {

		// if charge threshold is zero, which means we don't need to handle priority
		// we put Threshold in ChargeCalculator to avoid MailPool coupling with Simulation class, while
		// ChargeCalculator already has coupling with Simulation class
		if(ChargeCalculator.getChargeThreshold() == 0) {
			return;
		}

		HashMap<Integer, Double> serviceFeeMap = new HashMap<>();

		// travel all items in the pool, and calc the charge
		// if a item's charge exceeds threshold, move it to the head of the linked list
		// if not exceed, continue traveling
		int index = 0;
		for(int i =0;i<pool.size();i++) {
			// only put 2 high priority mail at the head of the linked list
			if (index ==2){
				break;
			}
			Item item = pool.get(i);
			MailItem mailItem = item.mailItem;
			double mailCharge = 0;

			if(serviceFeeMap.containsKey(mailItem.getDestFloor())) {
				// if the service fee for this floor has been looked up
				mailCharge = serviceFeeMap.get(mailItem.getDestFloor());
			} else {
				// if the service fee for this floor has not been looked up
				mailCharge = ChargeCalculator.estimateCharge(mailItem, mailroomLocation);
				serviceFeeMap.put(mailItem.getDestFloor(), mailCharge);
			}

			if (mailCharge > ChargeCalculator.getChargeThreshold()) {
				//add to the head
				pool.remove(item);
				pool.add(index,item);
				index++;
			}
		}

		serviceFeeMap = null;
	}

	/**
     * @param robot refers to a robot which has arrived back ready for more mailItems to deliver
     */	
	public void registerWaiting(Robot robot) { // assumes won't be there already
		robots.add(robot);
	}

	public int getMailroomLocation() {
		return mailroomLocation;
	}
}
