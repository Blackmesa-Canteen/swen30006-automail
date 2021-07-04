# swen30006-automail
Software modeling &amp; design subject's project: Automail

# Background
Delivering Solutions Inc. (DS) has recently developed and provided a Robotic Mail Delivery system called Automail to the market. Automail is an automated mail sorting and delivery system designed to operate in a large building that has dedicated mail rooms. The system offers end-to-end receipt and delivery of mail items within the building and can be tweaked to fit many different installation environments. The system consists of two key components:

- A MailPool subsystem which holds mail items after their arrival at the building’s mail room. The mail pool decides the order in which mail items should be delivered.

- Delivery Robots which take mail items from the mail room and delivers them throughout the building.

Each robot has two hands and one tube, i.e., a backpack-like container attached to each robot for carrying items (see Figure 1). The robot can hold one item in its hands (i.e., two hands carry one item) and one item in its tube. If a robot is holding two items (i.e., one in its hands and one it its tube) it will always deliver the item in its hands first. An installation of Automail can manage a team of delivery robots of any reasonable size.

DS also provides a simulation subsystem to show that Automail can operate to deliver mail items within a building. The subsystem runs a simulation based on a property file and shows the delivery log of the robots and delivery statistics, e.g., the elapsed time before each mail items is delivered and how long has it taken for all the mail items to be delivered. The system generates a measure of the effectiveness of the system in delivering all mail items, considering time to deliver and the types of mail items. You do not need to be concerned about the detail of how this measure is calculated.

The simulation subsystem uses clock to simulate operations of the mail pool and robot subsystems. Broadly speaking, for each tick of the clock (i.e. one unit of time), the mail pool subsystem will load items to the robots if there are robots available at the mailroom; and the robots will either move to deliver an item (if there are items in its hands or tube), deliver an item, or move to return to the mailroom (if all items are delivered). Currently, the
robots offered by DS will take one unit of time when moving one step (i.e., moving up or down one floor in a building). For example, if a mail destination is four floors from the mailroom, a robot will take 4 units of time for delivery, plus 1 unit of time for delivery, plus 4 units of time for returning to the mailroom.

You can assume that the hardware of this system has been well tested and will work with the Robot subsystem. The current software seems to perform reasonably well. However, the system is not well documented.

# Task
Due to the COVID-19 and economic downturn, the government now allows building owners to charge additional service fees to tenants. Therefore, customers of DS have requested DS to update their robot mail system to support a charge capability and to pilot a change towards charging service fees. DS has hired your team to extend the latest version of Automail to include the ability to have robots charge tenants upon successful delivery of mail items.
