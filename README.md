SEoIPServer
===========

Secure Element over IP server

Command list

LIST - List card readers present on system.
LOCK <CARDRDR_NUM> - Select and lock card reader.
UNLOCK - Unlock acquired card reader.
APDU <APDU command in hex form. e.g. 00 A4 04 00 07 D4 10 00 00 03 00 01> - Send apdu 

Response list

LIST <CARDRDR_NUM> <CARDRDR_NAME> - Card reader list
ENDLIST - End of cardreader list
APDU <APDU command response in hex form. e.g. 90 00> - Response APDU
OK - Command is successful
ERROR <ERROR_DESC> - Command is failed.


This project is released under GPLv3.
