# Traccia.RE - use cases#


we receive a trace from our car controller (mobile) that contains a valid plate number.
we need to verify:

 - if the plate number is associated to a valid parking slot (happy path)
    - the query is: start_date >= now() <= end_date, plate_number = plate_number
    - we send a notification to user to say "tank's to be honest and to remember him to close correctly the parking slot
 - if the parking slot doesn't exist, we try to find a valid user with this platenumber
    - we try to find inside users collection
    - if the user doesn't exist, we a send a notification to the police man that will go to phisically
    notification (using the coordinates with the initial trace)


