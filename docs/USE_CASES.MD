# Traccia.RE - use cases#


The system receives a trace from the mobile car controller and the trace contains a valid plate number.

 - if the plate number is associated to a valid parking slot (start_date >= now() <= end_date, plate_number = plate_number)
    - nothing to do (happy path)
 - if the parking slot doesn't exist, 
    - search for a valid user with this platenumber
        - if the user doesn't exist, we a send a notification to the police man that will go to physically (using the coordinates with the initial trace)
        - if the user exists, send and email/sms notifcation to the user that he/she will be charged for the a default min payment  
    


