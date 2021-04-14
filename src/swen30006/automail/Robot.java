package swen30006.automail;

import swen30006.exceptions.ExcessiveDeliveryException;
import swen30006.exceptions.ItemTooHeavyException;
import swen30006.simulation.Building;
import swen30006.simulation.ChargeCalculator;
import swen30006.simulation.Clock;
import swen30006.simulation.IMailDelivery;

/**
 * The robot delivers mail!
 */
public class Robot {
	
    static public final int INDIVIDUAL_MAX_WEIGHT = 2000;

    IMailDelivery delivery;
    protected final String id;
    /** Possible states the robot can be in */
    public enum RobotState { DELIVERING, WAITING, RETURNING }
    public RobotState current_state;
    private int current_floor;
    private int destination_floor;
    private MailPool mailPool;
    private boolean receivedDispatch;
    
    private MailItem deliveryItem = null;
    private MailItem tube = null;

    private int deliveryCounter = 0;

    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     */
    public Robot(IMailDelivery delivery, MailPool mailPool, int number){
    	this.id = "R" + number;
        // current_state = RobotState.WAITING;
    	current_state = RobotState.RETURNING;
        current_floor = Building.MAILROOM_LOCATION;
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
    }
    
    /**
     * This is called when a robot is assigned the mail items and ready to dispatch for the delivery 
     */
    public void dispatch() {
    	receivedDispatch = true;
    }

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void operate() throws ExcessiveDeliveryException {   
    	switch(current_state) {
    		/** This state is triggered when the robot is returning to the mailroom after a delivery */
    		case RETURNING:
    			/** If its current position is at the mailroom, then the robot should change state */
                if(current_floor == Building.MAILROOM_LOCATION){
                	if (tube != null) {
                		mailPool.addToPool(tube);
                        System.out.printf("T: %3d > old addToPool [%s]%n", Clock.Time(), tube.toString());
                        tube = null;
                	}
        			/** Tell the sorter the robot is ready */
        			mailPool.registerWaiting(this);
                	changeState(RobotState.WAITING);
                } else {
                	/** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(Building.MAILROOM_LOCATION);
                }
                break;
    		case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if(!isEmpty() && receivedDispatch){
                	receivedDispatch = false;
                	deliveryCounter = 0; // reset delivery counter
                	setDestination();
                	changeState(RobotState.DELIVERING);

                	// new features: calculate mails' movements when start delivering
                    calcMailsMovements(deliveryItem, tube);
                }
                break;
    		case DELIVERING:
    			if(current_floor == destination_floor){ // If already here drop off either way

                    // new features: calculate charge
                    ChargeCalculator.CalcCharge(deliveryItem, deliveryItem.getMovements(), true);

    			    /** Delivery complete, report this to the simulator! */
                    delivery.deliver(deliveryItem);

                    deliveryItem = null;
                    deliveryCounter++;

                    if(deliveryCounter > 2){  // Implies a swen30006.swen30006.simulation bug
                    	throw new ExcessiveDeliveryException();
                    }
                    /** Check if want to return, i.e. if there is no item in the tube*/
                    if(tube == null){
                    	changeState(RobotState.RETURNING);
                    }
                    else {
                        /** If there is another item, set the robot's route to the location to deliver the item */
                        deliveryItem = tube;
                        tube = null;
                        setDestination();
                        changeState(RobotState.DELIVERING);
                    }
    			} else {
	        		/** The robot is not at the destination yet, move towards it! */
	        		moveTowards(destination_floor);
    			}
                break;
    	}
    }

    /**
     * Sets the route for the robot
     */
    private void setDestination() {
        /** Set the destination floor */
        destination_floor = deliveryItem.getDestFloor();
    }

    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    private void moveTowards(int destination) {
        if(current_floor < destination){
            current_floor++;
        } else {
            current_floor--;
        }
    }
    
    private String getIdTube() {
    	return String.format("%s(%1d)", this.id, (tube == null ? 0 : 1));
    }
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    private void changeState(RobotState nextState){
    	assert(!(deliveryItem == null && tube != null));
    	if (current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
    	}
    	current_state = nextState;
    	if(nextState == RobotState.DELIVERING){
            System.out.printf("T: %3d > %7s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
    	}
    }

	public MailItem getTube() {
		return tube;
	}

	public boolean isEmpty() {
		return (deliveryItem == null && tube == null);
	}

	public void addToHand(MailItem mailItem) throws ItemTooHeavyException {
		assert(deliveryItem == null);
		deliveryItem = mailItem;
		if (deliveryItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

	public void addToTube(MailItem mailItem) throws ItemTooHeavyException {
		assert(tube == null);
		tube = mailItem;
		if (tube.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

	public void calcMailsMovements(MailItem hand, MailItem tube) {

        if(deliveryCounter == 0) {
            // Only one mail needs to be delivered: the mail in the hand is the first one, while nothing in the tube.
            if(tube == null) {
                int movements = Math.abs(hand.getDestFloor() - mailPool.getMailroomLocation()) * 2;
                hand.accumulateMailMovements(movements);
            }
            // There are two mails (in hand & in tube) need to be delivered
            else {
                // Explaining Vars
                // if the mailRoom is between 2 destinations
                boolean hand_mailRoom_tube = ((hand.getDestFloor() < mailPool.getMailroomLocation()) && (tube.getDestFloor() > mailPool.getMailroomLocation())) ;
                boolean tube_mailRoom_hand = (hand.getDestFloor() > mailPool.getMailroomLocation() && tube.getDestFloor() < mailPool.getMailroomLocation());

                // if the mailroom is under both mailitems' destinations
                boolean mailRoom_mails = (hand.getDestFloor() >= mailPool.getMailroomLocation()) && (tube.getDestFloor() >= mailPool.getMailroomLocation());
                // if the destination of the mail in hand is lower then mail's in tube
                boolean mailRoom_hand_tube = (hand.getDestFloor() <= tube.getDestFloor());

                // if the mail room is above both mailItem's destinations
                // if the destination of the mail in hand is lower then mail's in tube
                boolean hand_tube_mailRoom = (hand.getDestFloor() <= tube.getDestFloor());

                // movements between destination of mail in hand & mailRoom
                int delta_hand_mailRoom = Math.abs(hand.getDestFloor() - mailPool.getMailroomLocation());

                int delta_tube_mailRoom = Math.abs(tube.getDestFloor() - mailPool.getMailroomLocation());

                int delta_hand_tube = Math.abs(hand.getDestFloor() - tube.getDestFloor());



                // if the mailroom is between 2 destinations
                if(hand_mailRoom_tube || tube_mailRoom_hand) {
                    hand.accumulateMailMovements(delta_hand_mailRoom * 2);
                    tube.accumulateMailMovements(delta_tube_mailRoom * 2);

                }
                // if the mailroom is under both mailitems' destinations
                else if (mailRoom_mails) {

                    // if the destination of the mail in hand is lower then mail's in tube
                    if(mailRoom_hand_tube) {
                        hand.accumulateMailMovements(delta_hand_mailRoom);
                        tube.accumulateMailMovements(delta_hand_tube * 2 + delta_hand_mailRoom);
                    } else {
                        hand.accumulateMailMovements(delta_hand_tube * 2 + delta_tube_mailRoom);
                        tube.accumulateMailMovements(delta_tube_mailRoom);
                    }
                }
                // if the mail room is above both mailItem's destinations
                else {
                    // if the destination of the mail in hand is lower then mail's in tube
                    if(hand_tube_mailRoom) {
                        hand.accumulateMailMovements(delta_hand_tube* 2 + delta_tube_mailRoom);
                        tube.accumulateMailMovements(delta_tube_mailRoom);

                    } else {
                        hand.accumulateMailMovements(delta_hand_mailRoom);
                        tube.accumulateMailMovements(delta_hand_tube * 2 + delta_hand_mailRoom);

                    }
                }
            }
        }
    }
}
